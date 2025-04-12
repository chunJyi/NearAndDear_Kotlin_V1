package com.tc.nearanddear.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import com.google.accompanist.permissions.*
import com.tc.nearanddear.common.DataStoreManager.setOnboardingCompleted
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import android.provider.Settings
import androidx.core.app.ActivityCompat

import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val showPermissionError = remember { mutableStateOf(false) }
    val permissionsGranted = remember { mutableStateOf(false) }

    val activity = context as ComponentActivity

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted && coarseGranted) {
            permissionsGranted.value = true
            setOnboardingCompleted(context)
            onPermissionsGranted()
        } else {
            showPermissionError.value = true
        }
    }

    // Function to check and request permission properly
    fun checkAndRequestPermissions() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val shouldShowRationaleFine =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, fine)
        val shouldShowRationaleCoarse =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, coarse)

        if (shouldShowRationaleFine || shouldShowRationaleCoarse) {
            // Show explanation or custom dialog if needed
            showPermissionError.value = true
        } else {
            requestPermissionsLauncher.launch(arrayOf(fine, coarse))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalPager(count = 3, state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> Text("👋 Welcome to Near & Dear!", style = MaterialTheme.typography.headlineSmall)
                1 -> Text("📍 Track your friends & family live!", style = MaterialTheme.typography.headlineSmall)
                2 -> Column {
                    Text("🔐 We need location permissions to continue", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))

                    Button(onClick = { checkAndRequestPermissions() }) {
                        Text("Grant Permissions")
                    }

                    if (showPermissionError.value && !permissionsGranted.value) {
                        Text(
                            "Permission denied. Please enable location permissions from app settings.",
                            color = Color.Red
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            // Open app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    }
                }
            }
        }

        // Dots
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(3) { index ->
                val color = if (pagerState.currentPage == index) Color.Blue else Color.LightGray
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pagerState.currentPage < 2) {
            Button(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Text("Next")
            }
        }
    }
}



