package com.denyskostetskyi.androidcomponents.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import com.denyskostetskyi.androidcomponents.R

class JobDownloaderService : JobService() {
    private var isTaskRunning = false
    private lateinit var handlerThread: HandlerThread
    private lateinit var serviceHandler: Handler

    override fun onCreate() {
        handlerThread = HandlerThread(HANDLER_THREAD_NAME)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        serviceHandler.post {
            try {
                processJob(params)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing job", e)
                jobFinished(params, true)
            } finally {
                stopSelf()
            }
        }
        return true
    }

    private fun processJob(params: JobParameters?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var workItem = params?.dequeueWork()
            while (workItem != null) {
                val fileNumber = workItem.intent.getIntExtra(KEY_FILE_NUMBER, 0)
                Log.d(TAG, "Downloading file #$fileNumber")
                Thread.sleep(SLEEP_DURATION)
                params?.completeWork(workItem)
                workItem = params?.dequeueWork()
            }
            jobFinished(params, false)
        } else {
            Log.e(TAG, "JobScheduler requires SDK 26")
            jobFinished(params, true)
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        if (isTaskRunning) {
            Log.i(TAG, "Job stopped before completion")
        }
        return isTaskRunning
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
    }

    companion object {
        private const val TAG = "JobDownloaderService"
        private const val HANDLER_THREAD_NAME = "JobDownloaderServiceHandlerThread"
        private const val SLEEP_DURATION = 1000L
        private const val KEY_FILE_NUMBER = "file_number"
        const val JOB_ID = 1000

        fun newIntent(fileNumber: Int) = Intent().apply {
            putExtra(KEY_FILE_NUMBER, fileNumber)
        }
    }
}
