package com.tc.nearanddear.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tc.nearanddear.R
import com.tc.nearanddear.services.AuthService
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.tc.nearanddear.common.DataStoreManager
import com.tc.nearanddear.session.UserSession

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderText()

        GoogleSignInButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val user = AuthService.signInWithGoogle(context)
                        if (user != null) {
                            UserSession.loginUser = user
                            DataStoreManager.setUserLoggedIn(context, true)
//                            saveLoginStatus(context, true)
                            Toast.makeText(context, "You're signed in!", Toast.LENGTH_SHORT).show()
                            onLoginClick()
                        } else {
                            Toast.makeText(context, "Sign-in failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                        Log.e(TAG, "Sign-in failed: ${e.message}", e)
                    }
                }
            })

        Spacer(modifier = Modifier.height(16.dp))

        AppleSignInButton(onClick = {
            Toast.makeText(context, "Apple Sign-In not implemented", Toast.LENGTH_SHORT).show()
            onLoginClick()
        })

        Spacer(modifier = Modifier.height(24.dp))

        TermsText()
    }
}

@Composable
private fun HeaderText() {
    Text(
        text = "Near & Dear",
        fontSize = 40.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Cursive,
        modifier = Modifier.padding(bottom = 48.dp)
    )
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5795E6), contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AppleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5795E6), contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple_icon),
                contentDescription = "Apple Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Continue with Apple",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TermsText() {
    Text(
        text = "By clicking continue, you agree to our Terms of Service and Privacy Policy",
        fontSize = 12.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

fun saveLoginStatus(context: Context, isLoggedIn: Boolean) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit() {
        putBoolean("is_user_logged_in", isLoggedIn)
    }  // Apply changes asynchronously
}