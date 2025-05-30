package com.pethealthmonitor.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealthmonitor.data.model.UserResponse
import com.pethealthmonitor.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _profileResult = MutableLiveData<ProfileResult>()
    val profileResult: LiveData<ProfileResult> = _profileResult

    private val _updateProfileResult = MutableLiveData<UpdateProfileResult>()
    val updateProfileResult: LiveData<UpdateProfileResult> = _updateProfileResult

    fun getUserProfile(token: String, context: Context) {
        _profileResult.value = ProfileResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getUserProfile(token, context)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        // Сохраняем данные пользователя в SharedPreferences для будущего использования
                        com.pethealthmonitor.util.PreferenceHelper.saveUserData(
                            context = context,
                            token = token,
                            role = user.role,
                            userId = user._id,
                            email = user.email,
                            name = user.full_name
                        )
                        _profileResult.value = ProfileResult.Success(user)
                    } ?: run {
                        _profileResult.value = ProfileResult.Error("Failed to load user profile")
                    }
                } else {
                    _profileResult.value = ProfileResult.Error("Failed to load user profile: ${response.message()}")
                }
            } catch (e: Exception) {
                _profileResult.value = ProfileResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun updateUserProfile(
        token: String,
        context: Context,
        fullName: String?,
        email: String?,
        password: String?
    ) {
        _updateProfileResult.value = UpdateProfileResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.updateUserProfile(token, context, fullName, email, password)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _updateProfileResult.value = UpdateProfileResult.Success(it.message)

                        // Refresh user profile data
                        getUserProfile(token, context)
                    } ?: run {
                        _updateProfileResult.value = UpdateProfileResult.Error("Failed to update profile")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        409 -> "Email already in use"
                        else -> "Failed to update profile: ${response.message()}"
                    }
                    _updateProfileResult.value = UpdateProfileResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _updateProfileResult.value = UpdateProfileResult.Error("Network error: ${e.message}")
            }
        }
    }

    sealed class ProfileResult {
        object Loading : ProfileResult()
        data class Success(val user: UserResponse) : ProfileResult()
        data class Error(val message: String) : ProfileResult()
    }

    sealed class UpdateProfileResult {
        object Loading : UpdateProfileResult()
        data class Success(val message: String) : UpdateProfileResult()
        data class Error(val message: String) : UpdateProfileResult()
    }
}