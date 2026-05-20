package com.domus.homefy.ui.home // Verifique se o seu package está assim!

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseUIStatus
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    padding: PaddingValues,
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
            .padding(padding)
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nome da casa
                        Text(
                            text = house.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // Botão de Tarefas
                        IconButton(
                            onClick = {
                                navController.navigate("tasks/${house.id}/${house.name}")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Tarefas",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Botão de Configurações (Edit House)
                        IconButton(
                            onClick = {
                                val codigo = house.access_code ?: "VAZIO"
                                val status = house.is_code_active ?: false
                                navController.navigate("edit-house/${house.id}/${house.name}/$codigo/$status")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurações da Casa",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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