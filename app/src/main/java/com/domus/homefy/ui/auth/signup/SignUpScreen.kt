package com.domus.homefy.ui.auth.signup

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel = koinViewModel()) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is UiState.Success) {
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Criar conta",
            fontSize = 32.sp,
            color = Color.hsl(hue = 271F, saturation = 0.98F, lightness = 0.38F)
        )

        Spacer(modifier = Modifier.padding(8.dp))

        OutlinedTextField(
            placeholder = { Text(text = "Nome completo") },
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        OutlinedTextField(
            placeholder = { Text(text = "Nome de usuário") },
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(color = Color.Black)
        )

        OutlinedTextField(
            placeholder = { Text(text = "Email") },
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(color = Color.Black)
        )

        OutlinedTextField(
            placeholder = { Text(text = "Senha") },
            value = password,
            onValueChange = { password = it },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Lock else Icons.Default.Search,
                        contentDescription = null
                    )
                }
            },
            textStyle = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Button(
            onClick = { viewModel.signUp(email, password, name, username) },
            enabled = viewModel.uiState !is UiState.Loading
        ) {
            Text(if (viewModel.uiState is UiState.Loading) "Cadastrando..." else "Cadastrar")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Já tem conta? Entrar")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        if (viewModel.uiState is UiState.Error) {
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Red,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(4.dp)
            ) {
                Text(color = Color.Red, text = (viewModel.uiState as UiState.Error).message)
            }
        }
    }
}