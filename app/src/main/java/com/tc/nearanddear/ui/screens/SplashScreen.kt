package com.tc.nearanddear.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tc.nearanddear.R
import com.tc.nearanddear.common.DataStoreManager
import com.tc.nearanddear.data.SupabaseClientProvider
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.session.UserSession
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(context: Context, onSplashFinished: (Boolean, Boolean) -> Unit) {
    LaunchedEffect(Unit) {
//        delay(2000) // Simulate splash delay
        val isUserLoggedIn = DataStoreManager.isUserLoggedIn(context)
        val isOnboardingDone = DataStoreManager.isOnboardingCompleted(context)

        if (isUserLoggedIn) {
            val userId = DataStoreManager.getUserID(context)

            UserSession.loginUser = fetchUserById(userId)
        }
        onSplashFinished(isUserLoggedIn, isOnboardingDone)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Near & Dear",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = Color(0xFF2563EB),
                strokeWidth = 4.dp
            )
        }

        Text(
            text = "© 2025 Near & Dear. All rights reserved.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

suspend fun fetchUserById(userId: String): LoginUser? {
    val client = SupabaseClientProvider.client

    return try {
        val client = client
        val result = client
            .from("loginUser")
            .select {
                filter { eq("userID", userId) }
            }
            .decodeList<LoginUser>()
        result.firstOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
