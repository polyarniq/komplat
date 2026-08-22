package ru.komplat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.*

interface UtilityCompanyRepository {
    fun getAllCompanies(): Flow<List<UtilityCompany>>
    suspend fun getCompanyById(id: Long): UtilityCompany?
    fun getCompaniesByType(type: CompanyType): Flow<List<UtilityCompany>>
    suspend fun insertCompany(company: UtilityCompany): Long
    suspend fun updateCompany(company: UtilityCompany)
    suspend fun deleteCompany(company: UtilityCompany)
    suspend fun deleteCompanyById(id: Long)
    suspend fun getCompanyCount(): Int
}

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByPeriod(period: String): Flow<List<Expense>>
    fun getExpensesByCompany(companyId: Long): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun getTotalByPeriod(period: String): Double
    fun getExpensesForComparison(period1: String, period2: String): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
    fun getAllPeriods(): Flow<List<String>>
}

interface AttachedFileRepository {
    fun getAllFiles(): Flow<List<AttachedFile>>
    fun getFilesByExpense(expenseId: Long): Flow<List<AttachedFile>>
    fun getFilesByCompany(companyId: Long): Flow<List<AttachedFile>>
    suspend fun getFileById(id: Long): AttachedFile?
    suspend fun insertFile(file: AttachedFile): Long
    suspend fun deleteFile(file: AttachedFile)
    suspend fun deleteFileById(id: Long)
}
