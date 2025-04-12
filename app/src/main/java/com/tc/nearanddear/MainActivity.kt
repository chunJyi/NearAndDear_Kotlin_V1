package com.tc.nearanddear

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tc.nearanddear.data.SupabaseClientProvider
import com.tc.nearanddear.ui.screens.HomeScreen
import com.tc.nearanddear.ui.screens.LoginScreen
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


//val supabase = createSupabaseClient(
//    supabaseUrl = "https://bbdadykysfweivoqcrap.supabase.co",
//    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJiZGFkeWt5c2Z3ZWl2b3FjcmFwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNzQ1MzMsImV4cCI6MjA1ODY1MDUzM30.VLJiw_CcFT54PZHQzyW_du8gno6NZshu80O8tYkgbLA"
//) {
//    install(Auth)
//    install(Postgrest)
//    //install other modules
//}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClientProvider.initialize(this)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("login") {
                        LoginScreen(
                            onLoginClick = {
                                navController.navigate("home") {
                                    // Optional: Pop the login screen so the user can't go back to it
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        LoginScreen(onLoginClick = {})
    }
}
