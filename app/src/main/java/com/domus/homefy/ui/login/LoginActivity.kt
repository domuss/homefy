package com.domus.homefy.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            viewModel.login()
        }) {
            Text("Entrar")
        }
    }
}