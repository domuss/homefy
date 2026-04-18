package com.domus.homefy.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.domus.homefy.ui.home.LoginViewModel

//class LoginActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val sessionManager = SessionManager(this)
//
//        setContent {
//            LoginScreen(
//                onLogin = { onLogin(sessionManager) }
//            )
//        }
//    }
//}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Homefy", fontSize = 40.sp, color = Color.hsl(hue = 271F, saturation = 0.98F, lightness = 0.38F))

        Spacer(modifier = Modifier.padding(8.dp))

        OutlinedTextField(
            placeholder = { Text(text = "Nome de usuário") },
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            textStyle = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.padding(4.dp))

        OutlinedTextField(
            placeholder = { Text(text = "Senha") },
            value = password,
            onValueChange = { password = it },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.padding(4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
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

        Button(onClick = {
            viewModel.login()
        }) {
            Text("Entrar")
        }
    }
}