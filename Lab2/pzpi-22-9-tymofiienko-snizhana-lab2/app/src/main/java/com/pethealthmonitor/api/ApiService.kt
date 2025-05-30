package com.pethealthmonitor.api

import com.pethealthmonitor.data.model.HealthSummary
import com.pethealthmonitor.data.model.HealthHistoryResponse
import com.pethealthmonitor.data.model.OwnerHealthSummary
import com.pethealthmonitor.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("login/auth")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @PUT("login/forgot-password")
    suspend fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Response<MessageResponse>

    @POST("login/logout")
    suspend fun logout(@Header("Authorization") authToken: String): Response<MessageResponse>

    // Owner Profile
    @GET("owner/profile")
    suspend fun getOwnerProfile(@Header("Authorization") authToken: String): Response<UserResponse>

    @PUT("owner/profile")
    suspend fun updateOwnerProfile(
        @Header("Authorization") authToken: String,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<MessageResponse>

    // Owner Pets
    @GET("owner/pets")
    suspend fun getOwnerPets(
        @Header("Authorization") authToken: String,
        @Query("owner_id") ownerId: String
    ): Response<List<Pet>>

    @GET("owner/pets/{id}")
    suspend fun getOwnerPetDetails(
        @Header("Authorization") authToken: String,
        @Path("id") petId: String
    ): Response<PetWithHealth>

    @GET("owner/pets/{id}/summary")
    suspend fun getPetHealthSummary(
        @Header("Authorization") token: String,
        @Path("id") petId: String
    ): Response<HealthSummary>

    @GET("owner/pets/{id}/health")
    suspend fun getPetHealthHistory(
        @Header("Authorization") token: String,
        @Path("id") petId: String
    ): Response<HealthHistoryResponse>

    // Health Summary for all pets
    @GET("owner/pets/health/summary")
    suspend fun getOwnerPetsHealthSummary(
        @Header("Authorization") token: String
    ): Response<OwnerHealthSummary>
}