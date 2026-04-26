package com.domus.homefy.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun EditProfileScreen(navController: NavController, viewModel: EditProfileViewModel = koinViewModel()) {
    val user = viewModel.currentUser
    val currentName = user?.userMetadata?.get("name")?.toString()?.trim('"') ?: ""
    val currentUsername = user?.userMetadata?.get("username")?.toString()?.trim('"') ?: ""
    val currentEmail = user?.email ?: ""

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

        // Nome
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        // Username
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        // Email
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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