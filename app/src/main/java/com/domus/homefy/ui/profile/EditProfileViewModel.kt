package com.domus.homefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch

class EditProfileViewModel(private val repository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    val currentUser = repository.getCurrentUser()

    fun updateUser(name: String, username: String, email: String) {
        viewModelScope.launch {
            uiState = UiState.Loading

            val result = repository.updateUser(
                name = name.ifBlank { null },
                username = username.ifBlank { null }
            )

            if (result.isSuccess && email.isNotBlank()) {
                repository.updateEmail(email)
            }

            uiState = result.fold(
                onSuccess = { UiState.Success },
                onFailure = { UiState.Error(it.message ?: "Erro ao atualizar") }
            )
        }
    }
}