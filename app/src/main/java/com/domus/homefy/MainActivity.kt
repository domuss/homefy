package com.domus.homefy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domus.homefy.data.AuthState
import com.domus.homefy.ui.home.HomeScreen
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.auth.login.LoginScreen
import com.domus.homefy.ui.auth.signup.SignUpScreen
import com.domus.homefy.ui.house.EditHouseScreen
import com.domus.homefy.ui.house.CreateHouseScreen
import com.domus.homefy.ui.profile.EditProfileScreen
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
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        AuthState.Authenticated -> MainNavGraph()
        AuthState.NotAuthenticated -> AuthNavGraph()
        else -> {}
//        AuthState.NotAuthenticated -> LoginScreen()
    }

}

@Composable
fun AuthNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController)
        }
    }
}

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }

        composable("create-house") {
            CreateHouseScreen(
                onHouseCreated = {
                    // Quando a casa for criada, pedimos para o navController voltar
                    // para a tela anterior (a HomeScreen)
                    navController.popBackStack()
                }
            )
        }
        composable("edit-profile") {
            EditProfileScreen(navController)
        }
        composable("edit-house/{id}/{name}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLong() ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: ""

            EditHouseScreen(
                navController = navController,
                houseId = id,
                currentName = name
            )
        }
    }
}