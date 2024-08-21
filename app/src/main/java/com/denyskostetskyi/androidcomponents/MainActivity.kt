package com.denyskostetskyi.androidcomponents

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.androidcomponents.databinding.ActivityMainBinding
import com.denyskostetskyi.androidcomponents.service.TimerService

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw RuntimeException("ActivityMainBinding is null")

    private lateinit var timerService: TimerService
    private var isTimerServiceBound = false
    private val timerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            isTimerServiceBound = true
            switchTimerButtonsVisibility()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isTimerServiceBound = false
            switchTimerButtonsVisibility()
        }
    }

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
        binding.buttonStartTimerService.setOnClickListener {
            startService(TimerService.newIntent(this, TIMER_DURATION))
        }
        binding.buttonBindTimerService.setOnClickListener {
            bindTimerService()
        }
        binding.buttonStartTimer.setOnClickListener {
            if (isTimerServiceBound) {
                timerService.startTimer(TIMER_DURATION)
            }
        }
    }

    private fun launchComposeActivity() {
        val message = getString(R.string.hello_from_main_activity)
        val intent = ComposeActivity.newIntent(this, message)
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_ACTIVITY)
    }

    private fun bindTimerService() {
        if (!isTimerServiceBound) {
            val intent = TimerService.newIntent(this, TIMER_DURATION)
            bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun switchTimerButtonsVisibility() {
        with(binding) {
            buttonBindTimerService.isVisible = !isTimerServiceBound
            buttonStartTimer.isVisible = isTimerServiceBound
        }
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

    override fun onStop() {
        super.onStop()
        unbindService(timerServiceConnection)
        isTimerServiceBound = false
    }

    companion object {
        private const val REQUEST_CODE_COMPOSE_ACTIVITY = 1
        private const val KEY_COMPOSE_ACTIVITY_RESULT = "result"
        private const val NO_RESULT = "no result"

        private const val TIMER_DURATION = 15

        fun newResultIntent(result: String) = Intent().apply {
            putExtra(KEY_COMPOSE_ACTIVITY_RESULT, result)
        }
    }
}
