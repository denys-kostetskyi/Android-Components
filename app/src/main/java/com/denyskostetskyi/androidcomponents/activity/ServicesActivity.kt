package com.denyskostetskyi.androidcomponents.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.androidcomponents.IRemoteMockService
import com.denyskostetskyi.androidcomponents.databinding.ActivityServicesBinding
import com.denyskostetskyi.androidcomponents.service.TimerService

class ServicesActivity : AppCompatActivity() {
    private var _binding: ActivityServicesBinding? = null
    private val binding: ActivityServicesBinding
        get() = _binding ?: throw RuntimeException("ActivityServicesBinding is null")

    private var timerService: TimerService? = null
    private val isTimerServiceBound get() = timerService != null
    private val timerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            switchTimerButtonsVisibility()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            timerService = null
            switchTimerButtonsVisibility()
        }
    }

    private var remoteService: IRemoteMockService? = null
    private val isRemoteServiceBound get() = remoteService != null
    private val remoteServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            remoteService = IRemoteMockService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.e(TAG, "Service has unexpectedly disconnected")
            remoteService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setButtonClickListener()
    }

    private fun setButtonClickListener() {
        binding.buttonStartTimerService.setOnClickListener {
            startService(TimerService.newIntent(this, TIMER_DURATION))
        }
        binding.buttonBindTimerService.setOnClickListener {
            bindTimerService()
        }
        binding.buttonStartTimer.setOnClickListener {
            if (isTimerServiceBound) {
                timerService?.startTimer(TIMER_DURATION)
            } else {
                switchTimerButtonsVisibility()
            }
        }
        binding.buttonBindRemoteService.setOnClickListener {
            bindRemoteService()
        }
        binding.buttonCallRemoteService.setOnClickListener {
            if (isRemoteServiceBound) {
                remoteService?.showToast("MainActivity")
            } else {
                switchRemoteServiceButtonsVisibility()
            }
        }
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

    private fun bindRemoteService() {
        val intent = Intent().apply {
            setClassName(PACKAGE_NAME, CLASS_NAME)
        }
        bindService(intent, remoteServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun switchRemoteServiceButtonsVisibility() {
        with(binding) {
            buttonBindRemoteService.isVisible = !isRemoteServiceBound
            buttonCallRemoteService.isVisible = isRemoteServiceBound
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(timerServiceConnection)
    }

    companion object {
        private const val TAG = "ServicesActivity"
        private const val TIMER_DURATION = 15
        private const val PACKAGE_NAME = "com.denyskostetskyi.androidcomponents.service"
        private const val CLASS_NAME = "$PACKAGE_NAME.RemoteMockService"

        fun newIntent(context: Context) = Intent(context, ServicesActivity::class.java)
    }
}
