package com.denyskostetskyi.androidcomponents.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AirplaneModeReceiver(private val onChanged: (state: Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED == intent?.action) {
            val isTurnedOn = intent.getBooleanExtra("state", false)
            onChanged(isTurnedOn)
        }
    }
}
