package com.domus.homefy.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.AuthState
import kotlinx.coroutines.flow.SharingStarted
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

            if (email.isEmpty() || password.isEmpty()) {
                uiState = UiState.Error("E-mail e senha não podem estar vazios")
                return@launch
            }

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