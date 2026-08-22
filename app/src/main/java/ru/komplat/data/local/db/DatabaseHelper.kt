package ru.komplat.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "komplat.db"
        private const val DATABASE_VERSION = 4

        const val TABLE_CUSTOM_SERVICE_TYPES = "custom_service_types"

        const val TABLE_COMPANIES = "utility_companies"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_TYPE = "type"
        const val COL_CUSTOM_TYPE = "custom_type"
        const val COL_ACCOUNT_NUMBER = "account_number"
        const val COL_DESCRIPTION = "description"
        const val COL_LOGO_URI = "logo_uri"
        const val COL_PHONE = "contact_phone"
        const val COL_EMAIL = "contact_email"
        const val COL_WEBSITE = "website"
        const val COL_CREATED_AT = "created_at"
        const val COL_UPDATED_AT = "updated_at"

        const val TABLE_EXPENSES = "expenses"
        const val COL_COMPANY_ID = "company_id"
        const val COL_SERVICE_TYPE = "service_type"
        const val COL_AMOUNT = "amount"
        const val COL_PERIOD = "period"
        const val COL_PAYMENT_DATE = "payment_date"
        const val COL_IS_PAID = "is_paid"
        const val COL_NOTE = "note"

        const val TABLE_FILES = "attached_files"
        const val COL_EXPENSE_ID = "expense_id"
        const val COL_FILE_PATH = "file_path"
        const val COL_FILE_NAME = "file_name"
        const val COL_FILE_TYPE = "file_type"
        const val COL_MIME_TYPE = "mime_type"
        const val COL_FILE_SIZE = "file_size"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_CUSTOM_SERVICE_TYPES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL UNIQUE,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_COMPANIES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_TYPE TEXT NOT NULL,
                $COL_CUSTOM_TYPE TEXT,
                $COL_ACCOUNT_NUMBER TEXT,
                $COL_DESCRIPTION TEXT,
                $COL_LOGO_URI TEXT,
                $COL_PHONE TEXT,
                $COL_EMAIL TEXT,
                $COL_WEBSITE TEXT,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_COMPANY_ID INTEGER NOT NULL,
                $COL_SERVICE_TYPE TEXT NOT NULL DEFAULT 'OTHER',
                $COL_AMOUNT REAL NOT NULL,
                $COL_PERIOD TEXT NOT NULL,
                $COL_PAYMENT_DATE INTEGER,
                $COL_IS_PAID INTEGER NOT NULL DEFAULT 0,
                $COL_NOTE TEXT,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COL_COMPANY_ID) REFERENCES $TABLE_COMPANIES($COL_ID) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_FILES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EXPENSE_ID INTEGER,
                $COL_COMPANY_ID INTEGER,
                $COL_FILE_PATH TEXT NOT NULL,
                $COL_FILE_NAME TEXT NOT NULL,
                $COL_FILE_TYPE TEXT NOT NULL,
                $COL_MIME_TYPE TEXT NOT NULL,
                $COL_FILE_SIZE INTEGER NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($COL_EXPENSE_ID) REFERENCES $TABLE_EXPENSES($COL_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_COMPANY_ID) REFERENCES $TABLE_COMPANIES($COL_ID) ON DELETE CASCADE
            )
        """)

        db.execSQL("CREATE INDEX idx_expenses_period ON $TABLE_EXPENSES($COL_PERIOD)")
        db.execSQL("CREATE INDEX idx_expenses_company ON $TABLE_EXPENSES($COL_COMPANY_ID)")
        db.execSQL("CREATE INDEX idx_files_expense ON $TABLE_FILES($COL_EXPENSE_ID)")
        db.execSQL("CREATE INDEX idx_files_company ON $TABLE_FILES($COL_COMPANY_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COL_SERVICE_TYPE TEXT NOT NULL DEFAULT 'OTHER'")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_COMPANIES ADD COLUMN $COL_CUSTOM_TYPE TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE $TABLE_CUSTOM_SERVICE_TYPES (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_NAME TEXT NOT NULL UNIQUE,
                    $COL_CREATED_AT INTEGER NOT NULL
                )
            """)
        }
    }

    fun query(table: String, columns: Array<String>? = null, selection: String? = null,
              selectionArgs: Array<String>? = null, orderBy: String? = null): Cursor {
        return readableDatabase.query(table, columns, selection, selectionArgs, null, null, orderBy)
    }

    fun insert(table: String, values: ContentValues): Long {
        return writableDatabase.insert(table, null, values)
    }

    fun update(table: String, values: ContentValues, whereClause: String?, whereArgs: Array<String>?): Int {
        return writableDatabase.update(table, values, whereClause, whereArgs)
    }

    fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Int {
        return writableDatabase.delete(table, whereClause, whereArgs)
    }

    fun rawQuery(sql: String, selectionArgs: Array<String>? = null): Cursor {
        return readableDatabase.rawQuery(sql, selectionArgs)
    }
}
