package com.pethealthmonitor.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val user_id: String,
    val role: String,
    val token: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class MessageResponse(
    val message: String
)