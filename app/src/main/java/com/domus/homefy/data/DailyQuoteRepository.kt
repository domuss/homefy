package com.domus.homefy.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DailyQuoteRepository(
    private val api: KanyeQuoteApi,
    private val database: DailyQuoteDatabaseHelper
) {
    suspend fun getTodayQuote(): Result<DailyQuoteEntity> = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val cached = database.getQuoteByDate(today)

        if (cached != null) {
            return@withContext Result.success(cached)
        }

        try {
            val response = api.getQuote()
            val dailyQuote = DailyQuoteEntity(
                date = today,
                quote = response.quote,
                savedAt = System.currentTimeMillis()
            )

            database.replaceQuote(dailyQuote)
            Result.success(dailyQuote)
        } catch (e: Exception) {
            val latest = database.getLatestQuote()
            if (latest != null) {
                Result.success(latest)
            } else {
                Result.failure(e)
            }
        }
    }
}
