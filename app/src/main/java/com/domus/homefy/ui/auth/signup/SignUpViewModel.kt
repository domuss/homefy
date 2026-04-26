package com.domus.homefy.ui.auth.signup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch


class SignUpViewModel(private val repository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    fun signUp(email: String, password: String, name: String, username: String) {
        viewModelScope.launch {
            uiState = UiState.Loading
            val result = repository.signUp(email, password, name, username)
            uiState = result.fold(
                onSuccess = { UiState.Success },
                onFailure = { UiState.Error(it.message ?: "Erro ao criar conta") }
            )
        }
    }
}