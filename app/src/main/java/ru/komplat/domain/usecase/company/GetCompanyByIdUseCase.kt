package ru.komplat.domain.usecase.company

import ru.komplat.domain.model.UtilityCompany
import ru.komplat.domain.repository.UtilityCompanyRepository
import javax.inject.Inject

class GetCompanyByIdUseCase @Inject constructor(
    private val repository: UtilityCompanyRepository
) {
    suspend operator fun invoke(id: Long): UtilityCompany? {
        return repository.getCompanyById(id)
    }
}
