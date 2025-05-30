package com.pethealthmonitor.data.model

import com.google.gson.annotations.SerializedName

data class HealthData(
    @SerializedName("ID")
    val _id: String?,
    @SerializedName("PetID")
    val pet_id: String?,
    @SerializedName("Activity")
    val activity: Double,
    @SerializedName("SleepHours")
    val sleep_hours: Double,
    @SerializedName("Temperature")
    val temperature: Double,
    @SerializedName("Time")
    val time: HealthTime?
)

data class HealthTime(
    @SerializedName("T")
    val t: Long,
    @SerializedName("I")
    val i: Int
)