package com.domus.homefy.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.BlindsClosed
import androidx.compose.material.icons.filled.Curtains
import androidx.compose.material.icons.filled.CurtainsClosed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.RollerShades
import androidx.compose.material.icons.filled.RollerShadesClosed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.auth.UiState
import com.domus.homefy.ui.shared.Layout
import io.ktor.util.reflect.instanceOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = koinViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val submitEnabled = authViewModel.uiState !is UiState.Loading && (email.isNotEmpty() && password.isNotEmpty())

    val snackbarHostState = remember { SnackbarHostState() }

    val inputModifier = Modifier
        .fillMaxWidth()

    LaunchedEffect(authViewModel.uiState) {
        when (authViewModel.uiState) {
            is UiState.Error -> {
                snackbarHostState.showSnackbar((authViewModel.uiState as UiState.Error).message)
                authViewModel.clearUiState()
            }

            else -> {}
        }
    }

    Layout (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(238, 230, 252))
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6650A4))
                ) {
                    Icon(
                        Icons.Filled.Home, contentDescription = "Lock icon",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "Acesse sua conta",
                    fontSize = 32.sp,
                    color = Color.hsl(hue = 271F, saturation = 0.98F, lightness = 0.38F)
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Label("E-mail")
                OutlinedTextField(
                    placeholder = { Text("placeholder@email.com") },
                    value = email,
                    onValueChange = { email = it },
                    modifier = inputModifier,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    isError = (authViewModel.uiState is UiState.Error) or (authViewModel.uiState is UiState.FormError),
                    supportingText = {
                        if (authViewModel.uiState is UiState.FormError) Text("Campo Obrigatório")
                    }
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Label("Senha")
                OutlinedTextField(
                    placeholder = { Text("········", fontWeight = FontWeight.ExtraBold) },
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = inputModifier,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Filled.RollerShadesClosed else Icons.Filled.RollerShades,
                                contentDescription = null
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color.Black),
                    isError = (authViewModel.uiState is UiState.Error) or (authViewModel.uiState is UiState.FormError),
                    supportingText = {
                        if (authViewModel.uiState is UiState.FormError) Text("Campo Obrigatório")
                    }
                )

                Spacer(modifier = Modifier.padding(4.dp))

                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    authViewModel.login(email, password)
                }, enabled = submitEnabled) {
                    Text("Entrar")
                }

                TextButton(onClick = {
                    navController.navigate("signup")
                }) {
                    Text("Não tem conta? Cadastre-se")
                }
            }
        }
    }
}

@Composable
fun Label(text: String) {
    val labelModifier = Modifier.fillMaxWidth()

    Row(modifier = labelModifier, horizontalArrangement = Arrangement.Start) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
    Spacer(modifier = Modifier.padding(2.dp))
}