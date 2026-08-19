package ru.komplat.domain.usecase.expense

import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

data class ComparisonResult(
    val period1Total: Double,
    val period2Total: Double,
    val difference: Double,
    val percentageChange: Double,
    val expenses1: List<Expense>,
    val expenses2: List<Expense>
)

class CompareExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(period1: String, period2: String): ComparisonResult {
        val total1 = repository.getTotalByPeriod(period1)
        val total2 = repository.getTotalByPeriod(period2)
        val difference = total2 - total1
        val percentageChange = if (total1 > 0) (difference / total1) * 100 else 0.0

        return ComparisonResult(
            period1Total = total1,
            period2Total = total2,
            difference = difference,
            percentageChange = percentageChange,
            expenses1 = emptyList(),
            expenses2 = emptyList()
        )
    }
}
