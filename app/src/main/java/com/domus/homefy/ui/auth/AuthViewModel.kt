package com.domus.homefy.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.AuthState
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    object Success : UiState
    data class FormError(val message: String) : UiState
    data class Error(val message: String) : UiState
}

sealed interface AdminState {
    object Loading : AdminState
    data class IsAdmin(val admin: Boolean) : AdminState
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val userRepository: UserRepository,
    private val houseRepository: HouseRepository
) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    val authState = repository.sessionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )
    val _isAdmin = MutableStateFlow<AdminState>(AdminState.Loading)
    val isAdmin = _isAdmin.asStateFlow()

    fun clearUiState() {
        uiState = UiState.Idle
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            uiState = UiState.Loading

            if (email.isEmpty() || password.isEmpty()) {
                uiState = UiState.FormError("E-mail e senha não podem estar vazios")
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

    fun checkAdmin(houseId: Long) {
        resetIsAdmin()

        viewModelScope.launch {
            _isAdmin.value = AdminState.IsAdmin(isHouseAdmin(houseId))
        }
    }

    fun resetIsAdmin() {
        _isAdmin.value = AdminState.Loading
    }

    suspend fun isHouseAdmin(houseId: Long): Boolean {
        val authUserInfo = repository.getCurrentUser() ?: return false
        val userInfo = userRepository.getUserBySupaId(authUserInfo.id).getOrNull() ?: return false
        val userId = userInfo.id ?: return false

        return repository.isHouseAdmin(houseId, userId)
    }
}
