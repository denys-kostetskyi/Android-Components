package com.denyskostetskyi.androidcomponents.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denyskostetskyi.androidcomponents.R
import com.denyskostetskyi.androidcomponents.databinding.ActivityServicesBinding
import com.denyskostetskyi.androidcomponents.service.LocalService
import com.example.remoteservice.IRemoteService

class ServicesActivity : AppCompatActivity() {
    private var _binding: ActivityServicesBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("ActivityServicesBinding is null")

    private var localService: LocalService? = null
    private val isLocalServiceBound get() = localService != null

    private var remoteService: IRemoteService? = null
    private val isRemoteServiceBound get() = remoteService != null

    private val localServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocalService.LocalBinder
            localService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            localService = null
        }
    }
    private val remoteServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            remoteService = IRemoteService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.e(TAG, "RemoteService disconnected")
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
        setButtonClickListeners()
    }

    private fun setButtonClickListeners() {
        with(binding) {
            buttonStartLocalService.setOnClickListener { startLocalService() }
            buttonBindLocalService.setOnClickListener { bindLocalService() }
            buttonCallLocalService.setOnClickListener { callLocalService() }
            buttonBindRemoteService.setOnClickListener { bindRemoteService() }
            buttonCallRemoteService.setOnClickListener { callRemoteServiceBound() }
        }
    }

    private fun startLocalService() {
        startService(LocalService.newIntent(this, TIMER_DURATION))
    }

    private fun bindLocalService() {
        if (!isLocalServiceBound) {
            val intent = LocalService.newIntent(this, TIMER_DURATION)
            bindService(intent, localServiceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun callLocalService() {
        val message = if (!isLocalServiceBound) {
            getString(R.string.toast_local_service_is_not_bound)
        } else {
            localService?.message ?: NO_MESSAGE
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun bindRemoteService() {
        if (!isRemoteServiceBound) {
            val intent = Intent(INTENT_ACTION).apply {
                setClassName(PACKAGE_NAME, CLASS_NAME)
            }
            bindService(intent, remoteServiceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun callRemoteServiceBound() {
        val message = if (!isLocalServiceBound) {
            getString(R.string.toast_remote_service_is_not_bound)
        } else {
            remoteService?.message ?: NO_MESSAGE
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        if (isLocalServiceBound) {
            unbindService(localServiceConnection)
        }
        if (isRemoteServiceBound) {
            unbindService(remoteServiceConnection)
        }
    }

    companion object {
        private const val TAG = "ServicesActivity"
        private const val TIMER_DURATION = 15

        private const val INTENT_ACTION = "remoteService.AIDL"
        private const val PACKAGE_NAME = "com.denyskostetskyi.remoteservice"
        private const val CLASS_NAME = "$PACKAGE_NAME.RemoteService"
        private const val NO_MESSAGE = "No message"

        fun newIntent(context: Context) = Intent(context, ServicesActivity::class.java)
    }
}
