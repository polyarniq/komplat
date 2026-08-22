package ru.komplat.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.CustomServiceType
import ru.komplat.domain.usecase.servicetype.GetCustomServiceTypesUseCase
import ru.komplat.domain.usecase.servicetype.ManageCustomServiceTypeUseCase
import javax.inject.Inject

data class ServiceTypesUiState(
    val types: List<CustomServiceType> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ServiceTypesViewModel @Inject constructor(
    private val getCustomServiceTypes: GetCustomServiceTypesUseCase,
    private val manageCustomServiceType: ManageCustomServiceTypeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceTypesUiState())
    val uiState: StateFlow<ServiceTypesUiState> = _uiState.asStateFlow()

    init {
        loadTypes()
    }

    private fun loadTypes() {
        viewModelScope.launch {
            getCustomServiceTypes().collect { types ->
                _uiState.update { it.copy(types = types, isLoading = false) }
            }
        }
    }

    fun addType(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                manageCustomServiceType.add(name.trim())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка: ${e.message}") }
            }
        }
    }

    fun updateType(type: CustomServiceType, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            try {
                manageCustomServiceType.update(type.copy(name = newName.trim()))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка: ${e.message}") }
            }
        }
    }

    fun deleteType(id: Long) {
        viewModelScope.launch {
            try {
                manageCustomServiceType.delete(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
