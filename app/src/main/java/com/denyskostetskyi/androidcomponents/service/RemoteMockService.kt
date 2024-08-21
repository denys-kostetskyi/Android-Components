package com.denyskostetskyi.androidcomponents.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.denyskostetskyi.androidcomponents.IRemoteMockService
import com.denyskostetskyi.androidcomponents.R

class RemoteMockService : Service() {
    private val binder = object : IRemoteMockService.Stub() {
        override fun showToast(text: String?) {
            Toast.makeText(
                this@RemoteMockService,
                getString(R.string.toast_message_from_remote_service),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("XXX", "bind")
        return binder
    }
}
