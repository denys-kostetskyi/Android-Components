package com.denyskostetskyi.androidcomponents.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.denyskostetskyi.androidcomponents.user.User
import com.denyskostetskyi.androidcomponents.user.UserDao

class UserContentProvider : ContentProvider() {
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, USERS, USER_CODE)
    }

    private val userDao = UserDao()

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            USER_CODE -> {
                val cursor = createCursor()
                userDao.getUsers().forEach { user ->
                    cursor.addRow(arrayOf(user.id, user.firstName, user.lastName))
                }
                cursor
            }

            else -> null
        }
    }

    private fun createCursor() = MatrixCursor(
        arrayOf(
            User::id.name,
            User::firstName.name,
            User::lastName.name
        )
    )

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            USER_CODE -> "vnd.android.cursor.dir/$AUTHORITY.$USERS"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        return when (uriMatcher.match(uri)) {
            USER_CODE -> {
                val user = parseUser(values)
                if (userDao.addUser(user)) {
                    Uri.withAppendedPath(CONTENT_URI, user.id.toString())
                } else {
                    null
                }
            }

            else -> null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (selectionArgs.isNullOrEmpty()) return 0
        return when (uriMatcher.match(uri)) {
            USER_CODE -> {
                val id = selectionArgs[0].toIntOrNull() ?: return 0
                if (userDao.deleteUserById(id)) 1 else 0
            }

            else -> 0
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (values == null) return 0
        return when (uriMatcher.match(uri)) {
            USER_CODE -> {
                val user = parseUser(values)
                if (userDao.updateUser(user)) 1 else 0
            }

            else -> 0
        }
    }

    private fun parseUser(values: ContentValues): User {
        val id = values.getAsInteger(User::id.name) ?: 0
        val firstName = values.getAsString(User::firstName.name) ?: ""
        val lastName = values.getAsString(User::lastName.name) ?: ""
        return User(id, firstName, lastName)
    }

    companion object {
        private const val AUTHORITY = "com.denyskostetskyi.androidcomponents"
        private const val USERS = "users"
        private const val USER_CODE = 1

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$USERS")
    }
}
