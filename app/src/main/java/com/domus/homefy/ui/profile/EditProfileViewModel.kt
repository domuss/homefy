package com.domus.homefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.User
import com.domus.homefy.data.UserRepository
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    val currentAuthUser = authRepository.getCurrentUser()

    var currentPublicUser by mutableStateOf<User?>(null)
        private set

    init {
        loadPublicUser()
    }

    private fun loadPublicUser() {
        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser() ?: return@launch
            val result = userRepository.getUserBySupaId(authUser.id)
            currentPublicUser = result.getOrNull()
        }
    }

    fun updateUser(name: String, username: String, email: String) {
        viewModelScope.launch {
            uiState = UiState.Loading

            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                uiState = UiState.Error("Usuário não autenticado")
                return@launch
            }

            val profileResult = userRepository.updateUser(
                supaId = authUser.id,
                name = name.ifBlank { null },
                username = username.ifBlank { null }
            )

            if (profileResult.isSuccess && email.isNotBlank()) {
                authRepository.updateEmail(email)
            }

            uiState = profileResult.fold(
                onSuccess = { UiState.Success },
                onFailure = { UiState.Error(it.message ?: "Erro ao atualizar") }
            )
        }
    }
}