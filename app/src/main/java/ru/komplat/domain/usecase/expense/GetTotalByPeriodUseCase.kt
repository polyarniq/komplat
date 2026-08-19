package ru.komplat.domain.usecase.expense

import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetTotalByPeriodUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(period: String): Double {
        return repository.getTotalByPeriod(period)
    }
}
