package ru.komplat.presentation.screens.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.UtilityCompany
import ru.komplat.domain.usecase.company.GetCompaniesUseCase
import ru.komplat.domain.usecase.company.ManageCompanyUseCase
import javax.inject.Inject

data class CompanyListUiState(
    val companies: List<UtilityCompany> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val getCompanies: GetCompaniesUseCase,
    private val manageCompany: ManageCompanyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyListUiState())
    val uiState: StateFlow<CompanyListUiState> = _uiState.asStateFlow()

    init {
        loadCompanies()
    }

    private fun loadCompanies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getCompanies().first().let { companies ->
                    _uiState.update {
                        it.copy(companies = companies, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun deleteCompany(id: Long) {
        viewModelScope.launch {
            try {
                manageCompany.delete(id)
                loadCompanies()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
