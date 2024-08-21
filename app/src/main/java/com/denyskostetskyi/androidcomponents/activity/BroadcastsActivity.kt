package com.denyskostetskyi.androidcomponents.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.denyskostetskyi.androidcomponents.R
import com.denyskostetskyi.androidcomponents.databinding.ActivityBroadcastsBinding
import com.denyskostetskyi.androidcomponents.receiver.AirplaneModeReceiver
import com.denyskostetskyi.androidcomponents.service.LoaderService

class BroadcastsActivity : AppCompatActivity() {
    private var _binding: ActivityBroadcastsBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("ActivityBroadcastsBinding is null")

    private var loaderService: LoaderService? = null
    private val isLoaderServiceBound get() = loaderService != null

    private val loaderServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LoaderService.LocalBinder
            loaderService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            loaderService = null
        }
    }

    private val airplaneModeReceiver = AirplaneModeReceiver { isAirplaneModeOn ->
        if (isAirplaneModeOn) {
            pauseLoad()
        } else {
            resumeLoad()
        }
    }

    private val dataLoadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                LoaderService.ACTION_DATA_LOADED -> handleDataLoaded(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBroadcastsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setButtonClickListener()
        bindLoaderService()
        registerReceivers()
    }

    private fun setButtonClickListener() {
        binding.buttonLoadData.setOnClickListener { startLoad() }
    }

    private fun bindLoaderService() {
        if (!isLoaderServiceBound) {
            val intent = LoaderService.newIntent(this)
            bindService(intent, loaderServiceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun registerReceivers() {
        registerReceiver(AirplaneModeReceiver { isAirplaneModeOn ->
            if (isAirplaneModeOn) pauseLoad() else resumeLoad()
        }, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(dataLoadReceiver, IntentFilter(LoaderService.ACTION_DATA_LOADED))
    }

    private fun startLoad() {
        if (isLoaderServiceBound) {
            loaderService?.startLoading()
            binding.progressBar.isVisible = true
        }
    }

    private fun pauseLoad() {
        loaderService?.let {
            it.pauseLoad()
            Toast.makeText(
                this,
                getString(R.string.toast_load_paused),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resumeLoad() {
        loaderService?.let {
            it.resumeLoad()
            Toast.makeText(
                this,
                getString(R.string.toast_load_resumed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleDataLoaded(intent: Intent) {
        val percent = intent.getIntExtra(LoaderService.EXTRA_LOADED_PERCENT, 0)
        binding.progressBar.setProgress(percent, true)
        if (percent == PERCENT_COMPLETED) {
            with(binding) {
                progressBar.isVisible = false
                progressBar.progress = 0
            }
            Toast.makeText(
                this,
                getString(R.string.toast_all_data_loaded),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isLoaderServiceBound) {
            unbindService(loaderServiceConnection)
        }
        unregisterReceiver(airplaneModeReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataLoadReceiver)
    }

    companion object {
        private const val PERCENT_COMPLETED = 100

        fun newIntent(context: Context) = Intent(context, BroadcastsActivity::class.java)
    }
}
