package com.domus.homefy.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.domus.homefy.ui.house.HouseUIStatus

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel()
) {
    val houses = houseViewModel.housesList
    var codeInput by remember { mutableStateOf("") }


    val uiStatus = houseViewModel.uiStatus

    LaunchedEffect(Unit) {
        houseViewModel.loadHouses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Minhas Casas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = codeInput,
            onValueChange = { if (it.length <= 6) codeInput = it.uppercase() },
            label = { Text("Código de Acesso") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ex: A1B2C3") }
        )

        Button(
            onClick = {
                houseViewModel.joinHouse(codeInput)
                codeInput = ""
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = codeInput.length == 6
        ) {
            Text("Entrar na Casa")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(houses) { house ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val codigo = house.access_code ?: "VAZIO"
                            val status = house.is_code_active ?: false
                            navController.navigate("edit-house/${house.id}/${house.name}/$codigo/$status")
                        }
                ) {
                    Text(
                        text = house.name,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (houses.isEmpty()) {
                item {
                    Text("Você ainda não tem nenhuma casa.", modifier = Modifier.padding(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiStatus) {
            is HouseUIStatus.Loading -> Text("Carregando...", color = MaterialTheme.colorScheme.primary)
            is HouseUIStatus.Error -> Text(uiStatus.message, color = MaterialTheme.colorScheme.error)
            is HouseUIStatus.Sucesso -> Text("Ação concluída com sucesso!", color = MaterialTheme.colorScheme.primary)
            else -> {}
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            //
            Button(onClick = { navController.navigate("create-house") }) {
                Text("Nova Casa")
            }

            Button(onClick = { navController.navigate("edit-profile") }) {
                Text("Perfil")
            }

            Button(onClick = { authViewModel.logout() }) {
                Text("Sair")
            }
        }
    }
}