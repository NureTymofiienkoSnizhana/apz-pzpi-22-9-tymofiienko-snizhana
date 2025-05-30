package com.pethealthmonitor.data.model

import com.google.gson.annotations.SerializedName

data class PetWithHealth(
    @SerializedName("ID")
    val _id: String?,
    @SerializedName("Name")
    val name: String?,
    @SerializedName("Species")
    val species: String?,
    @SerializedName("Breed")
    val breed: String?,
    @SerializedName("Age")
    val age: Int?,
    @SerializedName("OwnerID")
    val owner_id: String?,
    @SerializedName("Health")
    val health: List<HealthData>?,
    val device_id: String? = null,
    val photo_url: String? = null
)