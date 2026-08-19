package ru.komplat.presentation.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.Expense
import ru.komplat.domain.usecase.expense.GetExpensesByPeriodUseCase
import ru.komplat.domain.usecase.expense.GetTotalByPeriodUseCase
import javax.inject.Inject

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesByPeriod: GetExpensesByPeriodUseCase,
    private val getTotalByPeriod: GetTotalByPeriodUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    fun loadExpenses(period: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getExpensesByPeriod(period).first().let { expenses ->
                    val total = getTotalByPeriod(period)
                    _uiState.update {
                        it.copy(
                            expenses = expenses,
                            totalAmount = total,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
