package com.tc.nearanddear.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.tc.nearanddear.R
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.*
import com.tc.nearanddear.session.SharedViewModel
import com.tc.nearanddear.session.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.tc.nearanddear.common.CommonUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.isNotEmpty

sealed class MapScreenState {
    object Loading : MapScreenState()
    data class Success(val user: LoginUser, val cameraPosition: CameraPosition) : MapScreenState()
    object Error : MapScreenState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, sharedViewModel: SharedViewModel = viewModel()) {
    val selectedID = sharedViewModel.selectedFriend
    var mapScreenState by remember { mutableStateOf<MapScreenState>(MapScreenState.Loading) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val defaultZoom = 15f;

    LaunchedEffect(selectedID) {
        selectedID?.takeIf { it.isNotEmpty() }?.let { id ->
            mapScreenState = MapScreenState.Loading
            val initData = getAllDataById(id)
            if (initData != null) {
                sharedViewModel.setUser(initData)
            }
            while (true) {
                try {
                    val user = getUserById(id)
                    if (user != null) {
                        var temp = sharedViewModel.selectedUser?.copy(
                            location_model = user.location_model
                        )
                        sharedViewModel.setUser(temp)
                        val cameraPosition = CameraPosition.fromLatLngZoom(
                            LatLng(
                                (user.location_model?.latitude ?: "0.0").toDouble(),
                                (user.location_model?.longitude ?: "0.0").toDouble()
                            ), 10f
                        )
                        mapScreenState = MapScreenState.Success(user, cameraPosition)
                    } else {
                        mapScreenState = MapScreenState.Error
                    }
                } catch (e: TimeoutCancellationException) {
                    println("Timeout: ${e.message}")
                    mapScreenState = MapScreenState.Error
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    mapScreenState = MapScreenState.Error
                }
                delay(5000)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Settings action */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000).copy(alpha = 0.1f)),
            modifier = Modifier.height(40.dp)
        )

        FriendListBottomSheet(
            sharedViewModel = sharedViewModel,
            showBottomSheet = showBottomSheet,
            onDismiss = { showBottomSheet = false },
            loginUser = UserSession.loginUser
        )

        when (val state = mapScreenState) {
            is MapScreenState.Loading -> {
                LoadingDialog("Fetching location...")
            }

            is MapScreenState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        Text("Failed to load user data.")
                    }

                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show Friends")
                    }
                }
            }

            is MapScreenState.Success -> {
                val cameraPositionState = rememberCameraPositionState {
                    position = state.cameraPosition
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMapView(sharedViewModel, cameraPositionState, defaultZoom) { latLng ->
                        println("Map clicked at: $latLng")
                    }

                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show Friends")
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingDialog(message: String = "Loading...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = message, fontSize = 16.sp)
            }
        }
    }
}

suspend fun getUserById(userId: String): LoginUser? {
    return try {
        val result = client.from("loginUser")
            .select(columns = Columns.list(listOf("name", "location_model"))) {
                filter { eq("userID", userId) }
            }.decodeList<LoginUserLite>().firstOrNull()

        result?.let {
            LoginUser.build {
                userID = userId
                name = it.name
                location_model = it.location_model
            }
        }
    } catch (e: Exception) {
        println("Error fetching user: ${e.message}")
        null
    }
}

suspend fun getAllDataById(userId: String): LoginUser? {
    return try {
        val result = client.from("loginUser").select(
            columns = Columns.list(
                listOf(
                    "name", "location_model", "avatar_url", "userID", "updated_at"
                )
            )
        ) {
            filter { eq("userID", userId) }
        }.decodeList<LoginUserWithoutFriendList>().firstOrNull()

        result?.let {
            LoginUser.build {
                userID = userId
                name = it.name
                location_model = it.location_model
                avatar_url = it.avatar_url
                updated_at = it.updated_at
                friendList = emptyList()
            }
        }
    } catch (e: Exception) {
        println("Error fetching user: ${e.message}")
        null
    }
}


