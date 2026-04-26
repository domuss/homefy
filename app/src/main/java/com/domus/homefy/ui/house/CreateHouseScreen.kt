package com.domus.homefy.ui.house

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateHouseScreen(
    houseViewModel: HouseViewModel = koinViewModel(),
    onHouseCreated: () -> Unit
) {
    var houseName by remember { mutableStateOf("") }
    val uiStatus = houseViewModel.uiStatus

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Nova Casa",
            fontSize = 32.sp,
            color = Color.hsl(hue = 271F, saturation = 0.98F, lightness = 0.38F)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = houseName,
            onValueChange = { houseName = it },
            label = { Text("Nome da Casa") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { houseViewModel.createHouse(houseName) },
            enabled = uiStatus != HouseUIStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiStatus == HouseUIStatus.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Criar Casa")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiStatus) {
            is HouseUIStatus.Error -> {
                Text(
                    text = uiStatus.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is HouseUIStatus.Sucesso -> {
                LaunchedEffect(Unit) {
                    onHouseCreated()
                }
            }
            else -> {}
        }
    }
}