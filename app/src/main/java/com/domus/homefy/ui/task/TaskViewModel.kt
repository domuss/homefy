package com.domus.homefy.ui.task

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.Task
import com.domus.homefy.data.TaskRepository
import kotlinx.coroutines.launch

sealed interface TaskUIStatus {
    object Esperando : TaskUIStatus
    object Loading : TaskUIStatus
    object Sucesso : TaskUIStatus
    data class Error(val message: String) : TaskUIStatus
}

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<TaskUIStatus>(TaskUIStatus.Esperando)
        private set

    var members by mutableStateOf<List<HouseMemberOption>>(emptyList())
        private set

    fun loadMembers(houseId: Long) {
        viewModelScope.launch {
            val result = houseRepository.getMembersByHouse(houseId)

            if (result.isSuccess) {
                members = result.getOrNull() ?: emptyList()
            } else {
                uiStatus = TaskUIStatus.Error("Erro ao buscar membros da casa")
            }
        }
    }

    fun createTask(
        houseId: Long?,
        assigneeId: Long?,
        title: String,
        description: String
    ) {
        if (houseId == null) {
            uiStatus = TaskUIStatus.Error("Selecione uma casa")
            return
        }

        if (assigneeId == null) {
            uiStatus = TaskUIStatus.Error("Selecione um membro")
            return
        }

        if (title.isBlank()) {
            uiStatus = TaskUIStatus.Error("O título da tarefa não pode ficar vazio")
            return
        }

        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading

            val task = Task(
                house_id = houseId,
                title = title,
                description = description.ifBlank { null },
                assignee_id = assigneeId
            )

            val result = taskRepository.createTask(task)

            uiStatus = if (result.isSuccess) {
                TaskUIStatus.Sucesso
            } else {
                TaskUIStatus.Error("Erro ao criar tarefa: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}