//suspend fun createCustomMarkerBitmap(
//    context: Context,
//    imageUrl: String
//): Bitmap {
//    val imageSize = 100 // 🔸 Inner image (avatar) size
//    val padding = 25    // 🔸 Padding around the avatar inside the marker
//
//    val imageLoader = ImageLoader(context)
//
//    // Load image
//    val request = ImageRequest.Builder(context)
//        .data(imageUrl)
//        .allowHardware(false)
//        .build()
//
//    val drawable = imageLoader.execute(request).drawable
//        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
//
//    val userBitmap = (drawable as BitmapDrawable).bitmap
//    val squareCropped = centerCropSquare(userBitmap)
//    val scaledAvatar = Bitmap.createScaledBitmap(squareCropped, imageSize, imageSize, true)
//
//    // Create circular avatar bitmap
//    val circularBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(circularBitmap)
//    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//    paint.shader = BitmapShader(scaledAvatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//
//    canvas.drawCircle(imageSize / 2f, imageSize / 2f, imageSize / 2f, paint)
//
//    // Load marker base pin
//    val pinBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user_marker)
//        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
//
//    // Resize pin to fit around the avatar
//    val pinSize = imageSize + (padding * 2)
//    val scaledPin = Bitmap.createScaledBitmap(pinBitmap, pinSize, pinSize, true)
//
//    // Create final output
//    val output = Bitmap.createBitmap(pinSize, pinSize, Bitmap.Config.ARGB_8888)
//    val finalCanvas = Canvas(output)
//
//    // Draw pin first
//    finalCanvas.drawBitmap(scaledPin, 0f, 0f, null)
//
//    // Center the avatar circle exactly in the middle horizontally
//    val avatarLeft = (pinSize - imageSize) / 2f
//    val avatarTop = (pinSize - imageSize) / 2f - 10f  // 🔸 shift slightly up if needed
//
//    finalCanvas.drawBitmap(circularBitmap, avatarLeft, avatarTop, null)
//
//    return output
//}
//
//// Crop to center square
//private fun centerCropSquare(bitmap: Bitmap): Bitmap {
//    val size = minOf(bitmap.width, bitmap.height)
//    val x = (bitmap.width - size) / 2
//    val y = (bitmap.height - size) / 2
//    return Bitmap.createBitmap(bitmap, x, y, size, size)
//}



@Composable
fun GoogleMapView(
    viewModel: SharedViewModel,
    cameraPositionState: CameraPositionState,
    zoomLevel: Float,
    onMapClick: (LatLng) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var bitmapDescriptor by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val latLng = viewModel.selectedUser?.location_model?.let {
        LatLng(it.latitude.toDoubleOrNull() ?: 0.0, it.longitude.toDoubleOrNull() ?: 0.0)
    } ?: LatLng(0.0, 0.0)

    LaunchedEffect(viewModel.selectedUser) {
        viewModel.selectedUser?.avatar_url?.let { imageUrl ->
            val markerBitmap = createCustomMarkerBitmap(context, imageUrl)
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(markerBitmap)
        }
    }

    LaunchedEffect(zoomLevel) {
        val currentLatLng = cameraPositionState.position.target
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel),
            durationMs = 1000
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                isBuildingEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = false,
                myLocationButtonEnabled = true
            ),
            onMapClick = onMapClick
        ) {
            if (bitmapDescriptor != null) {
                Marker(
                    state = MarkerState(position = latLng),
                    icon = bitmapDescriptor,
                    title = viewModel.selectedUser?.name ?: "Selected User"
                )
            }
        }

        LocationInfoCard(
            context = context,
            user = viewModel.selectedUser,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )
    }
}

