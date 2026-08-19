package ru.komplat.domain.model

enum class CompanyType {
    GAS,
    ELECTRICITY,
    WATER,
    HEATING,
    ELEVATOR,
    GARBAGE,
    MAINTENANCE,
    INTERNET,
    TV,
    OTHER
}

data class UtilityCompany(
    val id: Long = 0,
    val name: String,
    val type: CompanyType,
    val accountNumber: String? = null,
    val description: String? = null,
    val logoUri: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val website: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
