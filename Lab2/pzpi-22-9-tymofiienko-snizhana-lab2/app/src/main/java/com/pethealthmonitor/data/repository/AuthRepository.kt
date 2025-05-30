package com.pethealthmonitor.data.repository

import com.pethealthmonitor.api.ApiClient
import com.pethealthmonitor.data.model.AuthResponse
import com.pethealthmonitor.data.model.ForgotPasswordRequest
import com.pethealthmonitor.data.model.LoginRequest
import com.pethealthmonitor.data.model.MessageResponse
import retrofit2.Response

class AuthRepository {
    suspend fun login(email: String, password: String): Response<AuthResponse> {
        return ApiClient.apiService.login(LoginRequest(email, password))
    }

    suspend fun forgotPassword(email: String): Response<MessageResponse> {
        return ApiClient.apiService.forgotPassword(ForgotPasswordRequest(email))
    }

    suspend fun logout(token: String): Response<MessageResponse> {
        return ApiClient.apiService.logout("Bearer $token")
    }
}