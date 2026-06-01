package com.domus.homefy.ui.bill

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.domus.homefy.data.Bill
import com.domus.homefy.data.House
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.ui.auth.AdminState
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    navController: NavController,
    padding: PaddingValues,
    billViewModel: BillViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    var selectedHouse by remember { mutableStateOf<House?>(null) }
    var houseMenuExpanded by remember { mutableStateOf(false) }
    var billBeingEdited by remember { mutableStateOf<Bill?>(null) }

    val houses = houseViewModel.housesList
    val bills = billViewModel.bills
    val members = billViewModel.members
    val uiStatus = billViewModel.uiStatus

    val adminState by authViewModel.isAdmin.collectAsState()
    val isAdmin = (adminState as? AdminState.IsAdmin)?.admin == true

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
            billViewModel.loadMembers(houseId)
            billViewModel.loadBills(houseId)
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
            text = "Contas",
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

        Spacer(modifier = Modifier.height(16.dp))

        if (uiStatus is BillUIStatus.Error) {
            Text(
                text = uiStatus.message,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (selectedHouse == null) {
            Text("Selecione uma casa para ver as contas.")
            return@Column
        }

        if (bills.isEmpty()) {
            Text("Nenhuma conta cadastrada para esta casa.")
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bills) { bill ->
                val responsible = members.firstOrNull {
                    it.userId.toLong() == bill.responsible_id
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isAdmin) {
                            billBeingEdited = bill
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = bill.is_paid,
                            enabled = isAdmin,
                            onCheckedChange = { checked ->
                                billViewModel.toggleBillPaid(bill, checked)
                            }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (bill.is_paid) {
                                    TextDecoration.LineThrough
                                } else {
                                    null
                                }
                            )

                            if (!bill.description.isNullOrBlank()) {
                                Text(
                                    text = bill.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = "Valor: ${formatAmount(bill.amount_cents)}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "Responsável: ${responsible?.name ?: "Sem responsável"}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = bill.due_date?.let { "Vencimento: $it" } ?: "Sem vencimento",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        billBeingEdited?.let { bill ->
            EditBillDialog(
                bill = bill,
                members = members,
                onDismiss = {
                    billBeingEdited = null
                },
                onSave = { title, description, amount, dueDate, responsibleId, isPaid ->
                    billViewModel.updateBill(
                        bill = bill,
                        title = title,
                        description = description,
                        amount = amount,
                        dueDate = dueDate,
                        responsibleId = responsibleId,
                        isPaid = isPaid
                    )
                    billBeingEdited = null
                },
                onDelete = {
                    billViewModel.deleteBill(bill) {
                        billBeingEdited = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillDialog(
    bill: Bill,
    members: List<HouseMemberOption>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long?, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(bill.title) }
    var description by remember { mutableStateOf(bill.description ?: "") }
    var amount by remember { mutableStateOf(centsToInput(bill.amount_cents)) }
    var dueDate by remember { mutableStateOf(bill.due_date ?: "") }
    var isPaid by remember { mutableStateOf(bill.is_paid) }
    var selectedResponsibleId by remember { mutableStateOf(bill.responsible_id) }
    var memberMenuExpanded by remember { mutableStateOf(false) }

    val selectedMember = members.firstOrNull {
        it.userId.toLong() == selectedResponsibleId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar conta")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = memberMenuExpanded,
                        onDismissRequest = { memberMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sem responsável") },
                            onClick = {
                                selectedResponsibleId = null
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
                                    selectedResponsibleId = member.userId.toLong()
                                    memberMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it }
                    )

                    Text("Conta paga")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(title, description, amount, dueDate, selectedResponsibleId, isPaid)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }

                OutlinedButton(onClick = onDelete) {
                    Text("Excluir")
                }
            }
        }
    )
}

private fun formatAmount(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(cents / 100.0)
}

private fun centsToInput(cents: Long): String {
    return "${cents / 100},${(cents % 100).toString().padStart(2, '0')}"
}
