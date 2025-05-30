package com.pethealthmonitor.data.model

data class HealthHistoryResponse(
    val pet: PetInfo?,
    val health_data: List<HealthHistoryItem>?,
    val count: Int?
)

data class PetInfo(
    val id: String?,
    val name: String?,
    val species: String?,
    val breed: String?,
    val age: Int?
)