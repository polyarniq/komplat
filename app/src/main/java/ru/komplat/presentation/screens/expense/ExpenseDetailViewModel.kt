package ru.komplat.presentation.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.*
import ru.komplat.domain.usecase.company.GetCompaniesUseCase
import ru.komplat.domain.usecase.expense.AddExpenseUseCase
import ru.komplat.domain.usecase.expense.DeleteExpenseUseCase
import ru.komplat.domain.usecase.expense.GetExpensesByPeriodUseCase
import ru.komplat.domain.usecase.file.GetFilesUseCase
import javax.inject.Inject

data class ExpenseDetailUiState(
    val expense: Expense? = null,
    val companies: List<UtilityCompany> = emptyList(),
    val files: List<AttachedFile> = emptyList(),
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
    private val deleteExpense: DeleteExpenseUseCase,
    private val getCompanies: GetCompaniesUseCase,
    private val getFiles: GetFilesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseDetailUiState())
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCompanies()
    }

    private fun loadCompanies() {
        viewModelScope.launch {
            getCompanies().collect { companies ->
                _uiState.update { it.copy(companies = companies) }
            }
        }
    }

    fun loadExpense(expense: Expense) {
        _uiState.update {
            it.copy(
                expense = expense,
                selectedCompanyId = expense.companyId,
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
                    amount = amount,
                    period = period,
                    note = state.note.takeIf { it.isNotBlank() },
                    isPaid = state.isPaid
                )
                addExpense(expense)
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
}
