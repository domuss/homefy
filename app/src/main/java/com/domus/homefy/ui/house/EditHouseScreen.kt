package com.domus.homefy.ui.house

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
@Composable
fun EditHouseScreen(
    navController: NavController,
    houseId: Long,
    currentName: String,
    initialAccessCode: String,
    initialIsCodeActive: Boolean,
    houseViewModel: HouseViewModel = koinViewModel()
) {
    var houseName by remember { mutableStateOf(currentName) }
    var isCodeActive by remember { mutableStateOf(initialIsCodeActive) }

    val uiStatus = houseViewModel.uiStatus
    val members = houseViewModel.houseMembers

    LaunchedEffect(houseId) {
        houseViewModel.loadHouseMembers(houseId)
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Editar Casa", fontSize = 28.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = houseName,
            onValueChange = { houseName = it },
            label = { Text("Nome da Casa") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Código de Convite", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)


                Text(initialAccessCode, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isCodeActive) "Convites Ativos" else "Convites Bloqueados", fontSize = 16.sp)
                    Switch(
                        checked = isCodeActive,
                        onCheckedChange = { novoStatus ->
                            isCodeActive = novoStatus
                            houseViewModel.toggleCodeStatus(houseId, novoStatus)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))



        Text(
            text = "Moradores da Casa",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.run { fillMaxWidth().padding(vertical = 8.dp) }
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            if (members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum morador além de você.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(member.name, fontSize = 16.sp)
                            IconButton(
                                onClick = { houseViewModel.removeMemberFromHouse(houseId, member.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover Membro",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }





        Button(
            onClick = {
                houseViewModel.updateHouse(houseId, houseName)
            },
            enabled = uiStatus != HouseUIStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {

                houseViewModel.deleteHouse(houseId) {
                    navController.popBackStack()
                }
            },
            enabled = uiStatus != HouseUIStatus.Loading,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Excluir Casa")
        }

        when (uiStatus) {
            is HouseUIStatus.Sucesso -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
            is HouseUIStatus.Error -> {
                Text(uiStatus.message)
            }
            else -> {}
        }
    }
}