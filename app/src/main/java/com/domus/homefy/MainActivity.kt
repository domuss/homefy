package com.domus.homefy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domus.homefy.ui.home.HomeScreen
import com.domus.homefy.ui.home.LoginViewModel
import com.domus.homefy.ui.login.LoginScreen
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = koinViewModel()
    val isLogged by loginViewModel.isLogged.collectAsState()

    LaunchedEffect(isLogged) {
        if (isLogged) {
            navController.navigate("home") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("login")
            }

        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("login") {
            LoginScreen(loginViewModel)
        }

        composable("home") {
            HomeScreen(loginViewModel)
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    HomefyTheme {
//        Greeting("Android")
//    }
//}
