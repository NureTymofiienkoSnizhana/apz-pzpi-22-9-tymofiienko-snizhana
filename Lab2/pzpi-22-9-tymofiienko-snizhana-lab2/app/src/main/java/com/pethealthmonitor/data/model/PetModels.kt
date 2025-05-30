package com.pethealthmonitor.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Pet(
    @SerializedName("ID")
    val _id: String,
    @SerializedName("Name")
    val name: String? = null,
    @SerializedName("Species")
    val type: String? = null,
    @SerializedName("Breed")
    val breed: String? = null,
    @SerializedName("Age")
    val age: Int? = null,
    @SerializedName("OwnerID")
    val owner_id: String? = null,
    @SerializedName("DeviceID")
    val device_id: String? = null,
    @SerializedName("PhotoURL")
    val photo_url: String? = null
)

data class ServerTime(
    @SerializedName("T")
    val t: Long,
    @SerializedName("I")
    val i: Int
) {
    fun toDate(): Date {
        return Date(t * 1000) // Конвертируем из секунд в миллисекунды
    }
}