package com.denyskostetskyi.androidcomponents.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.concurrent.thread

class LoaderService : Service() {
    private var loadProgress = 0
    private var isPaused = false
    private var isLoading = false
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLoading()
        return START_NOT_STICKY
    }

    fun startLoading() {
        if (!isLoading) {
            isLoading = true
            loadProgress = 0
            thread {
                while (loadProgress < 100) {
                    if (!isPaused) {
                        loadData()
                    }
                    Thread.sleep(SLEEP_DURATION)
                }
                isLoading = false
            }
        }
    }

    fun pauseLoad() {
        isPaused = true
    }

    fun resumeLoad() {
        isPaused = false
    }

    private fun loadData() {
        loadProgress++
        val intent = Intent(ACTION_DATA_LOADED).apply {
            putExtra(EXTRA_LOADED_PERCENT, loadProgress)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService(): LoaderService = this@LoaderService
    }

    companion object {
        private const val SLEEP_DURATION = 200L
        const val EXTRA_LOADED_PERCENT = "loaded_percent"
        const val ACTION_DATA_LOADED = "data_loaded"

        fun newIntent(context: Context) = Intent(context, LoaderService::class.java)
    }
}
