package com.domus.homefy.ui.house

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.HouseRepository
import  androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.House
import com.domus.homefy.ui.auth.UiState
import kotlinx.coroutines.launch

sealed interface HouseUIStatus{
    object  Esperando: HouseUIStatus
    object Loading: HouseUIStatus
    object  Sucesso: HouseUIStatus
    data class Error(val message: String) : HouseUIStatus
}
class HouseViewModel(private  val houseRepository: HouseRepository,
                     private val authRepository : AuthRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<HouseUIStatus>(HouseUIStatus.Esperando)
    private set
    var housesList by mutableStateOf<List<House>>(emptyList())
        private set

    fun loadHouses() {
        viewModelScope.launch {
            uiStatus = HouseUIStatus.Loading
            val user = authRepository.getCurrentUser()

            if (user == null) {
                uiStatus = HouseUIStatus.Error("Usuário não está logado")
                return@launch
            }

            val result = houseRepository.getHousesByUser(user.id)

            if (result.isSuccess) {
                // Se deu certo, preenchemos nossa variável com a lista!
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
            val user = authRepository.getCurrentUser()
            if (user == null) {
                uiStatus = HouseUIStatus.Error("User não esta logado")
                return@launch
            }

            val newHouse = House(name = nome, creator_id = user.id)
            val result = houseRepository.CriarCasa(newHouse)


            if (result.isSuccess) {
                uiStatus = HouseUIStatus.Sucesso
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



}