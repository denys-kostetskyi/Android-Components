package com.denyskostetskyi.androidcomponents

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denyskostetskyi.androidcomponents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
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
        setButtonClickListener()
    }

    private fun setButtonClickListener() {
        binding.buttonStartComposeActivity.setOnClickListener {
            launchComposeActivity()
        }
    }

    private fun launchComposeActivity() {
        //TODO implement compose activity launching method
        //startActivityForResult(intent, REQUEST_CODE_COMPOSE_ACTIVITY);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_COMPOSE_ACTIVITY) {
            val result = data?.getStringExtra(KEY_COMPOSE_ACTIVITY_RESULT) ?: NO_RESULT
            binding.textViewComposeActivityResult.text = getString(
                R.string.result_from_compose_activity,
                result
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_COMPOSE_ACTIVITY = 1
        private const val KEY_COMPOSE_ACTIVITY_RESULT = "result"
        private const val NO_RESULT = "no result"

        fun newResultIntent(result: String) = Intent().apply {
            putExtra(KEY_COMPOSE_ACTIVITY_RESULT, result)
        }
    }
}