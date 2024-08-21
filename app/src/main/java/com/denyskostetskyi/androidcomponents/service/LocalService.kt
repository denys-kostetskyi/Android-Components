package com.denyskostetskyi.androidcomponents.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.denyskostetskyi.androidcomponents.R

class LocalService : Service() {
    private var isTaskRunning = false
    private lateinit var handlerThread: HandlerThread
    private lateinit var serviceHandler: Handler
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(
            this,
            getString(R.string.toast_local_service_started),
            Toast.LENGTH_SHORT
        ).show()
        handlerThread = HandlerThread(HANDLER_THREAD_NAME)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (isTaskRunning) {
            Toast.makeText(
                this,
                getString(R.string.toast_service_task_already_running),
                Toast.LENGTH_SHORT
            ).show()
            return START_NOT_STICKY
        }
        val duration = intent?.getIntExtra(KEY_TIMER_DURATION, DEFAULT_TIMER_DURATION)
            ?: DEFAULT_TIMER_DURATION
        isTaskRunning = true
        serviceHandler.post {
            try {
                for (i in 0..duration) {
                    Thread.sleep(SLEEP_DURATION)
                    Log.d(TAG, i.toString())
                }
            } finally {
                isTaskRunning = false
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    val message get() = getString(R.string.message_from_local_service)

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        Toast.makeText(
            this,
            getString(R.string.toast_local_service_destroyed),
            Toast.LENGTH_SHORT
        ).show()
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocalService = this@LocalService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        private const val TAG = "LocalService"
        private const val HANDLER_THREAD_NAME = "LocalServiceHandlerThread"
        private const val KEY_TIMER_DURATION = "timer_duration"
        private const val DEFAULT_TIMER_DURATION = 10
        private const val SLEEP_DURATION = 1000L

        fun newIntent(context: Context, duration: Int): Intent {
            if (duration < 0) {
                throw IllegalArgumentException("Duration can't be less than 0")
            }
            return Intent(context, LocalService::class.java).apply {
                putExtra(KEY_TIMER_DURATION, duration)
            }
        }
    }
}
