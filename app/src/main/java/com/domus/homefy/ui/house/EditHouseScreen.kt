package com.domus.homefy.ui.house

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

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


    //val currentHouse = houseViewModel.housesList.find { it.id == houseId }
    //val accessCode = currentHouse?.access_code ?: "..."
    //val isCodeActive = currentHouse?.is_code_active ?: false


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

                //daqui
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
                            isCodeActive = novoStatus // Atualiza a tela instantaneamente
                            houseViewModel.toggleCodeStatus(houseId, novoStatus) // Salva no banco
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


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