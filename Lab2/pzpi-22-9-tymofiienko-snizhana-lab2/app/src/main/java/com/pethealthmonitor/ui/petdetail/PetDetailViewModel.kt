package com.pethealthmonitor.ui.petdetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealthmonitor.data.model.HealthSummary
import com.pethealthmonitor.data.model.HealthHistoryResponse
import com.pethealthmonitor.data.model.PetWithHealth
import com.pethealthmonitor.data.repository.PetRepository
import kotlinx.coroutines.launch

class PetDetailViewModel : ViewModel() {

    private val repository = PetRepository()

    private val _petDetailsResult = MutableLiveData<PetDetailsResult>()
    val petDetailsResult: LiveData<PetDetailsResult> = _petDetailsResult

    private val _healthSummaryResult = MutableLiveData<HealthSummaryResult>()
    val healthSummaryResult: LiveData<HealthSummaryResult> = _healthSummaryResult

    private val _healthHistoryResult = MutableLiveData<HealthHistoryResult>()
    val healthHistoryResult: LiveData<HealthHistoryResult> = _healthHistoryResult

    fun getPetDetails(petId: String, token: String, context: Context) {
        _petDetailsResult.value = PetDetailsResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getPetDetails(token, petId, context)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _petDetailsResult.value = PetDetailsResult.Success(it)
                    } ?: run {
                        _petDetailsResult.value = PetDetailsResult.Error("Failed to load pet details")
                    }
                } else {
                    _petDetailsResult.value = PetDetailsResult.Error("Failed to load pet details: ${response.message()}")
                }
            } catch (e: Exception) {
                _petDetailsResult.value = PetDetailsResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun getPetHealthSummary(petId: String, token: String, context: Context) {
        _healthSummaryResult.value = HealthSummaryResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getPetHealthSummary(petId, token, context)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthSummaryResult.value = HealthSummaryResult.Success(it)
                    } ?: run {
                        _healthSummaryResult.value = HealthSummaryResult.Error("Failed to load health summary")
                    }
                } else {
                    _healthSummaryResult.value = HealthSummaryResult.Error("Failed to load health summary: ${response.message()}")
                }
            } catch (e: Exception) {
                _healthSummaryResult.value = HealthSummaryResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun getPetHealthHistory(petId: String, token: String, context: Context) {
        _healthHistoryResult.value = HealthHistoryResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getPetHealthHistory(petId, token, context)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthHistoryResult.value = HealthHistoryResult.Success(it)
                    } ?: run {
                        _healthHistoryResult.value = HealthHistoryResult.Error("Failed to load health history")
                    }
                } else {
                    _healthHistoryResult.value = HealthHistoryResult.Error("Failed to load health history: ${response.message()}")
                }
            } catch (e: Exception) {
                _healthHistoryResult.value = HealthHistoryResult.Error("Network error: ${e.message}")
            }
        }
    }

    sealed class PetDetailsResult {
        object Loading : PetDetailsResult()
        data class Success(val petWithHealth: PetWithHealth) : PetDetailsResult()
        data class Error(val message: String) : PetDetailsResult()
    }

    sealed class HealthSummaryResult {
        object Loading : HealthSummaryResult()
        data class Success(val healthSummary: HealthSummary) : HealthSummaryResult()
        data class Error(val message: String) : HealthSummaryResult()
    }

    sealed class HealthHistoryResult {
        object Loading : HealthHistoryResult()
        data class Success(val healthHistory: HealthHistoryResponse) : HealthHistoryResult()
        data class Error(val message: String) : HealthHistoryResult()
    }
}