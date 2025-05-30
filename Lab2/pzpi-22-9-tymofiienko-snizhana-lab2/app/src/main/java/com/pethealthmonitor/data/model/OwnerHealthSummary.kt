package com.pethealthmonitor.data.model

import com.google.gson.annotations.SerializedName

data class OwnerHealthSummary(
    @SerializedName("owner_id")
    val ownerId: String?,
    @SerializedName("total_pets")
    val totalPets: Int?,
    @SerializedName("healthy_pets")
    val healthyPets: Int?,
    @SerializedName("problems_count")
    val problemsCount: Int?,
    @SerializedName("last_updated")
    val lastUpdated: String?,
    @SerializedName("pets_health")
    val petsHealth: List<PetHealthStatus>?
)

data class PetHealthStatus(
    @SerializedName("pet_id")
    val petId: String?,
    @SerializedName("pet_name")
    val petName: String?,
    @SerializedName("pet_species")
    val petSpecies: String?,
    @SerializedName("last_check_time")
    val lastCheckTime: String?,
    @SerializedName("temperature_status")
    val temperatureStatus: String?,
    @SerializedName("temperature_value")
    val temperatureValue: Double?,
    @SerializedName("sleep_status")
    val sleepStatus: String?,
    @SerializedName("sleep_value")
    val sleepValue: Double?,
    @SerializedName("overall_status")
    val overallStatus: String?,
    @SerializedName("issues")
    val issues: List<String>?
)