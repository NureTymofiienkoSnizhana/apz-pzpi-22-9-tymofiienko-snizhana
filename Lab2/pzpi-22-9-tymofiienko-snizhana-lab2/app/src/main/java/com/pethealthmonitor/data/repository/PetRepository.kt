package com.pethealthmonitor.data.repository

import android.content.Context
import com.pethealthmonitor.api.ApiClient
import com.pethealthmonitor.data.model.HealthSummary
import com.pethealthmonitor.data.model.HealthHistoryResponse
import com.pethealthmonitor.data.model.OwnerHealthSummary
import com.pethealthmonitor.data.model.Pet
import com.pethealthmonitor.data.model.PetWithHealth
import com.pethealthmonitor.util.PreferenceHelper
import retrofit2.Response

class PetRepository {

    suspend fun getOwnerPets(token: String, context: Context): Response<List<Pet>> {
        val role = PreferenceHelper.getUserRole(context)
        return when (role) {
            "user" -> {
                // Для owner нужен owner_id
                val userId = PreferenceHelper.getUserId(context) ?: ""
                ApiClient.apiService.getOwnerPets("Bearer $token", userId)
            }
            else -> {
                // fallback для owner
                val userId = PreferenceHelper.getUserId(context) ?: ""
                ApiClient.apiService.getOwnerPets("Bearer $token", userId)
            }
        }
    }

    suspend fun getPetDetails(token: String, petId: String, context: Context): Response<PetWithHealth> {
        val role = PreferenceHelper.getUserRole(context)
        return when (role) {
            "user" -> ApiClient.apiService.getOwnerPetDetails("Bearer $token", petId)
            else -> ApiClient.apiService.getOwnerPetDetails("Bearer $token", petId) // fallback
        }
    }

    suspend fun getPetHealthSummary(petId: String, token: String, context: Context): Response<HealthSummary> {
        return try {
            ApiClient.apiService.getPetHealthSummary("Bearer $token", petId)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getPetHealthHistory(petId: String, token: String, context: Context): Response<HealthHistoryResponse> {
        return try {
            ApiClient.apiService.getPetHealthHistory("Bearer $token", petId)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getOwnerPetsHealthSummary(token: String, context: Context): Response<OwnerHealthSummary> {
        return try {
            ApiClient.apiService.getOwnerPetsHealthSummary("Bearer $token")
        } catch (e: Exception) {
            throw e
        }
    }
}