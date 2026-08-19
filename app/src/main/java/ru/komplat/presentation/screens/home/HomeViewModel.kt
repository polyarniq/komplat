package ru.komplat.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.Expense
import ru.komplat.domain.model.Period
import ru.komplat.domain.usecase.expense.GetExpensesByPeriodUseCase
import ru.komplat.domain.usecase.expense.GetTotalByPeriodUseCase
import javax.inject.Inject

data class HomeUiState(
    val currentPeriod: Period = Period.current(),
    val expenses: List<Expense> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getExpensesByPeriod: GetExpensesByPeriodUseCase,
    private val getTotalByPeriod: GetTotalByPeriodUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val period = _uiState.value.currentPeriod
                getExpensesByPeriod(period.formatted).first().let { expenses ->
                    val total = getTotalByPeriod(period.formatted)
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

    fun setPeriod(period: Period) {
        _uiState.update { it.copy(currentPeriod = period) }
        loadExpenses()
    }

    fun previousMonth() {
        setPeriod(_uiState.value.currentPeriod.previous())
    }

    fun nextMonth() {
        setPeriod(_uiState.value.currentPeriod.next())
    }
}
