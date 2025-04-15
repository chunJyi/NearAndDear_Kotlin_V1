package com.tc.nearanddear.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.maps.android.compose.CameraPositionState
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.model.LoginUserLite
import com.tc.nearanddear.session.SharedViewModel
import com.tc.nearanddear.session.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay

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

    LaunchedEffect(selectedID) {
        selectedID?.takeIf { it.isNotEmpty() }?.let { id ->
            mapScreenState = MapScreenState.Loading
            while (true) {
                try {
                    val user = getUserById(id)
                    if (user != null) {
                        sharedViewModel.setUser(user)

                        val cameraPosition = CameraPosition.fromLatLngZoom(
                            LatLng(
                                user.location_model?.latitude ?: 0.0,
                                user.location_model?.longitude ?: 0.0
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

    // Render UI based on state
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with back and settings buttons
        TopAppBar(
            title = { }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() /* Handle back navigation */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }, actions = {
                IconButton(onClick = { /* Handle settings action */ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Settings")
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF000000).copy(alpha = 0.1f) // 10% opacity
               ), modifier = Modifier.height(40.dp) // Custom height for the TopAppBar
        )

        // Bottom Sheet
        FriendListBottomSheet(
            sharedViewModel = sharedViewModel,
            showBottomSheet = showBottomSheet,
            onDismiss = { showBottomSheet = false },
            loginUser = UserSession.loginUser
        )

        when (val state = mapScreenState) {
            is MapScreenState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading user data...")
                }
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
                    GoogleMapView(sharedViewModel, cameraPositionState) { latLng ->
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


suspend fun getUserById(userId: String): LoginUser? {
    return try {
        // Query the database for a user with the given userID, selecting only the "name" column
        val result = client.from("loginUser")
            .select(columns = Columns.list(listOf("name", "location_model"))) {
                filter { eq("userID", userId) }
            }.decodeList<LoginUserLite>().firstOrNull()

        result?.let { data ->
            LoginUser(
                userID = userId, // Use the input userId since it's not fetched
                name = data.name,
                location_model = data.location_model,
                id = 0,
                created_at = "lsdkjf",
                email = "sldkjf",
                avatar_url = "sdlkfj",
                updated_at = "lsdkjf",
                friendList = emptyList(),
            )
        }
    } catch (e: Exception) {
        // Log the error and return null
        println("Error fetching user: ${e.message}")
        null
    }
}

@Composable
fun GoogleMapView(
    sharedViewModel: SharedViewModel,
    cameraPositionState: CameraPositionState,
    onMapClick: (LatLng) -> Unit
) {
    // Define the initial zoom level you want to set by default
    val defaultZoom = 10f // This can be adjusted as needed

    // Define custom GoogleMapOptions
    val googleMapOptions = GoogleMapOptions().apply {
        mapType(MapType.HYBRID.value)
        // Set the initial zoom level to default zoom
        CameraPosition.builder().target(
            LatLng(
                sharedViewModel.selectedUser?.location_model?.latitude ?: 0.0,
                sharedViewModel.selectedUser?.location_model?.longitude ?: 0.0
            )
        ).zoom(defaultZoom).build();
    }

    // Define custom UI settings
    val uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        compassEnabled = true,
        scrollGesturesEnabled = true,
        tiltGesturesEnabled = false,
        myLocationButtonEnabled = true,
    )

    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize(),
        googleMapOptionsFactory = { googleMapOptions },
        properties = MapProperties(
            isBuildingEnabled = true, isMyLocationEnabled = true
        ), // Enable the "blue dot"
        onMapClick = onMapClick,
        uiSettings = uiSettings
    ) {
        val latLng = LatLng(
            sharedViewModel.selectedUser?.location_model?.latitude ?: 0.0,
            sharedViewModel.selectedUser?.location_model?.longitude ?: 0.0
        )
        Marker(
            state = MarkerState(position = latLng),
            title = sharedViewModel.selectedUser?.name ?: "Selected User"
        )
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
            // Bottom sheet content: Friend list
            if (loginUser == null || loginUser.friendList?.isEmpty() == true) {
                Text(
                    text = "No friends available", modifier = Modifier.padding(16.dp)
                )
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
            // Close button
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
                    if (selectedFriendIndex == index) null else index

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
        // Profile picture (you can use a placeholder or an image)
        Image(
            painter = rememberAsyncImagePainter("https://ui-avatars.com/api/?background=random"),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(5.dp))

        // Friend's name
        Text(
            text = friend.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}

data class Friend(
    val name: String, val profileImageUrl: String // URL or resource ID for the profile picture
)




