package com.domus.homefy

import android.R.attr.padding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.domus.homefy.data.AuthState
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.auth.login.LoginScreen
import com.domus.homefy.ui.auth.signup.SignUpScreen
import com.domus.homefy.ui.home.HomeScreen
import com.domus.homefy.ui.house.CreateHouseScreen
import com.domus.homefy.ui.house.EditHouseScreen
import com.domus.homefy.ui.profile.EditProfileScreen
import com.domus.homefy.ui.shared.Layout
import org.koin.androidx.compose.koinViewModel
import com.domus.homefy.ui.task.CreateTaskScreen

val LocalNavController = compositionLocalOf<NavController> {
    error("NavController not found")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        AppNavigation()
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

    }
}

@Composable
fun AuthNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        /*exitTransition = { ExitTransition.None },
        enterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None }*/) {
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
    val snackbarHostState = remember { SnackbarHostState() }

    Layout(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }, navController = navController
    ) { padding ->
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(navController, padding)
            }

            composable("create-house") {
                CreateHouseScreen(
                    onHouseCreated = {

                        navController.popBackStack()
                    })
            }
            composable("edit-profile") {
                EditProfileScreen(navController, padding)
            }

            composable("create-task") {
                CreateTaskScreen(
                    navController = navController,
                    padding = padding
                )
            }
            composable("edit-house/{id}/{name}/{accessCode}/{isCodeActive}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLong() ?: 0
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val accessCode = backStackEntry.arguments?.getString("accessCode") ?: ""
                val isCodeActive =
                    backStackEntry.arguments?.getString("isCodeActive")?.toBoolean() ?: false

                EditHouseScreen(
                    navController = navController,
                    houseId = id,
                    currentName = name,
                    initialAccessCode = accessCode,
                    initialIsCodeActive = isCodeActive
                )
            }
        }
    }

}