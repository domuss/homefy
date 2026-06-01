package com.domus.homefy.ui.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.data.House
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.data.Task
import com.domus.homefy.ui.auth.AdminState
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    padding: PaddingValues,
    taskViewModel: TaskViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    var selectedHouse by remember { mutableStateOf<House?>(null) }
    var houseMenuExpanded by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val houses = houseViewModel.housesList
    val tasks = taskViewModel.tasks
    val members = taskViewModel.members
    val uiStatus = taskViewModel.uiStatus

    val adminState by authViewModel.isAdmin.collectAsState()
    val isAdmin = (adminState as? AdminState.IsAdmin)?.admin == true
    val normalizedSearchQuery = searchQuery.trim()
    val filteredTasks = if (normalizedSearchQuery.isBlank()) {
        tasks
    } else {
        tasks.filter { task ->
            val assigneeName = members.firstOrNull {
                it.userId.toLong() == task.assignee_id
            }?.name.orEmpty()

            task.title.contains(normalizedSearchQuery, ignoreCase = true) ||
                task.description.orEmpty().contains(normalizedSearchQuery, ignoreCase = true) ||
                assigneeName.contains(normalizedSearchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        houseViewModel.loadHouses()
    }

    LaunchedEffect(houses) {
        if (selectedHouse == null && houses.isNotEmpty()) {
            selectedHouse = houses.first()
        }
    }

    LaunchedEffect(selectedHouse?.id) {
        selectedHouse?.id?.let { houseId ->
            taskViewModel.loadMembers(houseId)
            taskViewModel.loadTasks(houseId)
            authViewModel.checkAdmin(houseId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(
            text = "Tarefas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = houseMenuExpanded,
            onExpandedChange = { houseMenuExpanded = !houseMenuExpanded }
        ) {
            OutlinedTextField(
                value = selectedHouse?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Casa") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = houseMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = houseMenuExpanded,
                onDismissRequest = { houseMenuExpanded = false }
            ) {
                houses.forEach { house ->
                    DropdownMenuItem(
                        text = { Text(house.name) },
                        onClick = {
                            selectedHouse = house
                            houseMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar tarefas") },
            placeholder = { Text("Título, descrição ou responsável") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiStatus is TaskUIStatus.Error) {
            Text(
                text = uiStatus.message,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (selectedHouse == null) {
            Text("Selecione uma casa para ver as tarefas.")
            return@Column
        }

        if (tasks.isEmpty()) {
            Text("Nenhuma tarefa cadastrada para esta casa.")
            return@Column
        }

        if (filteredTasks.isEmpty()) {
            Text("Nenhuma tarefa encontrada para a busca.")
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTasks) { task ->
                val assignee = members.firstOrNull {
                    it.userId.toLong() == task.assignee_id
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isAdmin) {
                            taskBeingEdited = task
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.is_completed,
                            onCheckedChange = { checked ->
                                taskViewModel.toggleTaskCompleted(task, checked)
                            }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (task.is_completed) {
                                    TextDecoration.LineThrough
                                } else {
                                    null
                                }
                            )

                            if (!task.description.isNullOrBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = "Responsável: ${assignee?.name ?: "Sem responsável"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        taskBeingEdited?.let { task ->
            EditTaskDialog(
                task = task,
                members = members,
                onDismiss = {
                    taskBeingEdited = null
                },
                onSave = { title, description, assigneeId ->
                    taskViewModel.updateTask(
                        task = task,
                        title = title,
                        description = description,
                        assigneeId = assigneeId
                    )
                    taskBeingEdited = null
                },
                onDelete = {
                    taskViewModel.deleteTask(task) {
                        taskBeingEdited = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    members: List<HouseMemberOption>,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }
    var selectedAssigneeId by remember { mutableStateOf(task.assignee_id) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    val selectedMember = members.firstOrNull {
        it.userId.toLong() == selectedAssigneeId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar tarefa")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = memberMenuExpanded,
                    onExpandedChange = {
                        memberMenuExpanded = !memberMenuExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Responsável") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = memberMenuExpanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = memberMenuExpanded,
                        onDismissRequest = { memberMenuExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (member.username.isNullOrBlank()) {
                                            member.name
                                        } else {
                                            "${member.name} (@${member.username})"
                                        }
                                    )
                                },
                                onClick = {
                                    selectedAssigneeId = member.userId.toLong()
                                    memberMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(title, description, selectedAssigneeId)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDelete
            ) {
                Text("Excluir")
            }
        }
    )
}
