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

class TimerService : Service() {
    private var isTaskRunning = false
    private lateinit var handlerThread: HandlerThread
    private lateinit var serviceHandler: Handler
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(
            this,
            getString(R.string.toast_service_created),
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
        val duration = intent?.getIntExtra(KEY_TIMER_DURATION, DEFAULT_TIMER_DURATION)
            ?: DEFAULT_TIMER_DURATION
        startTimer(duration)
        return START_STICKY
    }

    fun startTimer(duration: Int) {
        if (duration < 0) {
            throw IllegalArgumentException("Duration can't be less than 0")
        }
        if (isTaskRunning) {
            Toast.makeText(
                this,
                getString(R.string.toast_service_task_already_running),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        Toast.makeText(
            this,
            getString(R.string.toast_service_destroyed),
            Toast.LENGTH_SHORT
        ).show()
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        private const val TAG = "TimerService"
        private const val HANDLER_THREAD_NAME = "TimerServiceHandlerThread"
        private const val KEY_TIMER_DURATION = "timer_duration"
        private const val DEFAULT_TIMER_DURATION = 10
        private const val SLEEP_DURATION = 1000L

        fun newIntent(context: Context, duration: Int) =
            Intent(context, TimerService::class.java).apply {
                putExtra(KEY_TIMER_DURATION, duration)
            }
    }
}
