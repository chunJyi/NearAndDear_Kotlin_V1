package com.tc.nearanddear.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.location.Geocoder
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.maps.GoogleMapOptions
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
    val defaultZoom = 5f;

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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load user data.")
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
                        Icon(Icons.Filled.Star, contentDescription = "Show Friends")
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

//@Composable
//fun GoogleMapView(
//    sharedViewModel: SharedViewModel,
//    cameraPositionState: CameraPositionState,
//    zoomLevel: Float,
//    onMapClick: (LatLng) -> Unit,
//) {
//    val context = LocalContext.current
//    val selectedUser = sharedViewModel.selectedUser
//    val lat = selectedUser?.location_model?.latitude ?: "0.0"
//    val lon = selectedUser?.location_model?.longitude ?: "0.0"
//    val userLatLng = LatLng(lat.toDouble(), lon.toDouble())
//
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
//    }
//
//    val markerState = rememberMarkerState(position = userLatLng)
//
//    val bitmapDescriptor = remember {
//        val drawable = ContextCompat.getDrawable(context, R.drawable.marker)
//        val bitmap = drawable?.let {
//            createBitmap(96, 96).also { bmp ->
//                val canvas = Canvas(bmp)
//                it.setBounds(0, 0, canvas.width, canvas.height)
//                it.draw(canvas)
//            }
//        }
//        bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
//    }
//
//    val uiSettings = MapUiSettings(
//        zoomControlsEnabled = true,
//        compassEnabled = true,
//        scrollGesturesEnabled = true,
//        tiltGesturesEnabled = false,
//        myLocationButtonEnabled = true,
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        val googleMapOptions = GoogleMapOptions().apply {
//            mapType(MapType.HYBRID.value)
//            CameraPosition.builder().target(
//                LatLng(
//                    sharedViewModel.selectedUser?.location_model?.latitude?.toDouble() ?: 0.0,
//                    sharedViewModel.selectedUser?.location_model?.longitude?.toDouble() ?: 0.0
//                )
//            ).zoom(zoomLevel).build();
//        }
//        GoogleMap(
//            cameraPositionState = cameraPositionState,
//            modifier = Modifier.fillMaxSize(),
//            googleMapOptionsFactory = { googleMapOptions },
//            properties = MapProperties(
//                isBuildingEnabled = true, isMyLocationEnabled = true
//            ), // Enable the "blue dot"
//            onMapClick = onMapClick,
//            uiSettings = uiSettings
//        ) {
//            val latLng = LatLng(
//                sharedViewModel.selectedUser?.location_model?.latitude?.toDouble() ?: 0.0,
//                sharedViewModel.selectedUser?.location_model?.longitude?.toDouble() ?: 0.0
//            )
//            Marker(
//                state = MarkerState(position = latLng),
//                title = sharedViewModel.selectedUser?.name ?: "Selected User"
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .width(200.dp)
//                .padding(10.dp)
//                .align(Alignment.TopStart)
//                .background(Color(0xAA909090), shape = RoundedCornerShape(12.dp))
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//        ) {
//            val address = getAddressFromLatLng(
//                context,
//                selectedUser?.location_model?.latitude?.toDouble() ?: 0.0,
//                selectedUser?.location_model?.longitude?.toDouble() ?: 0.0
//            )
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.LocationOn,
//                    contentDescription = "Location",
//                    tint = Color.Cyan,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(
//                    text = (selectedUser?.location_model?.updatedAt ?: "No Updated") + address,
//                    color = Color.White,
//                    fontSize = 16.sp
//                )
//            }
//        }
//    }
//}

@Composable
fun GoogleMapView(
    viewModel: SharedViewModel,
    cameraPositionState: CameraPositionState,
    zoomLevel: Float,
    onMapClick: (LatLng) -> Unit,
) {
    val context = LocalContext.current

    // Compute LatLng once
    val latLng = viewModel.selectedUser?.location_model?.let {
        LatLng(it.latitude.toDoubleOrNull() ?: 0.0, it.longitude.toDoubleOrNull() ?: 0.0)
    } ?: LatLng(0.0, 0.0)

    // Custom marker icon
    val bitmapDescriptor = remember(context) {
        ContextCompat.getDrawable(context, R.drawable.marker)?.let { drawable ->
            createBitmap(96, 96).also { bitmap ->
                Canvas(bitmap).apply {
                    drawable.setBounds(0, 0, width, height)
                    drawable.draw(this)
                }
            }
        }?.let { BitmapDescriptorFactory.fromBitmap(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true,
                isBuildingEnabled = true,
                mapType = MapType.HYBRID // Configurable via parameter if needed
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
            Marker(
                state = MarkerState(position = latLng),
                title = viewModel.selectedUser?.name ?: "Selected User",
                icon = bitmapDescriptor
            )
            LaunchedEffect(Unit) {
                MarkerState(position = latLng).showInfoWindow() // Always show the info window on start
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

@Composable
private fun LocationInfoCard(
    context: Context,
    user: LoginUser?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAA909090))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.Cyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildString {
                    append(user?.location_model?.updatedAt ?: "No Update")
//                    append(" ")
//                    append(
//                        user?.location_model?.let {
//                            getAddressFromLatLng(
//                                context,
//                                it.latitude.toDoubleOrNull() ?: 0.0,
//                                it.longitude.toDoubleOrNull() ?: 0.0
//                            )
//                        } ?: "Address not found"
//                    )
                },
                color = Color.White,
                fontSize = 16.sp
            )
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
            painter = rememberAsyncImagePainter("https://ui-avatars.com/api/?background=random"),
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
