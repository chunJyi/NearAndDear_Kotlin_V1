package com.tc.nearanddear

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tc.nearanddear.common.DataStoreManager
import com.tc.nearanddear.data.SupabaseClientProvider
import com.tc.nearanddear.services.LocationService
import com.tc.nearanddear.session.SharedViewModel
import com.tc.nearanddear.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClientProvider.initialize(this)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val sharedViewModel: SharedViewModel = viewModel() // <- shared ViewModel here

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(context) { userIsLoggedIn, onboardingCompleted ->
                            when {
                                !onboardingCompleted -> {
                                    navController.navigate("onboarding") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }

                                !userIsLoggedIn -> {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }

                                else -> {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    composable("onboarding") {
                        OnboardingScreen(onPermissionsGranted = {
                            // Save onboarding state and move to login
                            DataStoreManager.setOnboardingCompleted(context)
                            navController.navigate("login") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        })
                    }

                    composable("login") {
                        LoginScreen(onLoginClick = {
                            // After login, go to home
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }

                    composable("home") {
                        HomeScreen(navController)
                        startLocationService()
                    }

                    composable("map") {
                        MapScreen()//                        startLocationService()
                    }
                }
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    content()
}


