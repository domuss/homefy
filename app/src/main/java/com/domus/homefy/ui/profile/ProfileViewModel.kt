package com.domus.homefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.User
import com.domus.homefy.data.UserRepository
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val houseRepository: HouseRepository
) : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    var deleteAccountState by mutableStateOf<UiState>(UiState.Idle)
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

    fun deleteAccount() {
        viewModelScope.launch {
            deleteAccountState = UiState.Loading

            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                deleteAccountState = UiState.Error("Usuário não autenticado")
                return@launch
            }

            val publicUser = currentPublicUser
            if (publicUser?.id == null) {
                deleteAccountState = UiState.Error("Perfil não encontrado")
                return@launch
            }

            val publicUserId = publicUser.id.toInt()

            // 1. Para cada casa que o usuário criou: transfere liderança ou deleta
            val createdHouses = houseRepository.getHousesByUser(publicUserId).getOrNull() ?: emptyList()
            for (house in createdHouses) {
                val houseId = house.id ?: continue
                val oldestMember = houseRepository.getOldestMember(houseId, publicUserId).getOrNull()
                if (oldestMember != null) {
                    houseRepository.transferOwnership(houseId, oldestMember.user_id)
                } else {
                    houseRepository.deleteHouse(houseId)
                }
            }

            // 2. Remove o usuário de todas as casas como membro
            houseRepository.removeMemberFromAllHouses(publicUserId)

            // 3. Deleta o registro público do usuário
            val deleteResult = userRepository.deleteUser(authUser.id)
            if (deleteResult.isFailure) {
                deleteAccountState = UiState.Error(
                    deleteResult.exceptionOrNull()?.message ?: "Erro ao deletar conta"
                )
                return@launch
            }

            // 4. Faz logout — o sessionFlow dispara NotAuthenticated e AppNavigation
            //    troca automaticamente para AuthNavGraph
            authRepository.logout()
        }
    }
}