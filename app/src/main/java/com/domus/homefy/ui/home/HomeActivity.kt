package com.domus.homefy.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseViewModel
import org.koin.androidx.compose.koinViewModel



@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel(),
    houseViewModel: HouseViewModel = koinViewModel()
) {
    val houses = houseViewModel.housesList

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

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(houses) { house ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("edit-house/${house.id}/${house.name}")
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            //
            Button(onClick = { navController.navigate("create-house") }) {
                Text("Nova Casa")
            }

            Button(onClick = { navController.navigate("edit-profile") }) {
                Text("Perfil") // Encurtei o texto só pra caber melhor na tela
            }

            Button(onClick = { authViewModel.logout() }) {
                Text("Sair")
            }
        }
    }
}