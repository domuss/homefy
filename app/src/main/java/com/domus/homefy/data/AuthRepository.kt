package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface AuthState {
    object Loading : AuthState
    object Authenticated : AuthState
    object NotAuthenticated : AuthState
}

class AuthRepository(
    private val supabase: SupabaseClient,
    private val userRepository: UserRepository
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
            // signUpWith já retorna o UserInfo, funciona mesmo com confirmação de email ativa
            val userInfo = supabase.auth.signUpWith(Email) {
                email = newEmail
                password = newPassword
            }

            val supaId = userInfo?.id
                ?: return Result.failure(Exception("Erro ao obter usuário após cadastro"))

            val publicUserResult = userRepository.createUser(
                supaId = supaId.toString(),
                name = newName,
                username = newUsername
            )

            if (publicUserResult.isFailure) {
                return Result.failure(
                    publicUserResult.exceptionOrNull()
                        ?: Exception("Erro ao criar usuário na base de dados pública")
                )
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

    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            supabase.auth.updateUser { email = newEmail }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}