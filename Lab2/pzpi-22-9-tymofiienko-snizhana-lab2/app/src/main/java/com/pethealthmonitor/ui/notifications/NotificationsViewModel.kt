package com.pethealthmonitor.ui.notifications

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealthmonitor.data.model.OwnerHealthSummary
import com.pethealthmonitor.data.model.PetHealthStatus
import com.pethealthmonitor.data.repository.PetRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationsViewModel : ViewModel() {

    private val repository = PetRepository()

    private val _notificationsResult = MutableLiveData<NotificationsResult>()
    val notificationsResult: LiveData<NotificationsResult> = _notificationsResult

    private val _healthSummaryResult = MutableLiveData<HealthSummaryResult>()
    val healthSummaryResult: LiveData<HealthSummaryResult> = _healthSummaryResult

    fun loadNotifications(token: String, context: Context) {
        _notificationsResult.value = NotificationsResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getOwnerPetsHealthSummary(token, context)
                if (response.isSuccessful) {
                    response.body()?.let { healthSummary ->
                        val notifications = convertHealthSummaryToNotifications(healthSummary)
                        _notificationsResult.value = NotificationsResult.Success(notifications)
                        _healthSummaryResult.value = HealthSummaryResult.Success(healthSummary)
                    } ?: run {
                        _notificationsResult.value = NotificationsResult.Error("Failed to load health data")
                    }
                } else {
                    _notificationsResult.value = NotificationsResult.Error("Failed to load notifications: ${response.message()}")
                }
            } catch (e: Exception) {
                _notificationsResult.value = NotificationsResult.Error("Network error: ${e.message}")
            }
        }
    }

    private fun convertHealthSummaryToNotifications(healthSummary: OwnerHealthSummary): List<Notification> {
        val notifications = mutableListOf<Notification>()

        healthSummary.petsHealth?.forEach { petHealth ->
            // Создаем уведомления для каждой проблемы
            petHealth.issues?.forEach { issue ->
                val notification = createNotificationFromIssue(petHealth, issue)
                notifications.add(notification)
            }
        }

        // Сортируем по времени (новые сначала)
        return notifications.sortedByDescending { it.timestamp }
    }

    private fun createNotificationFromIssue(petHealth: PetHealthStatus, issue: String): Notification {
        val notificationType = when {
            issue.contains("temperature", ignoreCase = true) -> NotificationType.TEMPERATURE
            issue.contains("heart", ignoreCase = true) -> NotificationType.HEART_RATE
            issue.contains("sleep", ignoreCase = true) || issue.contains("activity", ignoreCase = true) -> NotificationType.ACTIVITY_LEVEL
            else -> NotificationType.TEMPERATURE
        }

        val title = when (notificationType) {
            NotificationType.TEMPERATURE -> "Temperature Alert"
            NotificationType.HEART_RATE -> "Heart Rate Alert"
            NotificationType.ACTIVITY_LEVEL -> "Activity Alert"
        }

        // Парсим время последней проверки
        val timestamp = parseLastCheckTime(petHealth.lastCheckTime) ?: System.currentTimeMillis()

        // Создаем подробное сообщение
        val message = createDetailedMessage(petHealth, issue)

        return Notification(
            id = "${petHealth.petId}_${issue.hashCode()}",
            title = title,
            petId = petHealth.petId ?: "",
            petName = petHealth.petName ?: "Unknown Pet",
            message = message,
            timestamp = timestamp,
            type = notificationType
        )
    }

    private fun createDetailedMessage(petHealth: PetHealthStatus, issue: String): String {
        return when {
            issue.contains("temperature", ignoreCase = true) -> {
                "${petHealth.petName} has ${petHealth.temperatureStatus} temperature: ${petHealth.temperatureValue}°C"
            }
            issue.contains("sleep", ignoreCase = true) -> {
                "${petHealth.petName} has ${petHealth.sleepStatus} sleep: ${petHealth.sleepValue}h"
            }
            else -> {
                "${petHealth.petName}: $issue"
            }
        }
    }

    private fun parseLastCheckTime(timeString: String?): Long? {
        if (timeString.isNullOrEmpty()) return null

        return try {
            // Парсим ISO 8601 формат: "2025-05-23T09:30:24Z"
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(timeString)?.time
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    sealed class NotificationsResult {
        object Loading : NotificationsResult()
        data class Success(val notifications: List<Notification>) : NotificationsResult()
        data class Error(val message: String) : NotificationsResult()
    }

    sealed class HealthSummaryResult {
        data class Success(val healthSummary: OwnerHealthSummary) : HealthSummaryResult()
        data class Error(val message: String) : HealthSummaryResult()
    }
}