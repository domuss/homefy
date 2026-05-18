package com.domus.homefy.ui.house



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.Task
import kotlinx.coroutines.launch


sealed class TaskUIStatus {
    object Idle : TaskUIStatus()
    object Loading : TaskUIStatus()
    data class Success(val message: String? = null) : TaskUIStatus()
    data class Error(val message: String) : TaskUIStatus()
}

class TaskViewModel(private val houseRepository: HouseRepository) : ViewModel() {

    var showAddDialog by mutableStateOf(false)

    var uiStatus by mutableStateOf<TaskUIStatus>(TaskUIStatus.Idle)
        private set

    var tasksList by mutableStateOf<List<Task>>(emptyList())
        private set

    fun loadTasks(houseId: Long) {
        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading
            val result = houseRepository.getTasksByHouse(houseId)

            if (result.isSuccess) {
                tasksList = result.getOrNull() ?: emptyList()
                uiStatus = TaskUIStatus.Success()
            } else {
                uiStatus = TaskUIStatus.Error(result.exceptionOrNull()?.message ?: "Erro ao carregar tarefas")
            }
        }
    }

    fun addTask(houseId: Long, title: String, description: String, assigneeId: Int?) {
        if (title.isBlank()) {
            uiStatus = TaskUIStatus.Error("O título da tarefa não pode ser vazio")
            return
        }

        viewModelScope.launch {
            uiStatus = TaskUIStatus.Loading
            val newTask = Task(
                house_id = houseId,
                title = title,
                description = description.ifBlank { null },
                assignee_id = assigneeId
            )

            val result = houseRepository.createTask(newTask)
            if (result.isSuccess) {
                loadTasks(houseId)
            } else {
                uiStatus = TaskUIStatus.Error(result.exceptionOrNull()?.message ?: "Erro ao criar tarefa")
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {

            val updatedStatus = !task.is_completed
            tasksList = tasksList.map { if (it.id == task.id) it.copy(is_completed = updatedStatus) else it }


            task.id?.let { taskId ->
                houseRepository.toggleTaskCompletion(taskId, updatedStatus)
            }
        }
    }

    fun deleteTask(taskId: Long, houseId: Long) {
        viewModelScope.launch {
            val result = houseRepository.deleteTask(taskId)
            if (result.isSuccess) {
                loadTasks(houseId)
            }
        }
    }
}