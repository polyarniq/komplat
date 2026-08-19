package ru.komplat.domain.usecase.expense

import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteExpenseById(id)
    }
}
