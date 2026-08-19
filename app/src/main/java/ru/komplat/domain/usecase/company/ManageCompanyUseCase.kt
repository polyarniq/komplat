package ru.komplat.domain.usecase.company

import ru.komplat.domain.model.UtilityCompany
import ru.komplat.domain.repository.UtilityCompanyRepository
import javax.inject.Inject

class ManageCompanyUseCase @Inject constructor(
    private val repository: UtilityCompanyRepository
) {
    suspend fun add(company: UtilityCompany): Long {
        return repository.insertCompany(company)
    }

    suspend fun update(company: UtilityCompany) {
        repository.updateCompany(company)
    }

    suspend fun delete(id: Long) {
        repository.deleteCompanyById(id)
    }
}
