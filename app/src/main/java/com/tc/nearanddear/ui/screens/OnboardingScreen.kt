package com.tc.nearanddear.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.tc.nearanddear.common.DataStoreManager.setOnboardingCompleted
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onPermissionsGranted: () -> Unit,
    isPreview: Boolean = false // Add a parameter to detect preview mode
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val showPermissionError = remember { mutableStateOf(false) }
    val permissionsGranted = remember { mutableStateOf(false) }

    // Safely handle the context for ComponentActivity
    val activity = if (!isPreview && context is ComponentActivity) context else null

    val requestPermissionsLauncher = if (activity != null) {
        rememberLauncherForActivityResult(
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
    } else {
        null // No launcher in preview mode
    }

    fun checkAndRequestPermissions() {
        if (isPreview) {
            // Simulate permission handling in preview
            showPermissionError.value = true
            return
        }

        if (activity == null) return // Can't proceed without activity

        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val shouldShowRationaleFine =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, fine)
        val shouldShowRationaleCoarse =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, coarse)

        if (shouldShowRationaleFine || shouldShowRationaleCoarse) {
            showPermissionError.value = true
        } else {
            requestPermissionsLauncher?.launch(arrayOf(fine, coarse))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFCCE5FF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                count = 3,
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Select Item Illustration", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Select Item",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                            )
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Add to Cart Illustration", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Add to Cart",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                            )
                        }
                        2 -> {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Locate Yourself Illustration", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Locate Yourself",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { checkAndRequestPermissions() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp)
                            ) {
                                Text("Grant Permissions", color = Color.Black)
                            }

                            if (showPermissionError.value && !permissionsGranted.value) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Permission denied. Please enable location permissions from app settings.",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (isPreview) return@Button // Skip in preview
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        val uri = Uri.fromParts("package", context.packageName, null)
                                        intent.data = uri
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(48.dp)
                                ) {
                                    Text("Open Settings", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (pagerState.currentPage >= index) Color(0xFF3B82F6) else Color.White
                            )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (pagerState.currentPage > 0) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(38.dp)
                    ) {
                        Text("<<", color = Color.Black)
                    }
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < 2) {
                    Button(

                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(38.dp)
                    ) {
                        Text(">>", color = Color.Black)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen(
            onPermissionsGranted = {},
            isPreview = true // Pass the preview flag
        )
    }
}