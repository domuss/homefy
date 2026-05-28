package com.domus.homefy.data

import retrofit2.http.GET

interface KanyeQuoteApi {
    @GET("/")
    suspend fun getQuote(): KanyeQuoteResponse
}
