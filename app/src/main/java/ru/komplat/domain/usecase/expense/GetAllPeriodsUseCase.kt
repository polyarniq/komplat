package ru.komplat.domain.usecase.expense

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class GetAllPeriodsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getAllPeriods()
    }
}
