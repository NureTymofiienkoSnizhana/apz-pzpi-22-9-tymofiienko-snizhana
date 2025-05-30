package com.pethealthmonitor.data.repository

import android.content.Context
import com.pethealthmonitor.api.ApiClient
import com.pethealthmonitor.data.model.MessageResponse
import com.pethealthmonitor.data.model.UpdateProfileRequest
import com.pethealthmonitor.data.model.UserResponse
import com.pethealthmonitor.util.PreferenceHelper
import retrofit2.Response

class UserRepository {
    suspend fun getUserProfile(token: String, context: Context): Response<UserResponse> {
        val role = PreferenceHelper.getUserRole(context)

        // Попробуем загрузить данные на основе сохраненной роли
        if (!role.isNullOrEmpty()) {
            return when (role) {
                "user" -> ApiClient.apiService.getOwnerProfile("Bearer $token")
                else -> ApiClient.apiService.getOwnerProfile("Bearer $token") // fallback
            }
        }

        // Если роль неизвестна, попробуем все endpoints по очереди
        val endpoints = listOf("user", "vet", "admin")

        for (roleType in endpoints) {
            try {
                val response = when (roleType) {
                    else -> ApiClient.apiService.getOwnerProfile("Bearer $token")
                }

                if (response.isSuccessful && response.body() != null) {
                    // Сохраняем найденную роль для следующих запросов
                    PreferenceHelper.saveUserRole(context, roleType)
                    return response
                }
            } catch (e: Exception) {
                // Продолжаем пробовать следующий endpoint
                continue
            }
        }

        // Если ничего не сработало, возвращаем последний ответ как fallback
        return ApiClient.apiService.getOwnerProfile("Bearer $token")
    }

    suspend fun updateUserProfile(
        token: String,
        context: Context,
        fullName: String? = null,
        email: String? = null,
        password: String? = null
    ): Response<MessageResponse> {
        val updateRequest = UpdateProfileRequest(
            full_name = fullName,
            email = email,
            password = password
        )

        val role = PreferenceHelper.getUserRole(context)
        return when (role) {
            "user" -> ApiClient.apiService.updateOwnerProfile("Bearer $token", updateRequest)
            else -> ApiClient.apiService.updateOwnerProfile("Bearer $token", updateRequest) // fallback
        }
    }
}