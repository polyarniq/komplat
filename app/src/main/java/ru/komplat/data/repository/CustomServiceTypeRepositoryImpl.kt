package ru.komplat.data.repository

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.komplat.data.local.db.DatabaseHelper
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CREATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_NAME
import ru.komplat.data.local.db.DatabaseHelper.Companion.TABLE_CUSTOM_SERVICE_TYPES
import ru.komplat.domain.model.CustomServiceType
import ru.komplat.domain.repository.CustomServiceTypeRepository
import javax.inject.Inject

class CustomServiceTypeRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper
) : CustomServiceTypeRepository {

    override fun getAll(): Flow<List<CustomServiceType>> = flow {
        val types = mutableListOf<CustomServiceType>()
        val cursor = dbHelper.query(TABLE_CUSTOM_SERVICE_TYPES, orderBy = "$COL_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                types.add(cursorToType(it))
            }
        }
        emit(types)
    }.flowOn(Dispatchers.IO)

    override suspend fun getById(id: Long): CustomServiceType? {
        val cursor = dbHelper.query(TABLE_CUSTOM_SERVICE_TYPES, selection = "$COL_ID = ?", selectionArgs = arrayOf(id.toString()))
        cursor.use {
            return if (it.moveToFirst()) cursorToType(it) else null
        }
    }

    override suspend fun insert(type: CustomServiceType): Long {
        val values = ContentValues().apply {
            put(COL_NAME, type.name)
            put(COL_CREATED_AT, type.createdAt)
        }
        return dbHelper.insert(TABLE_CUSTOM_SERVICE_TYPES, values)
    }

    override suspend fun update(type: CustomServiceType) {
        val values = ContentValues().apply {
            put(COL_NAME, type.name)
        }
        dbHelper.update(TABLE_CUSTOM_SERVICE_TYPES, values, "$COL_ID = ?", arrayOf(type.id.toString()))
    }

    override suspend fun deleteById(id: Long) {
        dbHelper.delete(TABLE_CUSTOM_SERVICE_TYPES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    private fun cursorToType(cursor: android.database.Cursor): CustomServiceType {
        return CustomServiceType(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        )
    }
}
