package com.domus.homefy.ui.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var tasks by mutableStateOf<List<Task>>(emptyList())
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

    fun loadTasks(houseId: Long) {
        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading

            val result = taskRepository.getTasksByHouse(houseId)

            if (result.isSuccess) {
                tasks = result.getOrNull() ?: emptyList()
                uiStatus = TaskUIStatus.Sucesso
            } else {
                uiStatus = TaskUIStatus.Error("Erro ao carregar tarefas")
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

    fun toggleTaskCompleted(task: Task, checked: Boolean) {
        val taskId = task.id ?: return

        viewModelScope.launch {
            val result = taskRepository.updateTaskCompleted(taskId, checked)

            if (result.isSuccess) {
                tasks = tasks.map {
                    if (it.id == taskId) it.copy(is_completed = checked) else it
                }
            } else {
                uiStatus = TaskUIStatus.Error("Erro ao atualizar tarefa")
            }
        }
    }

    fun updateTask(
        task: Task,
        title: String,
        description: String,
        assigneeId: Long?
    ) {
        if (title.isBlank()) {
            uiStatus = TaskUIStatus.Error("O título da tarefa não pode ficar vazio")
            return
        }

        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading

            val updatedTask = task.copy(
                title = title,
                description = description.ifBlank { null },
                assignee_id = assigneeId
            )

            val result = taskRepository.updateTask(updatedTask)

            uiStatus = if (result.isSuccess) {
                tasks = tasks.map {
                    if (it.id == updatedTask.id) updatedTask else it
                }
                TaskUIStatus.Sucesso
            } else {
                TaskUIStatus.Error("Erro ao atualizar tarefa")
            }
        }
    }

    fun deleteTask(task: Task, onDeleted: () -> Unit) {
        val taskId = task.id ?: return

        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading

            val result = taskRepository.deleteTask(taskId)

            if (result.isSuccess) {
                tasks = tasks.filterNot { it.id == taskId }
                uiStatus = TaskUIStatus.Sucesso
                onDeleted()
            } else {
                uiStatus = TaskUIStatus.Error("Erro ao excluir tarefa")
            }
        }
    }
}