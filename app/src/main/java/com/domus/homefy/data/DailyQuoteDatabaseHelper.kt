package com.domus.homefy.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DailyQuoteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "daily_quotes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE daily_quotes (
                date TEXT PRIMARY KEY,
                quote TEXT NOT NULL,
                saved_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS daily_quotes")
        onCreate(db)
    }

    fun getQuoteByDate(date: String): DailyQuoteEntity? {
        readableDatabase.query(
            "daily_quotes",
            arrayOf("date", "quote", "saved_at"),
            "date = ?",
            arrayOf(date),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }

            return DailyQuoteEntity(
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                quote = cursor.getString(cursor.getColumnIndexOrThrow("quote")),
                savedAt = cursor.getLong(cursor.getColumnIndexOrThrow("saved_at"))
            )
        }
    }

    fun getLatestQuote(): DailyQuoteEntity? {
        readableDatabase.query(
            "daily_quotes",
            arrayOf("date", "quote", "saved_at"),
            null,
            null,
            null,
            null,
            "saved_at DESC",
            "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }

            return DailyQuoteEntity(
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                quote = cursor.getString(cursor.getColumnIndexOrThrow("quote")),
                savedAt = cursor.getLong(cursor.getColumnIndexOrThrow("saved_at"))
            )
        }
    }

    fun replaceQuote(quote: DailyQuoteEntity) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("daily_quotes", null, null)
            db.insertOrThrow(
                "daily_quotes",
                null,
                ContentValues().apply {
                    put("date", quote.date)
                    put("quote", quote.quote)
                    put("saved_at", quote.savedAt)
                }
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
