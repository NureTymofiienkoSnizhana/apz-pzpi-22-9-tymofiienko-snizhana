package com.pethealthmonitor.data.model

data class HealthHistoryItem(
    val temperature: Double,
    val sleep_hours: Double,
    val activity: Double,
    val time: Long
)