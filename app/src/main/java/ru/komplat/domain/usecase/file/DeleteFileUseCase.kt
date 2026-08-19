package ru.komplat.domain.usecase.file

import ru.komplat.domain.repository.AttachedFileRepository
import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(
    private val repository: AttachedFileRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteFileById(id)
    }
}
