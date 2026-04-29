package com.domus.homefy.ui.house

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.HouseRepository
import  androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.House
import com.domus.homefy.data.UserRepository
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch

sealed interface HouseUIStatus{
    object  Esperando: HouseUIStatus
    object Loading: HouseUIStatus
    object  Sucesso: HouseUIStatus
    data class Error(val message: String) : HouseUIStatus
}
class HouseViewModel(private  val houseRepository: HouseRepository,
                     private val authRepository : AuthRepository,
                     private val userRepository: UserRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<HouseUIStatus>(HouseUIStatus.Esperando)
    private set
    var housesList by mutableStateOf<List<House>>(emptyList())
        private set


    private fun generateAccessCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun loadHouses() {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading
            val supaId = authRepository.getCurrentUser()?.id

            if (supaId == null) {
                uiStatus = HouseUIStatus.Error("Usuário não está logado")
                return@launch
            }


            val userResult = userRepository.getUserBySupaId(supaId)
            val publicUserId = userResult.getOrNull()?.id?.toInt()

            if (publicUserId == null) {
                uiStatus = HouseUIStatus.Error("Usuário não encontrado na base de dados pública")
                return@launch
            }

            // Agora sim, passa o publicUserId (Int) certinho!
            val result = houseRepository.getHousesByUser(publicUserId)

            if (result.isSuccess) {
                housesList = result.getOrNull() ?: emptyList()
                uiStatus = HouseUIStatus.Sucesso
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao buscar as casas.")
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
            val publicUserId = userResult.getOrNull()?.id?.toInt()

            if (publicUserId == null) {
                uiStatus = HouseUIStatus.Error("Usuário não encontrado na base de dados pública")
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
                loadHouses()
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

            val result = houseRepository.updateHouseName(houseId, newName)

            if (result.isSuccess) {
                uiStatus = HouseUIStatus.Sucesso
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao atualizar casa")
            }
        }
    }



    fun toggleCodeStatus(houseId: Long, isActive: Boolean) {
        viewModelScope.launch {
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
            val result = houseRepository.deleteHouse(houseId)
            if (result.isSuccess) {
                onDeleted()
            } else {
                uiStatus = HouseUIStatus.Error("Erro ao deletar: ${result.exceptionOrNull()?.message}")
            }
        }
    }


}