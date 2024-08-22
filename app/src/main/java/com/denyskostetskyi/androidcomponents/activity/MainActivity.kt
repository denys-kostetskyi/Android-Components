package com.denyskostetskyi.androidcomponents.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denyskostetskyi.androidcomponents.R
import com.denyskostetskyi.androidcomponents.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("ActivityMainBinding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
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
            buttonStartComposeActivity.setOnClickListener { launchComposeActivity() }
            buttonStartServicesActivity.setOnClickListener { launchServicesActivity() }
            buttonStartBroadcastsActivity.setOnClickListener { launchBroadcastsActivity() }
            buttonStartProvidersActivity.setOnClickListener { launchProvidersActivity() }
        }
    }

    private fun launchComposeActivity() {
        val message = getString(R.string.hello_from_main_activity)
        val intent = ComposeActivity.newIntent(this, message)
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_ACTIVITY)
    }

    private fun launchServicesActivity() {
        startActivity(ServicesActivity.newIntent(this))
    }

    private fun launchBroadcastsActivity() {
        startActivity(BroadcastsActivity.newIntent(this))
    }

    private fun launchProvidersActivity() {
        startActivity(ProvidersActivity.newIntent(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_COMPOSE_ACTIVITY && resultCode == RESULT_OK) {
            val result = data?.getStringExtra(KEY_COMPOSE_ACTIVITY_RESULT) ?: NO_RESULT
            with(binding.textViewComposeActivityResult) {
                text = getString(
                    R.string.result_from_compose_activity,
                    result
                )
                visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_COMPOSE_ACTIVITY = 1
        private const val KEY_COMPOSE_ACTIVITY_RESULT = "result"
        private const val NO_RESULT = "no result"

        fun newResultIntent(result: String) = Intent().apply {
            putExtra(KEY_COMPOSE_ACTIVITY_RESULT, result)
        }
    }
}