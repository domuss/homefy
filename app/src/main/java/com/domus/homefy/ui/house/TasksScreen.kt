package com.domus.homefy.ui.house



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    houseId: Long,
    houseName: String,
    taskViewModel: TaskViewModel = koinViewModel()
) {
    val tasks = taskViewModel.tasksList
    val uiStatus = taskViewModel.uiStatus



    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }


    LaunchedEffect(houseId) {
        taskViewModel.loadTasks(houseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tarefas da Casa", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(houseName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiStatus is TaskUIStatus.Loading && tasks.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tudo limpo por aqui!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nenhuma tarefa pendente. Clique no botão + para adicionar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.is_completed)
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(
                                    checked = task.is_completed,
                                    onCheckedChange = { taskViewModel.toggleTask(task) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Textos da tarefa (Título e Descrição)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        textDecoration = if (task.is_completed) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (task.is_completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!task.description.isNullOrBlank()) {
                                        Text(
                                            text = task.description,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = if (task.is_completed) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                }


                                IconButton(
                                    onClick = { task.id?.let { taskViewModel.deleteTask(it, houseId) } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir Tarefa",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (taskViewModel.showAddDialog) {
        AlertDialog(

            onDismissRequest = { taskViewModel.showAddDialog = false },
            title = { Text("Nova Tarefa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("O que precisa ser feito?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = { newTaskDesc = it },
                        label = { Text("Descrição / Detalhes (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.addTask(
                            houseId = houseId,
                            title = newTaskTitle,
                            description = newTaskDesc,
                            assigneeId = null
                        )

                        newTaskTitle = ""
                        newTaskDesc = ""

                        taskViewModel.showAddDialog = false
                    },
                    enabled = newTaskTitle.isNotBlank()
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {

                TextButton(onClick = { taskViewModel.showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}