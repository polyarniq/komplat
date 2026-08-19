package ru.komplat.domain.usecase.company

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.UtilityCompany
import ru.komplat.domain.repository.UtilityCompanyRepository
import javax.inject.Inject

class GetCompaniesUseCase @Inject constructor(
    private val repository: UtilityCompanyRepository
) {
    operator fun invoke(): Flow<List<UtilityCompany>> {
        return repository.getAllCompanies()
    }
}
