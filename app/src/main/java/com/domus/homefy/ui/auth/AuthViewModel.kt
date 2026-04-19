package com.domus.homefy.ui.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    object Success : UiState
    data class Error(val message: String) : UiState
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    val authState = repository.sessionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            uiState = UiState.Loading

            val result = repository.login(email, password)

            result.fold(
                onSuccess = {
                    uiState = UiState.Success
                },
                onFailure = { e ->
                    var message = "Error trying to login"

                    if (e.message != null) {
                        message = e.message!!.split("\n")[1]
                    }

                    uiState = UiState.Error(message)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            uiState = UiState.Loading

            val result = runCatching {
                repository.logout()
            }

            uiState = when {
                result.isSuccess -> UiState.Success
                result.isFailure -> UiState.Success

                else -> UiState.Idle
            }
        }
    }
}