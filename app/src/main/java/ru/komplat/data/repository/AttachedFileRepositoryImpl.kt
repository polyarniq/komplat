package ru.komplat.data.repository

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.komplat.data.local.db.DatabaseHelper
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_COMPANY_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_CREATED_AT
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_EXPENSE_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_FILE_NAME
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_FILE_PATH
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_FILE_SIZE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_FILE_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_ID
import ru.komplat.data.local.db.DatabaseHelper.Companion.COL_MIME_TYPE
import ru.komplat.data.local.db.DatabaseHelper.Companion.TABLE_FILES
import ru.komplat.domain.model.AttachedFile
import ru.komplat.domain.model.FileType
import ru.komplat.domain.repository.AttachedFileRepository
import javax.inject.Inject

class AttachedFileRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper
) : AttachedFileRepository {

    override fun getAllFiles(): Flow<List<AttachedFile>> = flow {
        val files = mutableListOf<AttachedFile>()
        val cursor = dbHelper.query(TABLE_FILES, orderBy = "$COL_CREATED_AT DESC")
        cursor.use {
            while (it.moveToNext()) {
                files.add(cursorToFile(it))
            }
        }
        emit(files)
    }.flowOn(Dispatchers.IO)

    override fun getFilesByExpense(expenseId: Long): Flow<List<AttachedFile>> = flow {
        val files = mutableListOf<AttachedFile>()
        val cursor = dbHelper.query(TABLE_FILES, selection = "$COL_EXPENSE_ID = ?", selectionArgs = arrayOf(expenseId.toString()), orderBy = "$COL_CREATED_AT DESC")
        cursor.use {
            while (it.moveToNext()) {
                files.add(cursorToFile(it))
            }
        }
        emit(files)
    }.flowOn(Dispatchers.IO)

    override fun getFilesByCompany(companyId: Long): Flow<List<AttachedFile>> = flow {
        val files = mutableListOf<AttachedFile>()
        val cursor = dbHelper.query(TABLE_FILES, selection = "$COL_COMPANY_ID = ?", selectionArgs = arrayOf(companyId.toString()), orderBy = "$COL_CREATED_AT DESC")
        cursor.use {
            while (it.moveToNext()) {
                files.add(cursorToFile(it))
            }
        }
        emit(files)
    }.flowOn(Dispatchers.IO)

    override suspend fun getFileById(id: Long): AttachedFile? {
        val cursor = dbHelper.query(TABLE_FILES, selection = "$COL_ID = ?", selectionArgs = arrayOf(id.toString()))
        cursor.use {
            return if (it.moveToFirst()) cursorToFile(it) else null
        }
    }

    override suspend fun insertFile(file: AttachedFile): Long {
        val values = fileToContentValues(file)
        return dbHelper.insert(TABLE_FILES, values)
    }

    override suspend fun deleteFile(file: AttachedFile) {
        dbHelper.delete(TABLE_FILES, "$COL_ID = ?", arrayOf(file.id.toString()))
    }

    override suspend fun deleteFileById(id: Long) {
        dbHelper.delete(TABLE_FILES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    private fun cursorToFile(cursor: android.database.Cursor): AttachedFile {
        return AttachedFile(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            expenseId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_EXPENSE_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXPENSE_ID)),
            companyId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_COMPANY_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPANY_ID)),
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH)),
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)),
            fileType = FileType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE))),
            mimeType = cursor.getString(cursor.getColumnIndexOrThrow(COL_MIME_TYPE)),
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FILE_SIZE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        )
    }

    private fun fileToContentValues(file: AttachedFile): ContentValues {
        return ContentValues().apply {
            put(COL_EXPENSE_ID, file.expenseId)
            put(COL_COMPANY_ID, file.companyId)
            put(COL_FILE_PATH, file.filePath)
            put(COL_FILE_NAME, file.fileName)
            put(COL_FILE_TYPE, file.fileType.name)
            put(COL_MIME_TYPE, file.mimeType)
            put(COL_FILE_SIZE, file.fileSize)
            put(COL_CREATED_AT, file.createdAt)
        }
    }
}
