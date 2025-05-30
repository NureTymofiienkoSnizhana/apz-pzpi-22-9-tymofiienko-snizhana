package com.pethealthmonitor.util

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object HealthMonitorManager {

    private const val HEALTH_CHECK_WORK_NAME = "health_check_work"

    fun startHealthMonitoring(context: Context) {
        if (!PreferenceHelper.areNotificationsEnabled(context)) {
            return
        }

        val intervalMinutes = PreferenceHelper.getHealthCheckInterval(context).toLong()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val healthCheckRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(
            intervalMinutes, TimeUnit.MINUTES,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HEALTH_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            healthCheckRequest
        )
    }

    fun stopHealthMonitoring(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HEALTH_CHECK_WORK_NAME)
    }

    fun restartHealthMonitoring(context: Context) {
        stopHealthMonitoring(context)
        startHealthMonitoring(context)
    }

    fun isHealthMonitoringActive(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(HEALTH_CHECK_WORK_NAME)
            .get()

        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
        }
    }
}