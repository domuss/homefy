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

    suspend fun getTasksByHouse(houseId: Long): Result<List<Task>> {
        return try {
            val tasks = supabase.postgrest["tasks"].select {
                filter {
                    eq("house_id", houseId)
                }
            }.decodeList<Task>()

            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskCompleted(taskId: Long, isCompleted: Boolean): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].update(
                {
                    set("is_completed", isCompleted)
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(task: Task): Result<Unit> {
        val taskId = task.id ?: return Result.failure(Exception("Tarefa sem id"))

        return try {
            supabase.postgrest["tasks"].update(
                {
                    set("title", task.title)
                    set("description", task.description)
                    set("assignee_id", task.assignee_id)
                    set("is_completed", task.is_completed)
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: Long): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].delete {
                filter {
                    eq("id", taskId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}