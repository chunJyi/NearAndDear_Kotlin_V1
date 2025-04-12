package com.tc.nearanddear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tc.nearanddear.common.DataStoreManager
import com.tc.nearanddear.data.SupabaseClientProvider
import com.tc.nearanddear.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClientProvider.initialize(this)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

//                // Start navigation after checking login and onboarding status
//                LaunchedEffect(Unit) {
//                    val userIsLoggedIn = DataStoreManager.isUserLoggedIn(context)
//                    val onboardingCompleted = DataStoreManager.isOnboardingCompleted(context)
//
//                    // Navigate based on login and onboarding status
//                    when {
//                        !userIsLoggedIn -> navController.navigate("login") {
//                            popUpTo("splash") {
//                                inclusive = true
//                            }
//                        }
//
//                        !onboardingCompleted -> navController.navigate("onboarding") {
//                            popUpTo("splash") {
//                                inclusive = true
//                            }
//                        }
//
//                        else -> navController.navigate("home") {
//                            popUpTo("splash") {
//                                inclusive = true
//                            }
//                        }
//                    }
//                }

                // NavHost for screen transitions
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(context) { userIsLoggedIn, onboardingCompleted ->
                            if (!onboardingCompleted) {
                                navController.navigate("onboarding") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else if (!userIsLoggedIn) {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }

                    }

                    composable("login") {
                        LoginScreen(onLoginClick = {
                            // Once logged in, go to onboarding
                            navController.navigate("onboarding") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }

                    composable("onboarding") {
                        OnboardingScreen(onPermissionsGranted = {
                            // After onboarding is done, go to home
                            DataStoreManager.setOnboardingCompleted(context)
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        })
                    }

                    composable("home") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    content()
}

