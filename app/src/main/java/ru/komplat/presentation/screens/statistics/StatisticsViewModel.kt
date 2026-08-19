package ru.komplat.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.Expense
import ru.komplat.domain.usecase.expense.CompareExpensesUseCase
import ru.komplat.domain.usecase.expense.GetAllPeriodsUseCase
import ru.komplat.domain.usecase.expense.GetExpensesByPeriodUseCase
import ru.komplat.domain.usecase.expense.GetTotalByPeriodUseCase
import javax.inject.Inject

data class StatisticsUiState(
    val periods: List<String> = emptyList(),
    val period1: String = "",
    val period2: String = "",
    val period1Expenses: List<Expense> = emptyList(),
    val period2Expenses: List<Expense> = emptyList(),
    val period1Total: Double = 0.0,
    val period2Total: Double = 0.0,
    val difference: Double = 0.0,
    val percentageChange: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getAllPeriods: GetAllPeriodsUseCase,
    private val getExpensesByPeriod: GetExpensesByPeriodUseCase,
    private val getTotalByPeriod: GetTotalByPeriodUseCase,
    private val compareExpenses: CompareExpensesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadPeriods()
    }

    private fun loadPeriods() {
        viewModelScope.launch {
            getAllPeriods().first().let { periods ->
                _uiState.update {
                    it.copy(
                        periods = periods,
                        period1 = periods.getOrElse(0) { "" },
                        period2 = periods.getOrElse(1) { "" }
                    )
                }
                if (periods.size >= 2) {
                    comparePeriods()
                }
            }
        }
    }

    fun updatePeriod1(period: String) {
        _uiState.update { it.copy(period1 = period) }
        comparePeriods()
    }

    fun updatePeriod2(period: String) {
        _uiState.update { it.copy(period2 = period) }
        comparePeriods()
    }

    private fun comparePeriods() {
        val state = _uiState.value
        if (state.period1.isBlank() || state.period2.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = compareExpenses(state.period1, state.period2)
                _uiState.update {
                    it.copy(
                        period1Total = result.period1Total,
                        period2Total = result.period2Total,
                        difference = result.difference,
                        percentageChange = result.percentageChange,
                        isLoading = false
                    )
                }

                getExpensesByPeriod(state.period1).first().let { expenses ->
                    _uiState.update { it.copy(period1Expenses = expenses) }
                }
                getExpensesByPeriod(state.period2).first().let { expenses ->
                    _uiState.update { it.copy(period2Expenses = expenses) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
