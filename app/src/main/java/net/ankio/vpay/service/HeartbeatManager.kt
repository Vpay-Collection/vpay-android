package net.ankio.vpay.service

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class HeartbeatManager(private val context: Context) {
    fun startHeartbeat() {
        val heartbeatRequest = PeriodicWorkRequestBuilder<HeartbeatWorker>(10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(heartbeatRequest)
    }

    fun stopHeartbeat() {
        WorkManager.getInstance(context).cancelAllWorkByTag(HeartbeatWorker::class.java.name)
    }
}