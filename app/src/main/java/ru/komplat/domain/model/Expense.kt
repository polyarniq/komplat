package ru.komplat.domain.model

data class Expense(
    val id: Long = 0,
    val companyId: Long,
    val companyName: String = "",
    val companyType: CompanyType = CompanyType.OTHER,
    val amount: Double,
    val period: String, // Format: "YYYY-MM"
    val paymentDate: Long? = null,
    val isPaid: Boolean = false,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
