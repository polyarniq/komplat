package ru.komplat.data.repository

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.komplat.data.local.db.DatabaseHelper
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_ACCOUNT_NUMBER
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CREATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CUSTOM_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_DESCRIPTION
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_EMAIL
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_LOGO_URI
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_NAME
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_PHONE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_UPDATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_WEBSITE
import ru.komplat.data.local.db.DatabaseHelper.Companion.TABLE_COMPANIES
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.UtilityCompany
import ru.komplat.domain.repository.UtilityCompanyRepository
import javax.inject.Inject

class UtilityCompanyRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper
) : UtilityCompanyRepository {

    override fun getAllCompanies(): Flow<List<UtilityCompany>> = flow {
        val companies = mutableListOf<UtilityCompany>()
        val cursor = dbHelper.query(TABLE_COMPANIES, orderBy = COL_NAME)
        cursor.use {
            while (it.moveToNext()) {
                companies.add(cursorToCompany(it))
            }
        }
        emit(companies)
    }.flowOn(Dispatchers.IO)

    override suspend fun getCompanyById(id: Long): UtilityCompany? {
        val cursor = dbHelper.query(TABLE_COMPANIES, selection = "$COL_ID = ?", selectionArgs = arrayOf(id.toString()))
        cursor.use {
            return if (it.moveToFirst()) cursorToCompany(it) else null
        }
    }

    override fun getCompaniesByType(type: CompanyType): Flow<List<UtilityCompany>> = flow {
        val companies = mutableListOf<UtilityCompany>()
        val cursor = dbHelper.query(TABLE_COMPANIES, selection = "$COL_TYPE = ?", selectionArgs = arrayOf(type.name), orderBy = COL_NAME)
        cursor.use {
            while (it.moveToNext()) {
                companies.add(cursorToCompany(it))
            }
        }
        emit(companies)
    }.flowOn(Dispatchers.IO)

    override suspend fun insertCompany(company: UtilityCompany): Long {
        val values = companyToContentValues(company)
        return dbHelper.insert(TABLE_COMPANIES, values)
    }

    override suspend fun updateCompany(company: UtilityCompany) {
        val values = companyToContentValues(company)
        dbHelper.update(TABLE_COMPANIES, values, "$COL_ID = ?", arrayOf(company.id.toString()))
    }

    override suspend fun deleteCompany(company: UtilityCompany) {
        dbHelper.delete(TABLE_COMPANIES, "$COL_ID = ?", arrayOf(company.id.toString()))
    }

    override suspend fun deleteCompanyById(id: Long) {
        dbHelper.delete(TABLE_COMPANIES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    override suspend fun getCompanyCount(): Int {
        val cursor = dbHelper.rawQuery("SELECT COUNT(*) FROM $TABLE_COMPANIES")
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    private fun cursorToCompany(cursor: android.database.Cursor): UtilityCompany {
        return UtilityCompany(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            type = CompanyType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))),
            customType = cursor.getString(cursor.getColumnIndexOrThrow(COL_CUSTOM_TYPE)),
            accountNumber = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCOUNT_NUMBER)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
            logoUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOGO_URI)),
            contactPhone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
            contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            website = cursor.getString(cursor.getColumnIndexOrThrow(COL_WEBSITE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
        )
    }

    private fun companyToContentValues(company: UtilityCompany): ContentValues {
        return ContentValues().apply {
            put(COL_NAME, company.name)
            put(COL_TYPE, company.type.name)
            put(COL_CUSTOM_TYPE, company.customType)
            put(COL_ACCOUNT_NUMBER, company.accountNumber)
            put(COL_DESCRIPTION, company.description)
            put(COL_LOGO_URI, company.logoUri)
            put(COL_PHONE, company.contactPhone)
            put(COL_EMAIL, company.contactEmail)
            put(COL_WEBSITE, company.website)
            put(COL_CREATED_AT, company.createdAt)
            put(COL_UPDATED_AT, company.updatedAt)
        }
    }
}
