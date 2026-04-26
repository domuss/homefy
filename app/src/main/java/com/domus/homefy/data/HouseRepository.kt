package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class HouseRepository(private val supabase: SupabaseClient) {

    suspend fun  CriarCasa(house: House): Result<Unit>{
            return try {
                supabase.postgrest["HOME"].insert(house)
                Result.success(Unit)
            } catch (e: Exception){
                Result.failure(e)
        }
    }
}