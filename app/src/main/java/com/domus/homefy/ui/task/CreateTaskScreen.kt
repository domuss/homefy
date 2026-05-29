package com.domus.homefy.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.data.House
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    padding: PaddingValues,
    taskViewModel: TaskViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedHouse by remember { mutableStateOf<House?>(null) }
    var selectedMember by remember { mutableStateOf<HouseMemberOption?>(null) }

    var houseMenuExpanded by remember { mutableStateOf(false) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    val houses = houseViewModel.housesList
    val members = taskViewModel.members
    val uiStatus = taskViewModel.uiStatus

    LaunchedEffect(Unit) {
        houseViewModel.loadHouses()
    }

    LaunchedEffect(selectedHouse?.id) {
        selectedMember = null

        selectedHouse?.id?.let { houseId ->
            taskViewModel.loadMembers(houseId)
        }
    }

    LaunchedEffect(uiStatus) {
        if (uiStatus is TaskUIStatus.Sucesso) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Criar tarefa",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = memberMenuExpanded,
            onExpandedChange = {
                if (selectedHouse != null) {
                    memberMenuExpanded = !memberMenuExpanded
                }
            }
        ) {
            OutlinedTextField(
                value = selectedMember?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Membro responsável") },
                enabled = selectedHouse != null,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberMenuExpanded)
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
                            selectedMember = member
                            memberMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                taskViewModel.createTask(
                    houseId = selectedHouse?.id,
                    assigneeId = selectedMember?.userId?.toLong(),
                    title = title,
                    description = description
                )
            },
            enabled = uiStatus != TaskUIStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar tarefa")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        if (uiStatus is TaskUIStatus.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiStatus.message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}