suspend fun createCustomMarkerBitmap(
    context: Context,
    imageUrl: String
): Bitmap {
    val imageSize = 96  // 🔹 inner circle diameter (smaller than before)
    val imageLoader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val drawable = imageLoader.execute(request).drawable
        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    val userBitmap = (drawable as BitmapDrawable).bitmap
    val croppedBitmap = centerCropSquare(userBitmap)
    val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, imageSize, imageSize, true)

    // Draw circular avatar
    val circularBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
    val circleCanvas = Canvas(circularBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

    val radius = imageSize / 2f
    circleCanvas.drawCircle(radius, radius, radius, paint)

    // Load and scale the pin icon
    val pinBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user_marker)
        ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    val pinSize = imageSize + 60  // 🔹 smaller pin to match reduced image
    val scaledPin = Bitmap.createScaledBitmap(pinBitmap, pinSize, pinSize, true)

    // Final output canvas
    val output = Bitmap.createBitmap(pinSize, pinSize, Bitmap.Config.ARGB_8888)
    val finalCanvas = Canvas(output)

    // Draw the marker pin
    finalCanvas.drawBitmap(scaledPin, 0f, 0f, null)

    // Draw circular image over the pin (centered horizontally, and shifted down a bit)
    val imageLeft = (pinSize - imageSize) / 2f
    val imageTop = 12f // adjust based on your pin's layout

    finalCanvas.drawBitmap(circularBitmap, imageLeft, imageTop, null)

    return output
}

// Center crop helper
private fun centerCropSquare(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val size = minOf(width, height)
    val xOffset = (width - size) / 2
    val yOffset = (height - size) / 2
    return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
}


@Composable
private fun LocationInfoCard(
    context: Context,
    user: LoginUser?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(190.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xBAFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(id = R.drawable.clock), // Replace with your image name
                contentDescription = "Sync",
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
                // Optional: control the size
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(

                text = buildString {
                    append(
                        user?.location_model?.let {
                            val instant = Instant.parse(user?.location_model?.updatedAt)
                            val formatter = DateTimeFormatter.ofPattern("MM-dd-yy: HH:mm")
                                .withZone(ZoneId.systemDefault()) // You can specify a different zone if needed
                            formatter.format(instant);
                        } ?: "local time zone"
                    )
                },
                color = Color.Black,
                fontSize = 16.sp
            )
            Spacer(Modifier.width(5.dp))

            if (user?.avatar_url?.isEmpty() == true) {
            } else {
                Image(
                    painter = rememberAsyncImagePainter(user?.avatar_url), // Assuming the URL of the avatar
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

        }
    }
}


fun getAddressFromLatLng(
    context: Context, latitude: Double, longitude: Double
): String {
    val geocoder = Geocoder(context)
    val addressList = geocoder.getFromLocation(latitude, longitude, 1)
    return if (addressList != null && addressList.isNotEmpty()) {
        val address = addressList[0]
        "${address.getAddressLine(0)}, ${address.locality}, ${address.adminArea}, ${address.countryName}"
    } else {
        "Address not found"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListBottomSheet(
    sharedViewModel: SharedViewModel,
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    loginUser: LoginUser?
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss, modifier = Modifier.fillMaxSize()
        ) {
            val tempUserList =
                loginUser?.friendList?.filter { FriendState.FRIEND == it.friendState }

            if (loginUser == null || tempUserList?.isEmpty() == true) {
                Text(text = "No friends available", modifier = Modifier.padding(16.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "${loginUser.name}'s Friends",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp, color = Color.LightGray
                    )
                    FriendListView(sharedViewModel, loginUser.friendList)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun FriendListView(sharedViewModel: SharedViewModel, friendList: List<FriendModel>?) {
    var selectedFriendIndex by remember { mutableStateOf<Int?>(null) }

    Column {
        friendList?.forEachIndexed { index, friend ->
            FriendItem(
                friend = friend, isSelected = selectedFriendIndex == index, onClick = {
                    sharedViewModel.setFriend(friend.userID)
                    selectedFriendIndex = if (selectedFriendIndex == index) null else index
                })
        }
    }
}

@Composable
fun FriendItem(friend: FriendModel, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clickable { onClick() }
            .background(
                color = if (isSelected) Color.LightGray else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberAsyncImagePainter(friend.friendAvatarUrl), // Assuming the URL of the avatar
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = friend.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }


}
