package com.tc.nearanddear.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import com.tc.nearanddear.model.LocationModel
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.session.SharedViewModel
import com.tc.nearanddear.session.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(sharedViewModel: SharedViewModel = viewModel()) {
    var selectedID = sharedViewModel.selectedFriend;
    if (selectedID?.isEmpty() == true) {
        return
    }

    // TODO: fetch loginUser from supabase by id 
    LaunchedEffect(Unit) {
        if(selectedID?.isEmpty() == false){
            val user = getUserById(selectedID)
            if (user != null) {
                sharedViewModel.setUser(user)
            } else {
                println("User not found or error occurred.")
            }
        }

    }

    sharedViewModel.clearFriend();


    // State to control bottom sheet visibility
    var showBottomSheet by remember { mutableStateOf(false) }

    // Remember the CameraPosition state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                sharedViewModel.selectedUser?.location_model?.latitude ?: 0.0,  // Default to 0.0 if latitude is null
                sharedViewModel.selectedUser?.location_model?.longitude ?: 0.0   // Default to 0.0 if longitude is null
            ),
            10f
        )
    }

    // Define custom GoogleMapOptions
    val googleMapOptions = GoogleMapOptions().apply {
        mapType(MapType.HYBRID.value)
    }

    // Define custom UI settings
    val uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        compassEnabled = true,
        scrollGesturesEnabled = true,
        tiltGesturesEnabled = false,
        myLocationButtonEnabled = true
    )

    // Box to layer FAB over the map
    Box(modifier = Modifier.fillMaxSize()) {
        // GoogleMap composable
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize(),
            googleMapOptionsFactory = { googleMapOptions },
            properties = MapProperties(isBuildingEnabled = true),
            uiSettings = uiSettings,
            contentPadding = PaddingValues(16.dp),
            onMapClick = { latLng ->
                println("Map clicked at: $latLng")
            },
            onMapLongClick = { latLng ->
                println("Map long clicked at: $latLng")
            }
        ) {
            Marker(
                state = MarkerState(position = LatLng(37.7749, -122.4194)),
                title = "San Francisco"
            )
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = "Show Friends")
        }
    }

    // Bottom Sheet Dialog
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier.fillMaxSize()
        ) {
            // Bottom sheet content: Friend list
            val loginUser = UserSession.loginUser
            if (loginUser == null || loginUser.friendList?.isEmpty() == true) {
                Text(
                    text = "No friends available",
                    modifier = Modifier.padding(16.dp)
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
                    loginUser.friendList?.forEach { friend ->
                        FriendItem(friend = friend)
                    }
                }
            }
            // Close button
            Button(
                onClick = { showBottomSheet = false },
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
fun FriendItem(friend: FriendModel) {
    // Simple UI for each friend
    Text(
        text = "${friend.name}",
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

suspend fun getUserById(userId: String): LoginUser? {
    return withContext(Dispatchers.IO) {
        try {
            // Query the database for a user with the given userID, selecting only the "name" column
            val result = client.from("loginUser")
                .select(columns = Columns.list(listOf("name", "location_model"))) {
                    filter { eq("userID", userId) }
                }
                .decodeList<Map<String, Any>>()
                .firstOrNull()

            // Map the result to a LoginUser object
            result?.let { userMap ->
                LoginUser(
                    userID = userId, // Use the input userId since it's not fetched
                    name = userMap["name"] as String,
                    location_model = userMap["location_model"] as LocationModel,
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
}
