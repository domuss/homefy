package com.domus.homefy.ui.house

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.House
import com.domus.homefy.data.HouseMemberFull
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.Role
import com.domus.homefy.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HouseUIStatus {
    object Esperando : HouseUIStatus
    object Loading : HouseUIStatus
    object Sucesso : HouseUIStatus
    data class Error(val message: String) : HouseUIStatus
}

class HouseViewModel(
    private val houseRepository: HouseRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<HouseUIStatus>(HouseUIStatus.Esperando)
        private set
    var housesList by mutableStateOf<List<House>>(emptyList())
        private set

    val _houseMembersState = MutableStateFlow<List<HouseMemberFull>>(emptyList())
    val houseMembersState = _houseMembersState.asStateFlow()

    private fun generateAccessCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private suspend fun isCurrentUserHouseAdmin(houseId: Long): Boolean {
        val authUser = authRepository.getCurrentUser() ?: return false
        val publicUser = userRepository.getUserBySupaId(authUser.id).getOrNull() ?: return false
        val publicUserId = publicUser.id ?: return false

        return houseRepository.isHouseAdmin(houseId, publicUserId)
    }

    private suspend fun getCurrentPublicUserId(): Long? {
        val authUser = authRepository.getCurrentUser() ?: return null
        return userRepository.getUserBySupaId(authUser.id).getOrNull()?.id
    }

    fun loadHousesWithAdmin() {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading

            val supaId = authRepository.getCurrentUser()?.id

            if (supaId == null) {
                uiStatus = HouseUIStatus.Error("Usuário não está logado")
                return@launch
            }

            val userResult = userRepository.getUserBySupaId(supaId)
            if (userResult.isFailure) {
                val message = userResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                uiStatus = HouseUIStatus.Error("Erro ao buscar usuário público: $message")
                return@launch
            }

            val publicUserId = userResult.getOrNull()?.id?.toInt()
            if (publicUserId == null) {
                uiStatus =
                    HouseUIStatus.Error("Usuário público sem id (verifique a tabela 'users')")
                return@launch
            }

            val result = houseRepository.getHousesByUserAdmin(publicUserId)

            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                val message = if (exception != null) ": ${exception.message}" else ""
                uiStatus = HouseUIStatus.Error("Erro ao buscar casas$message")
                return@launch
            }

            housesList = result.getOrNull() ?: emptyList()
            uiStatus = HouseUIStatus.Sucesso
        }
    }

    fun loadHouses(updateUiStatus: Boolean = true) {
        viewModelScope.launch {
            if (updateUiStatus) {
                uiStatus = HouseUIStatus.Loading
            }
            val supaId = authRepository.getCurrentUser()?.id

            if (supaId == null) {
                if (updateUiStatus) {
                    uiStatus = HouseUIStatus.Error("Usuário não está logado")
                }
                return@launch
            }

            val userResult = userRepository.getUserBySupaId(supaId)
            if (userResult.isFailure) {
                val message = userResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                if (updateUiStatus) {
                    uiStatus = HouseUIStatus.Error("Erro ao buscar usuário público: $message")
                }
                return@launch
            }

            val publicUserId = userResult.getOrNull()?.id?.toInt()
            if (publicUserId == null) {
                if (updateUiStatus) {
                    uiStatus =
                        HouseUIStatus.Error("Usuário público sem id (verifique a tabela 'users')")
                }
                return@launch
            }


            val createdResult = houseRepository.getHousesByUser(publicUserId)
            val createdHouses = createdResult.getOrNull() ?: emptyList()


            val joinedResult = houseRepository.getJoinedHouses(publicUserId)
            val joinedHouses = joinedResult.getOrNull() ?: emptyList()


            if (createdResult.isFailure && joinedResult.isFailure) {
                val erroReal = createdResult.exceptionOrNull()?.message
                    ?: joinedResult.exceptionOrNull()?.message
                    ?: "Erro desconhecido"

                if (updateUiStatus) {
                    uiStatus = HouseUIStatus.Error("Erro ao buscar as casas: $erroReal")
                }
            } else {

                housesList = (createdHouses + joinedHouses).distinctBy { it.id }

                if (updateUiStatus) {
                    uiStatus = HouseUIStatus.Sucesso
                }
            }
        }
    }

    fun createHouse(nome: String) {
        if (nome.isBlank()) {
            uiStatus = HouseUIStatus.Error("O nome da casa não pode ficar vazio")
            return
        }

        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading
            val supaId = authRepository.getCurrentUser()?.id
            if (supaId == null) {
                uiStatus = HouseUIStatus.Error("User não esta logado")
                return@launch
            }


            val userResult = userRepository.getUserBySupaId(supaId)
            if (userResult.isFailure) {
                val message = userResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                uiStatus = HouseUIStatus.Error("Erro ao buscar usuário público: $message")
                return@launch
            }

            val publicUserId = userResult.getOrNull()?.id?.toInt()
            if (publicUserId == null) {
                uiStatus =
                    HouseUIStatus.Error("Usuário público sem id (verifique a tabela 'users')")
                return@launch
            }

            val newHouse = House(
                name = nome,
                creator_id = publicUserId,
                access_code = generateAccessCode(),
                is_code_active = true
            )

            val result = houseRepository.CriarCasa(newHouse)

            if (result.isSuccess) {
                uiStatus = HouseUIStatus.Sucesso
                loadHouses(updateUiStatus = false)
            } else {
                val erroReal = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                uiStatus = HouseUIStatus.Error("Erro do Banco: $erroReal")
            }
        }
    }


    fun updateHouse(houseId: Long, newName: String) {
        if (newName.isBlank()) {
            uiStatus = HouseUIStatus.Error("Nome não pode ser vazio")
            return
        }

        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading

            if (!isCurrentUserHouseAdmin(houseId)) {
                uiStatus = HouseUIStatus.Error("Somente administradores podem editar a casa")
                return@launch
            }

            val result = houseRepository.updateHouseName(houseId, newName)

            if (result.isSuccess) {
                housesList = housesList.map {
                    if (it.id == houseId) it.copy(name = newName.trim()) else it
                }
                uiStatus = HouseUIStatus.Sucesso
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao atualizar casa")
            }
        }
    }


    fun toggleCodeStatus(houseId: Long, isActive: Boolean) {
        viewModelScope.launch {
            if (!isCurrentUserHouseAdmin(houseId)) {
                uiStatus = HouseUIStatus.Error("Somente administradores podem editar a casa")
                return@launch
            }

            val result = houseRepository.updateCodeStatus(houseId, isActive)
            if (result.isSuccess) {
                housesList = housesList.map {
                    if (it.id == houseId) it.copy(is_code_active = isActive) else it
                }
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao alterar o status do código")
            }
        }
    }


    fun deleteHouse(houseId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            if (!isCurrentUserHouseAdmin(houseId)) {
                uiStatus = HouseUIStatus.Error("Somente administradores podem excluir a casa")
                return@launch
            }

            val result = houseRepository.deleteHouse(houseId)
            if (result.isSuccess) {
                housesList = housesList.filterNot { it.id == houseId }
                onDeleted()
            } else {
                uiStatus =
                    HouseUIStatus.Error("Erro ao deletar: ${result.exceptionOrNull()?.message}")
            }
        }
    }


    fun joinHouse(code: String) {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading


            val supaId = authRepository.getCurrentUser()?.id
            if (supaId == null) {
                uiStatus = HouseUIStatus.Error("Usuário não autenticado.")
                return@launch
            }


            val userResult = userRepository.getUserBySupaId(supaId)
            val publicUserId = userResult.getOrNull()?.id?.toInt()

            if (publicUserId == null) {
                uiStatus = HouseUIStatus.Error("Perfil do usuário não encontrado no banco.")
                return@launch
            }


            val houseResult = houseRepository.getHouseByAccessCode(code)
            val house = houseResult.getOrNull()

            if (house == null) {
                uiStatus = HouseUIStatus.Error("Código inválido ou casa não encontrada.")
                return@launch
            }


            if (house.is_code_active != true) {
                uiStatus =
                    HouseUIStatus.Error("Esta casa não está aceitando novos membros no momento.")
                return@launch
            }


            val joinResult = houseRepository.insertMember(house.id!!, publicUserId)

            if (joinResult.isSuccess) {
                uiStatus = HouseUIStatus.Sucesso
                loadHouses()
            } else {
                uiStatus =
                    HouseUIStatus.Error("Erro ao entrar na casa: ${joinResult.exceptionOrNull()?.message}")
            }
        }
    }

    fun loadHouseMembers(houseId: Long) {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading
            _houseMembersState.emit(
                houseRepository.getHouseMembers(houseId).getOrNull() ?: emptyList()
            )
            uiStatus = HouseUIStatus.Sucesso
        }
    }

    fun giveAdmin(houseId: Long, memberId: Long) {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading

            if (!isCurrentUserHouseAdmin(houseId)) {
                uiStatus = HouseUIStatus.Error("Somente administradores podem promover membros")
                return@launch
            }

            val result = houseRepository.giveAdmin(memberId)
            if (result.isSuccess) {
                loadHouseMembers(houseId)
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao promover membro")
            }
        }
    }

    fun removeMember(houseId: Long, memberId: Long) {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading

            if (!isCurrentUserHouseAdmin(houseId)) {
                uiStatus = HouseUIStatus.Error("Somente administradores podem remover membros")
                return@launch
            }

            val currentUserId = getCurrentPublicUserId()
            val memberToRemove = _houseMembersState.value.firstOrNull { it.id == memberId }
            val adminCount = _houseMembersState.value.count { it.role == Role.HOUSE_ADMIN }
            val removingSelfAsLastAdmin = memberToRemove != null &&
                    memberToRemove.user.id == currentUserId &&
                    memberToRemove.role == Role.HOUSE_ADMIN &&
                    adminCount <= 1

            if (removingSelfAsLastAdmin) {
                uiStatus = HouseUIStatus.Error("Você não pode sair sendo o último administrador")
                return@launch
            }

            val result = houseRepository.removeMember(memberId)
            if (result.isSuccess) {
                loadHouseMembers(houseId)
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao remover membro")
            }
        }
    }

}
