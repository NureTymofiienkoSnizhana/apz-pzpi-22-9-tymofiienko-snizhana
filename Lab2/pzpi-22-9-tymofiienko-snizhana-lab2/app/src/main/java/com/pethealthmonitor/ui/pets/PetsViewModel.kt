package com.pethealthmonitor.ui.pets

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealthmonitor.data.model.Pet
import com.pethealthmonitor.data.repository.PetRepository
import kotlinx.coroutines.launch

class PetsViewModel : ViewModel() {

    private val repository = PetRepository()

    private val _petsResult = MutableLiveData<PetsResult>()
    val petsResult: LiveData<PetsResult> = _petsResult

    fun getOwnerPets(token: String, context: Context) {
        _petsResult.value = PetsResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getOwnerPets(token, context)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _petsResult.value = PetsResult.Success(it)
                    } ?: run {
                        _petsResult.value = PetsResult.Error("Failed to load pets")
                    }
                } else {
                    _petsResult.value = PetsResult.Error("Failed to load pets: ${response.message()}")
                }
            } catch (e: Exception) {
                _petsResult.value = PetsResult.Error("Network error: ${e.message}")
            }
        }
    }

    sealed class PetsResult {
        object Loading : PetsResult()
        data class Success(val pets: List<Pet>) : PetsResult()
        data class Error(val message: String) : PetsResult()
    }
}