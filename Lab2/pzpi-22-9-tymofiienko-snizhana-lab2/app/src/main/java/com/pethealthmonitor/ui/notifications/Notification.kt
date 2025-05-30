package com.pethealthmonitor.ui.notifications

enum class NotificationType {
    TEMPERATURE,
    HEART_RATE,
    ACTIVITY_LEVEL
}

data class Notification(
    val id: String,
    val title: String,
    val petId: String,
    val petName: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType
)