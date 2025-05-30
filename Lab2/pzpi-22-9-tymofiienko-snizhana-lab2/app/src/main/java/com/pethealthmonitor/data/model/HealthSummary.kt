package com.pethealthmonitor.data.model

data class HealthSummary(
    val pet_id: String?,
    val pet_name: String?,
    val issues: List<String>?,
    val recommendations: List<String>?,
    val notification_level: String?, // "info", "warning", "urgent"
    val health_score: Int?,
    val overall_status: String?,
    val requires_attention: Boolean?
)