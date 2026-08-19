package ru.komplat.domain.usecase.file

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.AttachedFile
import ru.komplat.domain.repository.AttachedFileRepository
import javax.inject.Inject

class GetFilesUseCase @Inject constructor(
    private val repository: AttachedFileRepository
) {
    fun byExpense(expenseId: Long): Flow<List<AttachedFile>> {
        return repository.getFilesByExpense(expenseId)
    }

    fun byCompany(companyId: Long): Flow<List<AttachedFile>> {
        return repository.getFilesByCompany(companyId)
    }
}
