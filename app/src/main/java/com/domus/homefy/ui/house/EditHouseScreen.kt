package com.domus.homefy.ui.house

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditHouseScreen(
    navController: NavController,
    houseId: Long,
    currentName: String,
    houseViewModel: HouseViewModel = koinViewModel()
) {
    var houseName by remember { mutableStateOf(currentName) }
    val uiStatus = houseViewModel.uiStatus

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

        Button(
            onClick = {
                houseViewModel.updateHouse(houseId, houseName)
            },
            enabled = uiStatus != HouseUIStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
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