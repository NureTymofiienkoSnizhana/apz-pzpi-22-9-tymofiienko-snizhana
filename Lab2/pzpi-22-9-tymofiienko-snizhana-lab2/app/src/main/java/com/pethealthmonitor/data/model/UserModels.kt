package com.pethealthmonitor.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("ID")
    val _id: String,
    @SerializedName("Email")
    val email: String,
    @SerializedName("FullName")
    val full_name: String,
    @SerializedName("Role")
    val role: String,
    @SerializedName("PetsID")
    val pets_id: List<String>
)

data class UpdateProfileRequest(
    val full_name: String? = null,
    val email: String? = null,
    val password: String? = null
)