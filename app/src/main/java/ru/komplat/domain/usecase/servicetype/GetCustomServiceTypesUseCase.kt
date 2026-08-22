package ru.komplat.domain.usecase.servicetype

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.CustomServiceType
import ru.komplat.domain.repository.CustomServiceTypeRepository
import javax.inject.Inject

class GetCustomServiceTypesUseCase @Inject constructor(
    private val repository: CustomServiceTypeRepository
) {
    operator fun invoke(): Flow<List<CustomServiceType>> {
        return repository.getAll()
    }
}
