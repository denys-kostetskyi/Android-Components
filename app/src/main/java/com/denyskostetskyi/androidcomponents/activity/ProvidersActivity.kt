package com.denyskostetskyi.androidcomponents.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denyskostetskyi.androidcomponents.R
import com.denyskostetskyi.androidcomponents.databinding.ActivityProvidersBinding
import com.denyskostetskyi.androidcomponents.provider.UserContentProvider
import com.denyskostetskyi.androidcomponents.user.User
import kotlin.concurrent.thread

class ProvidersActivity : AppCompatActivity() {
    private var _binding: ActivityProvidersBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("ActivityProvidersBinding is null")

    private val canReadContacts: Boolean
        get() = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityProvidersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setButtonClickListeners()

    }

    private fun setButtonClickListeners() {
        with(binding) {
            buttonInsertUser.setOnClickListener { insertUser() }
            buttonQueryUser.setOnClickListener { queryUsers() }
            buttonUpdateUser.setOnClickListener { updateUser() }
            buttonDeleteUser.setOnClickListener { deleteUser() }
            buttonQueryContacts.setOnClickListener { queryContacts() }
        }
    }

    private fun insertUser() {
        val user = parseUser()
        if (user != null) {
            val (id, firstName, lastName) = user
            val values = ContentValues().apply {
                put(User::id.name, id)
                put(User::firstName.name, firstName)
                put(User::lastName.name, lastName)
            }
            val uri = contentResolver.insert(UserContentProvider.CONTENT_URI, values)
            uri?.let {
                Log.d(TAG, "Inserted User URI: $uri")
                binding.textViewResults.text = getString(
                    R.string.user_inserted_message,
                    id,
                    firstName,
                    lastName
                )
            }
        } else {
            binding.textViewResults.text = getString(R.string.enter_user_info)
        }
    }

    private fun queryUsers() {
        val cursor = contentResolver.query(
            UserContentProvider.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            val results = StringBuilder()
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(User::id.name))
                    val firstName =
                        cursor.getString(cursor.getColumnIndexOrThrow(User::firstName.name))
                    val lastName =
                        cursor.getString(cursor.getColumnIndexOrThrow(User::lastName.name))
                    results.append("ID: $id, Name: $firstName $lastName\n")
                    cursor.moveToNext()
                }
            }
            binding.textViewResults.text = results.toString()
            cursor.close()
        }
    }

    private fun updateUser() {
        val user = parseUser()
        if (user != null) {
            val (id, firstName, lastName) = user
            val values = ContentValues().apply {
                put(User::id.name, id)
                put(User::firstName.name, firstName)
                put(User::lastName.name, lastName)
            }
            val rowsUpdated = contentResolver.update(
                UserContentProvider.CONTENT_URI,
                values,
                null,
                null
            )
            binding.textViewResults.text = getString(R.string.updated_users, rowsUpdated)
        } else {
            binding.textViewResults.text = getString(R.string.enter_user_info)
        }
    }

    private fun deleteUser() {
        val id = parseUserId()
        if (id != null) {
            val rowsDeleted: Int = contentResolver.delete(
                UserContentProvider.CONTENT_URI,
                null,
                arrayOf(id.toString())
            )
            binding.textViewResults.text = getString(R.string.deleted_users, rowsDeleted)
        } else {
            binding.textViewResults.text = getString(R.string.enter_user_info)
        }
    }

    private fun queryContacts() {
        if (canReadContacts) {
            requestContacts()
        } else {
            requestReadContactsPermission()
        }
    }

    private fun requestContacts() {
        thread {
            contentResolver.query(
                Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )?.let {
                val results = StringBuilder()
                while (it.moveToNext()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(Contacts._ID))
                    val name = it.getString(it.getColumnIndexOrThrow(Contacts.DISPLAY_NAME))
                    results.append("ID: $id, Name: $name\n")
                }
                it.close()
                runOnUiThread {
                    binding.textViewResults.text = results.toString()
                }
            }
        }
    }

    private fun requestReadContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestContacts()
            } else {
                Log.d(TAG, "Permission denied")
            }
        }
    }

    private fun parseUserId() = binding.editTextId.text.toString().toIntOrNull()

    private fun parseUserName(field: EditText) = field.text.toString().trim()

    private fun parseUser(): User? {
        val id = parseUserId()
        val firstName = parseUserName(binding.editTextFirstName)
        val lastName = parseUserName(binding.editTextLastName)
        if (id != null && firstName.isNotEmpty() && lastName.isNotEmpty()) {
            return User(id, firstName, lastName)
        }
        return null
    }

    companion object {
        private const val TAG = "ProvidersActivity"
        private const val READ_CONTACTS_REQUEST_CODE = 135

        fun newIntent(context: Context) = Intent(context, ProvidersActivity::class.java)
    }
}
