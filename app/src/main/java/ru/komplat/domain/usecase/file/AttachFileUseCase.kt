package ru.komplat.domain.usecase.file

import ru.komplat.domain.model.AttachedFile
import ru.komplat.domain.repository.AttachedFileRepository
import javax.inject.Inject

class AttachFileUseCase @Inject constructor(
    private val repository: AttachedFileRepository
) {
    suspend operator fun invoke(file: AttachedFile): Long {
        return repository.insertFile(file)
    }
}
