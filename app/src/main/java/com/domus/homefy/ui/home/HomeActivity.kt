package com.domus.homefy.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.domus.homefy.ui.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

//class HomeActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            HomeScreen()
//        }
//    }
//}

@Composable
fun HomeScreen(authViewModel: AuthViewModel = koinViewModel()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            authViewModel.logout()
        }) {
            Text("Sair")
        }
    }
}