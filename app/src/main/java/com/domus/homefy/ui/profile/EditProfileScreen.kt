package com.domus.homefy.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val publicUser = viewModel.currentPublicUser
    val currentName = publicUser?.name ?: ""
    val currentUsername = publicUser?.username ?: ""
    val currentEmail = viewModel.currentAuthUser?.email ?: ""

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is UiState.Success) {
            navController.popBackStack()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Editar perfil",
            fontSize = 32.sp,
            color = Color.hsl(hue = 271F, saturation = 0.98F, lightness = 0.38F)
        )

        Spacer(modifier = Modifier.padding(12.dp))

        Text(
            text = "Nome atual: $currentName",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        OutlinedTextField(
            placeholder = { Text("Novo nome") },
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        Text(
            text = "Usuário atual: $currentUsername",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        OutlinedTextField(
            placeholder = { Text("Novo nome de usuário") },
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        Text(
            text = "Email atual: $currentEmail",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        OutlinedTextField(
            placeholder = { Text("Novo email") },
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Button(
            onClick = { viewModel.updateUser(name, username, email) },
            enabled = viewModel.uiState !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (viewModel.uiState is UiState.Loading) "Salvando..." else "Salvar alterações")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Cancelar")
        }

        if (viewModel.uiState is UiState.Error) {
            Spacer(modifier = Modifier.padding(4.dp))
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Red, RoundedCornerShape(3.dp))
                    .padding(8.dp)
            ) {
                Text(color = Color.Red, text = (viewModel.uiState as UiState.Error).message)
            }
        }
    }
}