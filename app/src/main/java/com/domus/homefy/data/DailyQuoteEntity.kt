package com.domus.homefy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_quotes")
data class DailyQuoteEntity(
    @PrimaryKey
    val date: String,
    val quote: String,
    @ColumnInfo(name = "saved_at")
    val savedAt: Long
)
