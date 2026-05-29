package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class TaskRepository(private val supabase: SupabaseClient) {

    suspend fun createTask(task: Task): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].insert(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}