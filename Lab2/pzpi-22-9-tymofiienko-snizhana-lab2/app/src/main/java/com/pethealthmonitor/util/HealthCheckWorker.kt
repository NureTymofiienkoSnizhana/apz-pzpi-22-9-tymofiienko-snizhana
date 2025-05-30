package com.pethealthmonitor.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pethealthmonitor.R
import com.pethealthmonitor.data.repository.PetRepository
import com.pethealthmonitor.ui.MainActivity

class HealthCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = PetRepository()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "pet_health_alerts"
        const val NOTIFICATION_CHANNEL_NAME = "Pet Health Alerts"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        return try {
            val token = PreferenceHelper.getAuthToken(context)
            if (token.isNullOrEmpty()) {
                return Result.success() // Пользователь не авторизован
            }

            val response = repository.getOwnerPetsHealthSummary(token, context)
            if (response.isSuccessful) {
                response.body()?.let { healthSummary ->
                    checkForHealthIssues(healthSummary)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkForHealthIssues(healthSummary: com.pethealthmonitor.data.model.OwnerHealthSummary) {
        createNotificationChannel()

        healthSummary.petsHealth?.forEach { petHealth ->
            // Проверяем, есть ли проблемы
            if (!petHealth.issues.isNullOrEmpty() && petHealth.overallStatus != "healthy") {
                // Проверяем, не отправляли ли уже уведомление для этого питомца недавно
                if (shouldSendNotification(petHealth.petId)) {
                    sendNotification(petHealth)
                    // Сохраняем время последнего уведомления
                    saveLastNotificationTime(petHealth.petId)
                }
            }
        }
    }

    private fun shouldSendNotification(petId: String?): Boolean {
        if (petId == null) return false

        val lastNotificationTime = PreferenceHelper.getLastNotificationTime(context, petId)
        val currentTime = System.currentTimeMillis()

        // Отправляем уведомление не чаще чем раз в час
        return (currentTime - lastNotificationTime) > (60 * 60 * 1000)
    }

    private fun saveLastNotificationTime(petId: String?) {
        if (petId != null) {
            PreferenceHelper.saveLastNotificationTime(context, petId, System.currentTimeMillis())
        }
    }

    private fun sendNotification(petHealth: com.pethealthmonitor.data.model.PetHealthStatus) {
        val title = "⚠️ Health Alert: ${petHealth.petName}"
        val message = when (petHealth.overallStatus) {
            "minor_issues" -> "${petHealth.petName} has minor health issues that need attention"
            "attention_needed" -> "${petHealth.petName} needs immediate attention!"
            "critical" -> "⚠️ URGENT: ${petHealth.petName} has critical health issues!"
            else -> "${petHealth.petName} has health issues"
        }

        // Создаем интент для открытия деталей питомца
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("petId", petHealth.petId)
            putExtra("petName", petHealth.petName)
            putExtra("openPetDetail", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            petHealth.petId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Определяем приоритет уведомления
        val priority = when (petHealth.overallStatus) {
            "critical" -> NotificationCompat.PRIORITY_HIGH
            "attention_needed" -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pet_placeholder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(createDetailedMessage(petHealth)))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + (petHealth.petId?.hashCode() ?: 0), notification)
    }

    private fun createDetailedMessage(petHealth: com.pethealthmonitor.data.model.PetHealthStatus): String {
        val issues = petHealth.issues?.joinToString("\n• ", "Issues:\n• ") ?: ""
        val tempInfo = "Temperature: ${petHealth.temperatureValue}°C (${petHealth.temperatureStatus})"
        val sleepInfo = "Sleep: ${petHealth.sleepValue}h (${petHealth.sleepStatus})"

        return "$issues\n\n$tempInfo\n$sleepInfo"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for pet health alerts"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}