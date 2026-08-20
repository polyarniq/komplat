package ru.komplat.presentation.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.*
import ru.komplat.domain.repository.ExpenseRepository
import ru.komplat.domain.repository.UtilityCompanyRepository
import ru.komplat.domain.usecase.expense.AddExpenseUseCase
import ru.komplat.domain.usecase.expense.DeleteExpenseUseCase
import ru.komplat.domain.usecase.expense.UpdateExpenseUseCase
import ru.komplat.domain.usecase.file.AttachFileUseCase
import ru.komplat.domain.usecase.file.DeleteFileUseCase
import ru.komplat.domain.usecase.file.GetFilesUseCase
import javax.inject.Inject

data class ExpenseDetailUiState(
    val expense: Expense? = null,
    val allCompanies: List<UtilityCompany> = emptyList(),
    val filteredCompanies: List<UtilityCompany> = emptyList(),
    val files: List<AttachedFile> = emptyList(),
    val selectedServiceType: CompanyType = CompanyType.OTHER,
    val selectedCompanyId: Long? = null,
    val amount: String = "",
    val period: String = "",
    val note: String = "",
    val isPaid: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    private val addExpense: AddExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val expenseRepository: ExpenseRepository,
    private val companyRepository: UtilityCompanyRepository,
    private val getFiles: GetFilesUseCase,
    private val attachFile: AttachFileUseCase,
    private val deleteFile: DeleteFileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseDetailUiState())
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCompanies()
    }

    private fun loadCompanies() {
        viewModelScope.launch {
            companyRepository.getAllCompanies().collect { companies ->
                _uiState.update {
                    it.copy(
                        allCompanies = companies,
                        filteredCompanies = companies.filter { c -> c.type == it.selectedServiceType }
                    )
                }
            }
        }
    }

    fun updateServiceType(type: CompanyType) {
        _uiState.update {
            it.copy(
                selectedServiceType = type,
                selectedCompanyId = null,
                filteredCompanies = it.allCompanies.filter { c -> c.type == type }
            )
        }
    }

    fun loadExpenseById(expenseId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null) {
                    loadExpense(expense)
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadExpense(expense: Expense) {
        _uiState.update {
            it.copy(
                expense = expense,
                selectedServiceType = expense.serviceType,
                selectedCompanyId = expense.companyId,
                filteredCompanies = it.allCompanies.filter { c -> c.type == expense.serviceType },
                amount = expense.amount.toString(),
                period = expense.period,
                note = expense.note ?: "",
                isPaid = expense.isPaid
            )
        }
        loadFiles(expense.id)
    }

    private fun loadFiles(expenseId: Long) {
        viewModelScope.launch {
            getFiles.byExpense(expenseId).collect { files ->
                _uiState.update { it.copy(files = files) }
            }
        }
    }

    fun updateCompanyId(id: Long) {
        _uiState.update { it.copy(selectedCompanyId = id) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updatePeriod(period: String) {
        _uiState.update { it.copy(period = period) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateIsPaid(isPaid: Boolean) {
        _uiState.update { it.copy(isPaid = isPaid) }
    }

    fun save() {
        val state = _uiState.value
        val companyId = state.selectedCompanyId ?: return
        val amount = state.amount.toDoubleOrNull() ?: return
        val period = state.period

        if (period.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val expense = Expense(
                    id = state.expense?.id ?: 0,
                    companyId = companyId,
                    serviceType = state.selectedServiceType,
                    amount = amount,
                    period = period,
                    note = state.note.takeIf { it.isNotBlank() },
                    isPaid = state.isPaid
                )
                if (state.expense == null) {
                    addExpense(expense)
                } else {
                    updateExpense(expense)
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
        val expenseId = _uiState.value.expense?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                deleteExpense(expenseId)
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun attachFile(filePath: String, fileName: String, mimeType: String, fileSize: Long) {
        val expenseId = _uiState.value.expense?.id ?: return
        viewModelScope.launch {
            try {
                val file = AttachedFile(
                    expenseId = expenseId,
                    filePath = filePath,
                    fileName = fileName,
                    fileType = if (mimeType.startsWith("image/")) FileType.RECEIPT else FileType.OTHER,
                    mimeType = mimeType,
                    fileSize = fileSize
                )
                attachFile(file)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteAttachedFile(fileId: Long) {
        viewModelScope.launch {
            try {
                deleteFile(fileId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
