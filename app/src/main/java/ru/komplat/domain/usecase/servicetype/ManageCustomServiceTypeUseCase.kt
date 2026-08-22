package ru.komplat.domain.usecase.servicetype

import ru.komplat.domain.model.CustomServiceType
import ru.komplat.domain.repository.CustomServiceTypeRepository
import javax.inject.Inject

class ManageCustomServiceTypeUseCase @Inject constructor(
    private val repository: CustomServiceTypeRepository
) {
    suspend fun add(name: String): Long {
        return repository.insert(CustomServiceType(name = name))
    }

    suspend fun update(type: CustomServiceType) {
        repository.update(type)
    }

    suspend fun delete(id: Long) {
        repository.deleteById(id)
    }
}
