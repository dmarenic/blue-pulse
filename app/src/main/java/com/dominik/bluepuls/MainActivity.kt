package com.dominik.bluepuls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dominik.bluepuls.presentation.AuthViewModel
import com.dominik.bluepuls.presentation.screens.LoginScreen
import com.dominik.bluepuls.presentation.screens.MainAppScreen
import com.dominik.bluepuls.presentation.screens.RegisterScreen
import com.dominik.bluepuls.ui.theme.BluePulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluePulseTheme {
                // AuthViewModel je vlasništvo Activityja - preživljava prelazak
                // između auth ekrana i glavne aplikacije.
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
                val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

                if (isUserLoggedIn) {
                    MainAppScreen(onLogout = { authViewModel.logout() })
                } else {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = {
                                    authViewModel.clearError()
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateToLogin = {
                                    authViewModel.clearError()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
