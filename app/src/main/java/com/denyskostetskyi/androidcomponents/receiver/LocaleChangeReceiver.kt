package com.denyskostetskyi.androidcomponents.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LocaleChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_LOCALE_CHANGED -> {
                handleLocaleChange(context)
            }
        }
    }

    private fun handleLocaleChange(context: Context?) {
        val newLocale = if (context != null) {
            context.resources.configuration.getLocales().get(0).displayName
        } else {
            "unknown"
        }
        Log.d(TAG, "Locale changed to: $newLocale")
    }

    companion object {
        private const val TAG = "LocaleChangeReceiver"
    }
}
