package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class UserRepository(private val supabase: SupabaseClient) {

    suspend fun createUser(supaId: String, name: String, username: String): Result<Unit> {
        return try {
            val user = User(supa_id = supaId, name = name, username = username)
            supabase.postgrest["users"].insert(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBySupaId(supaId: String): Result<User> {
        return try {
            val user = supabase.postgrest["users"].select {
                filter { eq("supa_id", supaId) }
            }.decodeSingle<User>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(supaId: String, name: String?, username: String?): Result<Unit> {
        return try {
            supabase.postgrest["users"].update({
                name?.let { set("name", it) }
                username?.let { set("username", it) }
            }) {
                filter { eq("supa_id", supaId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}