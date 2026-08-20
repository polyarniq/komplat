package ru.komplat.data.repository

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.komplat.data.local.db.DatabaseHelper
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_AMOUNT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_COMPANY_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CREATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CUSTOM_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_IS_PAID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_NAME
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_NOTE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_PAYMENT_DATE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_PERIOD
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_SERVICE_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_UPDATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.TABLE_COMPANIES
import ru.komplat.data.local.db.DatabaseHelper.Companion.TABLE_EXPENSES
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.ExpenseRepository
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> = flow {
        val expenses = mutableListOf<Expense>()
        val sql = """
            SELECT e.*, c.$COL_NAME as company_name, c.$COL_TYPE as company_type, c.$COL_CUSTOM_TYPE as company_custom_type
            FROM $TABLE_EXPENSES e
            INNER JOIN $TABLE_COMPANIES c ON e.$COL_COMPANY_ID = c.$COL_ID
            ORDER BY e.$COL_PERIOD DESC, c.$COL_NAME ASC
        """
        val cursor = dbHelper.rawQuery(sql)
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(cursorToExpense(it))
            }
        }
        emit(expenses)
    }.flowOn(Dispatchers.IO)

    override fun getExpensesByPeriod(period: String): Flow<List<Expense>> = flow {
        val expenses = mutableListOf<Expense>()
        val sql = """
            SELECT e.*, c.$COL_NAME as company_name, c.$COL_TYPE as company_type, c.$COL_CUSTOM_TYPE as company_custom_type
            FROM $TABLE_EXPENSES e
            INNER JOIN $TABLE_COMPANIES c ON e.$COL_COMPANY_ID = c.$COL_ID
            WHERE e.$COL_PERIOD = ?
            ORDER BY c.$COL_NAME ASC
        """
        val cursor = dbHelper.rawQuery(sql, arrayOf(period))
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(cursorToExpense(it))
            }
        }
        emit(expenses)
    }.flowOn(Dispatchers.IO)

    override fun getExpensesByCompany(companyId: Long): Flow<List<Expense>> = flow {
        val expenses = mutableListOf<Expense>()
        val sql = """
            SELECT e.*, c.$COL_NAME as company_name, c.$COL_TYPE as company_type, c.$COL_CUSTOM_TYPE as company_custom_type
            FROM $TABLE_EXPENSES e
            INNER JOIN $TABLE_COMPANIES c ON e.$COL_COMPANY_ID = c.$COL_ID
            WHERE e.$COL_COMPANY_ID = ?
            ORDER BY e.$COL_PERIOD DESC
        """
        val cursor = dbHelper.rawQuery(sql, arrayOf(companyId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(cursorToExpense(it))
            }
        }
        emit(expenses)
    }.flowOn(Dispatchers.IO)

    override suspend fun getExpenseById(id: Long): Expense? {
        val sql = """
            SELECT e.*, c.$COL_NAME as company_name, c.$COL_TYPE as company_type, c.$COL_CUSTOM_TYPE as company_custom_type
            FROM $TABLE_EXPENSES e
            INNER JOIN $TABLE_COMPANIES c ON e.$COL_COMPANY_ID = c.$COL_ID
            WHERE e.$COL_ID = ?
        """
        val cursor = dbHelper.rawQuery(sql, arrayOf(id.toString()))
        cursor.use {
            return if (it.moveToFirst()) cursorToExpense(it) else null
        }
    }

    override suspend fun getTotalByPeriod(period: String): Double {
        val cursor = dbHelper.rawQuery("SELECT SUM($COL_AMOUNT) FROM $TABLE_EXPENSES WHERE $COL_PERIOD = ?", arrayOf(period))
        cursor.use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    override fun getExpensesForComparison(period1: String, period2: String): Flow<List<Expense>> = flow {
        val expenses = mutableListOf<Expense>()
        val sql = """
            SELECT e.*, c.$COL_NAME as company_name, c.$COL_TYPE as company_type, c.$COL_CUSTOM_TYPE as company_custom_type
            FROM $TABLE_EXPENSES e
            INNER JOIN $TABLE_COMPANIES c ON e.$COL_COMPANY_ID = c.$COL_ID
            WHERE e.$COL_PERIOD IN (?, ?)
            ORDER BY c.$COL_NAME ASC, e.$COL_PERIOD ASC
        """
        val cursor = dbHelper.rawQuery(sql, arrayOf(period1, period2))
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(cursorToExpense(it))
            }
        }
        emit(expenses)
    }.flowOn(Dispatchers.IO)

    override suspend fun insertExpense(expense: Expense): Long {
        val values = expenseToContentValues(expense)
        return dbHelper.insert(TABLE_EXPENSES, values)
    }

    override suspend fun updateExpense(expense: Expense) {
        val values = expenseToContentValues(expense)
        dbHelper.update(TABLE_EXPENSES, values, "$COL_ID = ?", arrayOf(expense.id.toString()))
    }

    override suspend fun deleteExpense(expense: Expense) {
        dbHelper.delete(TABLE_EXPENSES, "$COL_ID = ?", arrayOf(expense.id.toString()))
    }

    override suspend fun deleteExpenseById(id: Long) {
        dbHelper.delete(TABLE_EXPENSES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    override fun getAllPeriods(): Flow<List<String>> = flow {
        val periods = mutableListOf<String>()
        val cursor = dbHelper.rawQuery("SELECT DISTINCT $COL_PERIOD FROM $TABLE_EXPENSES ORDER BY $COL_PERIOD DESC")
        cursor.use {
            while (it.moveToNext()) {
                periods.add(it.getString(0))
            }
        }
        emit(periods)
    }.flowOn(Dispatchers.IO)

    private fun cursorToExpense(cursor: android.database.Cursor): Expense {
        return Expense(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            companyId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPANY_ID)),
            companyName = cursor.getString(cursor.getColumnIndexOrThrow("company_name")),
            companyType = CompanyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("company_type"))),
            companyCustomType = cursor.getString(cursor.getColumnIndexOrThrow("company_custom_type")),
            serviceType = CompanyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_SERVICE_TYPE))),
            amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
            period = cursor.getString(cursor.getColumnIndexOrThrow(COL_PERIOD)),
            paymentDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_PAYMENT_DATE))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COL_PAYMENT_DATE)),
            isPaid = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PAID)) == 1,
            note = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
        )
    }

    private fun expenseToContentValues(expense: Expense): ContentValues {
        return ContentValues().apply {
            put(COL_COMPANY_ID, expense.companyId)
            put(COL_SERVICE_TYPE, expense.serviceType.name)
            put(COL_AMOUNT, expense.amount)
            put(COL_PERIOD, expense.period)
            put(COL_PAYMENT_DATE, expense.paymentDate)
            put(COL_IS_PAID, if (expense.isPaid) 1 else 0)
            put(COL_NOTE, expense.note)
            put(COL_CREATED_AT, expense.createdAt)
            put(COL_UPDATED_AT, expense.updatedAt)
        }
    }
}
