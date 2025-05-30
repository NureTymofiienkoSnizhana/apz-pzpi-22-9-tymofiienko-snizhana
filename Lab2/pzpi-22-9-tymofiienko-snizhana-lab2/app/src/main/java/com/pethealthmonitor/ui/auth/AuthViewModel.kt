package com.pethealthmonitor.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealthmonitor.data.model.AuthResponse
import com.pethealthmonitor.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _forgotPasswordResult = MutableLiveData<ForgotPasswordResult>()
    val forgotPasswordResult: LiveData<ForgotPasswordResult> = _forgotPasswordResult

    private val _logoutResult = MutableLiveData<LogoutResult>()
    val logoutResult: LiveData<LogoutResult> = _logoutResult

    fun login(email: String, password: String) {
        _loginResult.value = AuthResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _loginResult.value = AuthResult.Success(it)
                    } ?: run {
                        _loginResult.value = AuthResult.Error("Unknown error occurred")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid email or password"
                        else -> "Login failed: ${response.message()}"
                    }
                    _loginResult.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginResult.value = AuthResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun forgotPassword(email: String) {
        _forgotPasswordResult.value = ForgotPasswordResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _forgotPasswordResult.value = ForgotPasswordResult.Success(it.message)
                    } ?: run {
                        _forgotPasswordResult.value = ForgotPasswordResult.Error("Unknown error occurred")
                    }
                } else {
                    _forgotPasswordResult.value = ForgotPasswordResult.Error("Failed to reset password: ${response.message()}")
                }
            } catch (e: Exception) {
                _forgotPasswordResult.value = ForgotPasswordResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun logout(token: String) {
        _logoutResult.value = LogoutResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.logout(token)
                if (response.isSuccessful) {
                    _logoutResult.value = LogoutResult.Success
                } else {
                    _logoutResult.value = LogoutResult.Error("Logout failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _logoutResult.value = LogoutResult.Error("Network error: ${e.message}")
            }
        }
    }

    sealed class AuthResult {
        object Loading : AuthResult()
        data class Success(val authResponse: AuthResponse) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    sealed class ForgotPasswordResult {
        object Loading : ForgotPasswordResult()
        data class Success(val message: String) : ForgotPasswordResult()
        data class Error(val message: String) : ForgotPasswordResult()
    }

    sealed class LogoutResult {
        object Loading : LogoutResult()
        object Success : LogoutResult()
        data class Error(val message: String) : LogoutResult()
    }
}