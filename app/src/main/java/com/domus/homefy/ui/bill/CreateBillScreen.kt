package com.domus.homefy.ui.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.data.House
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.ui.auth.AdminState
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseUIStatus
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBillScreen(
    navController: NavController,
    padding: PaddingValues,
    billViewModel: BillViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    var selectedHouse by remember { mutableStateOf<House?>(null) }
    var selectedMember by remember { mutableStateOf<HouseMemberOption?>(null) }

    var houseMenuExpanded by remember { mutableStateOf(false) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    val houses = houseViewModel.housesList
    val members = billViewModel.members
    val uiStatus = billViewModel.uiStatus
    val houseUiStatus = houseViewModel.uiStatus

    val adminState by authViewModel.isAdmin.collectAsState()
    val isAdmin = (adminState as? AdminState.IsAdmin)?.admin == true

    LaunchedEffect(Unit) {
        billViewModel.clearStatus()
        houseViewModel.loadHousesWithAdmin()
    }

    LaunchedEffect(selectedHouse?.id) {
        selectedMember = null

        selectedHouse?.id?.let { houseId ->
            billViewModel.loadMembers(houseId)
            authViewModel.checkAdmin(houseId)
        }
    }

    LaunchedEffect(uiStatus) {
        if (uiStatus is BillUIStatus.Sucesso) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Criar conta",
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

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Valor (ex: 120,50)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Vencimento (AAAA-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = houseMenuExpanded,
            onExpandedChange = {
                if (houses.isNotEmpty()) {
                    houseMenuExpanded = !houseMenuExpanded
                }
            }
        ) {
            OutlinedTextField(
                value = selectedHouse?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Casa") },
                enabled = houses.isNotEmpty(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = houseMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                if (selectedHouse != null && isAdmin) {
                    memberMenuExpanded = !memberMenuExpanded
                }
            }
        ) {
            OutlinedTextField(
                value = selectedMember?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Responsável (opcional)") },
                enabled = selectedHouse != null && isAdmin,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = memberMenuExpanded,
                onDismissRequest = { memberMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sem responsável") },
                    onClick = {
                        selectedMember = null
                        memberMenuExpanded = false
                    }
                )

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

        if (houseUiStatus is HouseUIStatus.Sucesso && houses.isEmpty()) {
            Text(
                text = "Você precisa ser administrador de uma casa para criar contas.",
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (selectedHouse != null && !isAdmin) {
            Text(
                text = "Apenas administradores podem criar contas nesta casa.",
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                billViewModel.createBill(
                    houseId = selectedHouse?.id,
                    responsibleId = selectedMember?.userId?.toLong(),
                    title = title,
                    description = description,
                    amount = amount,
                    dueDate = dueDate
                )
            },
            enabled = selectedHouse != null && isAdmin && uiStatus != BillUIStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar conta")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        if (uiStatus is BillUIStatus.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiStatus.message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
