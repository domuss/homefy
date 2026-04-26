package com.domus.homefy.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface AuthState {
    object Loading : AuthState
    object Authenticated : AuthState
    object NotAuthenticated : AuthState
}

class AuthRepository(
    private val supabase: SupabaseClient
) {
    val sessionFlow: Flow<AuthState> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.Authenticated
            is SessionStatus.NotAuthenticated -> AuthState.NotAuthenticated
            else -> AuthState.Loading
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        newEmail: String,
        newPassword: String,
        newName: String,
        newUsername: String
    ): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                email = newEmail
                password = newPassword
                data = buildJsonObject {
                    put("name", newName)
                    put("username", newUsername)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    suspend fun updateUser(name: String?, username: String?): Result<Unit> {
        return try {
            supabase.auth.updateUser {
                data = buildJsonObject {
                    name?.let { put("name", it) }
                    username?.let { put("username", it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            supabase.auth.updateUser { email = newEmail }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}