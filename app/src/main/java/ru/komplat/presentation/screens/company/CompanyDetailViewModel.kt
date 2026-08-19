package ru.komplat.presentation.screens.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.*
import ru.komplat.domain.usecase.company.GetCompanyByIdUseCase
import ru.komplat.domain.usecase.company.ManageCompanyUseCase
import ru.komplat.domain.usecase.expense.GetExpensesByPeriodUseCase
import ru.komplat.domain.usecase.file.GetFilesUseCase
import javax.inject.Inject

data class CompanyDetailUiState(
    val company: UtilityCompany? = null,
    val expenses: List<Expense> = emptyList(),
    val files: List<AttachedFile> = emptyList(),
    val name: String = "",
    val type: CompanyType = CompanyType.OTHER,
    val accountNumber: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CompanyDetailViewModel @Inject constructor(
    private val getCompanyById: GetCompanyByIdUseCase,
    private val manageCompany: ManageCompanyUseCase,
    private val getFiles: GetFilesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyDetailUiState())
    val uiState: StateFlow<CompanyDetailUiState> = _uiState.asStateFlow()

    fun loadCompany(companyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val company = getCompanyById(companyId)
                if (company != null) {
                    _uiState.update {
                        it.copy(
                            company = company,
                            name = company.name,
                            type = company.type,
                            accountNumber = company.accountNumber ?: "",
                            description = company.description ?: "",
                            phone = company.contactPhone ?: "",
                            email = company.contactEmail ?: "",
                            website = company.website ?: "",
                            isLoading = false
                        )
                    }
                    loadFiles(companyId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadFiles(companyId: Long) {
        viewModelScope.launch {
            getFiles.byCompany(companyId).collect { files ->
                _uiState.update { it.copy(files = files) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateType(type: CompanyType) {
        _uiState.update { it.copy(type = type) }
    }

    fun updateAccountNumber(accountNumber: String) {
        _uiState.update { it.copy(accountNumber = accountNumber) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updateWebsite(website: String) {
        _uiState.update { it.copy(website = website) }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val company = UtilityCompany(
                    id = state.company?.id ?: 0,
                    name = state.name,
                    type = state.type,
                    accountNumber = state.accountNumber.takeIf { it.isNotBlank() },
                    description = state.description.takeIf { it.isNotBlank() },
                    contactPhone = state.phone.takeIf { it.isNotBlank() },
                    contactEmail = state.email.takeIf { it.isNotBlank() },
                    website = state.website.takeIf { it.isNotBlank() }
                )
                if (state.company == null) {
                    manageCompany.add(company)
                } else {
                    manageCompany.update(company)
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun delete() {
        val companyId = _uiState.value.company?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                manageCompany.delete(companyId)
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
