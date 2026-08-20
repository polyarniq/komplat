package ru.komplat.domain.usecase.expense

import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        repository.updateExpense(expense)
    }
}
