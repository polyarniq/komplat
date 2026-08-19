package ru.komplat.domain.usecase.expense

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetExpensesByPeriodUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(period: String): Flow<List<Expense>> {
        return repository.getExpensesByPeriod(period)
    }
}
