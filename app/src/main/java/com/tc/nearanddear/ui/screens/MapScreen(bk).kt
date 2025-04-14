//package com.tc.nearanddear.ui.screens
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.google.android.gms.maps.GoogleMapOptions
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.MapProperties
//import com.google.maps.android.compose.MapType
//import com.google.maps.android.compose.MapUiSettings
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.rememberCameraPositionState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import com.tc.nearanddear.model.FriendModel
//import com.tc.nearanddear.session.UserSession
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MapScreenbk() {
//    // State to control bottom sheet visibility
//    var showBottomSheet by remember { mutableStateOf(false) }
//
//    // Remember the CameraPosition state
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f)
//    }
//
//    // Define custom GoogleMapOptions
//    val googleMapOptions = GoogleMapOptions().apply {
//        mapType(MapType.HYBRID.value)
//    }
//
//    // Define custom UI settings
//    val uiSettings = MapUiSettings(
//        zoomControlsEnabled = true,
//        compassEnabled = true,
//        scrollGesturesEnabled = true,
//        tiltGesturesEnabled = false,
//        myLocationButtonEnabled = true
//    )
//
//    // Box to layer FAB over the map
//    Box(modifier = Modifier.fillMaxSize()) {
//        // GoogleMap composable
//        GoogleMap(
//            cameraPositionState = cameraPositionState,
//            modifier = Modifier.fillMaxSize(),
//            googleMapOptionsFactory = { googleMapOptions },
//            properties = MapProperties(isBuildingEnabled = true),
//            uiSettings = uiSettings,
//            contentPadding = PaddingValues(16.dp),
//            onMapClick = { latLng ->
//                println("Map clicked at: $latLng")
//            },
//            onMapLongClick = { latLng ->
//                println("Map long clicked at: $latLng")
//            }
//        ) {
//            Marker(
//                state = MarkerState(position = LatLng(37.7749, -122.4194)),
//                title = "San Francisco"
//            )
//        }
//
//        // Floating Action Button
//        FloatingActionButton(
//            onClick = { showBottomSheet = true },
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Filled.Star, contentDescription = "Show Friends")
//        }
//    }
//
//    // Bottom Sheet Dialog
//    if (showBottomSheet) {
//        ModalBottomSheet(
//            onDismissRequest = { showBottomSheet = false },
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Bottom sheet content: Friend list
//            val loginUser = UserSession.loginUser
//            if (loginUser == null || loginUser.friendList?.isEmpty() == true) {
//                Text(
//                    text = "No friends available",
//                    modifier = Modifier.padding(16.dp)
//                )
//            } else {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState())
//                ) {
//                    Text(
//                        text = "${loginUser.name}'s Friends",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    loginUser.friendList?.forEach { friend ->
//                        FriendItem(friend = friend)
//                    }
//                }
//            }
//            // Close button
//            Button(
//                onClick = { showBottomSheet = false },
//                modifier = Modifier
//                    .padding(16.dp)
//                    .align(Alignment.CenterHorizontally)
//            ) {
//                Text("Close")
//            }
//        }
//    }
//}
//
//@Composable
//fun FriendItem(friend: FriendModel) {
//    // Simple UI for each friend
//    Text(
//        text = "${friend.name}",
//        modifier = Modifier.padding(vertical = 4.dp)
//    )
//}
//
//
////package com.tc.nearanddear.ui.screens
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.layout.Box
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.PaddingValues
////import androidx.compose.foundation.layout.Row
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.fillMaxWidth
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.layout.size
////import androidx.compose.foundation.shape.CircleShape
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.Add
////import androidx.compose.material3.BottomSheetScaffold
////import androidx.compose.material3.ExperimentalMaterial3Api
////import androidx.compose.material3.Icon
////import androidx.compose.material3.IconButton
////import androidx.compose.material3.MaterialTheme
////import androidx.compose.material3.Text
////import androidx.compose.material3.rememberBottomSheetScaffoldState
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.text.style.TextOverflow
////import androidx.compose.ui.unit.dp
////import com.google.android.gms.maps.GoogleMapOptions
////import com.google.android.gms.maps.model.BitmapDescriptorFactory
////import com.google.android.gms.maps.model.CameraPosition
////import com.google.android.gms.maps.model.LatLng
////import com.google.maps.android.compose.CameraPositionState
////import com.google.maps.android.compose.GoogleMap
////import com.google.maps.android.compose.MapProperties
////import com.google.maps.android.compose.MapType
////import com.google.maps.android.compose.MapUiSettings
////import com.google.maps.android.compose.rememberCameraPositionState
////import com.tc.nearanddear.model.FriendModel
////import com.tc.nearanddear.session.UserSession
////import androidx.compose.foundation.verticalScroll
////import androidx.compose.foundation.rememberScrollState
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun MapScreen() {
////    // CameraPosition state
////    val cameraPositionState = rememberCameraPositionState {
////        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f)
////    }
////
////    // GoogleMap options
////    val googleMapOptions = GoogleMapOptions().apply {
////        mapType(MapType.NORMAL.value) // Match screenshot's map style
////    }
////
////    // UI settings (disable controls like screenshot)
////    val uiSettings = MapUiSettings(
////        zoomControlsEnabled = false,
////        compassEnabled = false,
////        scrollGesturesEnabled = true,
////        tiltGesturesEnabled = false,
////        myLocationButtonEnabled = false
////    )
////
////    // Bottom sheet scaffold state
////    val scaffoldState = rememberBottomSheetScaffoldState()
////
////    // BottomSheetScaffold for persistent bottom sheet
////    BottomSheetScaffold(
////        scaffoldState = scaffoldState,
////        sheetPeekHeight = 120.dp, // Initial height (shows header + 1-2 items)
////        sheetContent = {
////            BottomSheetContent(cameraPositionState)
////        },
////        sheetContainerColor = MaterialTheme.colorScheme.surface,
////        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
////        modifier = Modifier.fillMaxSize()
////    ) {
////        // Map content
////        Box(modifier = Modifier.fillMaxSize()) {
////            GoogleMap(
////                cameraPositionState = cameraPositionState,
////                modifier = Modifier.fillMaxSize(),
////                googleMapOptionsFactory = { googleMapOptions },
////                properties = MapProperties(isBuildingEnabled = true),
////                uiSettings = uiSettings,
////                contentPadding = PaddingValues(16.dp),
////                onMapClick = { latLng ->
////                    println("Map clicked at: $latLng")
////                },
////                onMapLongClick = { latLng ->
////                    println("Map long clicked at: $latLng")
////                }
////            ) {
////                // Add markers for each friend
////                val loginUser = UserSession.loginUser
////                loginUser?.friendList?.forEach { friend ->
////                    friend.let { latLng ->
//////                        Marker(
//////                            state = MarkerState(position = latLng),
//////                            title = friend.name,
//////                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE), // Placeholder for avatar
//////                            onClick = {
//////                                // Optionally center map on friend
//////                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
//////                                true
//////                            }
//////                        )
////                    }
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun BottomSheetContent(cameraPositionState: CameraPositionState) {
////    val loginUser = UserSession.loginUser
////    Column(
////        modifier = Modifier
////            .fillMaxWidth()
////            .padding(horizontal = 16.dp)
////            .verticalScroll(rememberScrollState())
////    ) {
////        // Header
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 8.dp),
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Text(
////                text = "People",
////                style = MaterialTheme.typography.titleLarge,
////                modifier = Modifier.weight(1f)
////            )
////            IconButton(onClick = { /* TODO: Handle add friend */ }) {
////                Icon(Icons.Filled.Add, contentDescription = "Add Friend")
////            }
////        }
////
////        // Friend list
////        if (loginUser == null || loginUser.friendList?.isEmpty() == true) {
////            Text(
////                text = "No friends available",
////                modifier = Modifier.padding(vertical = 16.dp)
////            )
////        } else {
////            loginUser.friendList?.forEach { friend ->
////                FriendItem(
////                    friend = friend,
////                    onClick = {
//////                        friend.let { latLng ->
//////                            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
//////                        }
////                    }
////                )
////            }
////        }
////    }
////}
////
////@Composable
////fun FriendItem(friend: FriendModel, onClick: () -> Unit) {
////    Row(
////        modifier = Modifier
////            .fillMaxWidth()
////            .padding(vertical = 8.dp)
////            .clickable { onClick() },
////        verticalAlignment = Alignment.CenterVertically
////    ) {
////        // Avatar placeholder
////        Box(
////            modifier = Modifier
////                .size(40.dp)
////                .clip(CircleShape)
////                .background(Color.Gray),
////            contentAlignment = Alignment.Center
////        ) {
////            Text(
////                text = friend.name.firstOrNull()?.uppercase() ?: "?", // First letter of name
////                color = Color.White,
////                style = MaterialTheme.typography.bodyMedium
////            )
////        }
////
////        // Friend details
////        Column(
////            modifier = Modifier
////                .padding(start = 12.dp)
////                .weight(1f)
////        ) {
////            Text(
////                text = friend.name,
////                style = MaterialTheme.typography.bodyLarge,
////                maxLines = 1,
////                overflow = TextOverflow.Ellipsis
////            )
////            Text(
////                text =  "Unknown location",
////                style = MaterialTheme.typography.bodySmall,
////                color = MaterialTheme.colorScheme.onSurfaceVariant
////            )
////        }
////
////        // Distance
////        Text(
////            text =  "N/A",
////            style = MaterialTheme.typography.bodyMedium,
////            color = MaterialTheme.colorScheme.onSurfaceVariant
////        )
////    }
////}
//
////package com.tc.nearanddear.ui.screens
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.layout.Box
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.Row
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.fillMaxWidth
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.layout.size
////import androidx.compose.foundation.rememberScrollState
////import androidx.compose.foundation.shape.CircleShape
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.foundation.verticalScroll
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.Add
////import androidx.compose.material3.BottomSheetScaffold
////import androidx.compose.material3.ExperimentalMaterial3Api
////import androidx.compose.material3.Icon
////import androidx.compose.material3.IconButton
////import androidx.compose.material3.MaterialTheme
////import androidx.compose.material3.Text
////import androidx.compose.material3.rememberBottomSheetScaffoldState
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.rememberCoroutineScope
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.platform.LocalConfiguration
////import androidx.compose.ui.platform.LocalDensity
////import androidx.compose.ui.text.style.TextOverflow
////import androidx.compose.ui.unit.dp
////import com.google.android.gms.maps.GoogleMapOptions
////import com.google.android.gms.maps.model.BitmapDescriptorFactory
////import com.google.android.gms.maps.model.CameraPosition
////import com.google.android.gms.maps.model.LatLng
////import com.google.maps.android.compose.CameraPositionState
////import com.google.maps.android.compose.GoogleMap
////import com.google.maps.android.compose.MapProperties
////import com.google.maps.android.compose.MapType
////import com.google.maps.android.compose.MapUiSettings
////import com.google.maps.android.compose.Marker
////import com.google.maps.android.compose.MarkerState
////import com.google.maps.android.compose.rememberCameraPositionState
////import com.tc.nearanddear.model.FriendModel
////import com.tc.nearanddear.session.UserSession
////import kotlinx.coroutines.launch
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun MapScreen() {
////    // CameraPosition state
////    val cameraPositionState = rememberCameraPositionState {
////        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f)
////    }
////
////    // GoogleMap options
////    val googleMapOptions = GoogleMapOptions().apply {
////        mapType(MapType.NORMAL.value)
////    }
////
////    // UI settings
////    val uiSettings = MapUiSettings(
////        zoomControlsEnabled = false,
////        compassEnabled = false,
////        scrollGesturesEnabled = true,
////        tiltGesturesEnabled = false,
////        myLocationButtonEnabled = false
////    )
////
////    // Bottom sheet scaffold state
////    val scaffoldState = rememberBottomSheetScaffoldState()
////    val coroutineScope = rememberCoroutineScope()
////
////    // Screen height and three states
////    val configuration = LocalConfiguration.current
////    val screenHeightDp = configuration.screenHeightDp.dp
////    val collapsedHeight = 60.dp // Just the header
////    val partiallyExpandedHeight = screenHeightDp / 3 // 1/3 of the screen
////    val fullyExpandedHeight = screenHeightDp // Full screen
////
////    // BottomSheetScaffold with three states
////    BottomSheetScaffold(
////        scaffoldState = scaffoldState,
////        sheetPeekHeight = collapsedHeight, // Collapsed state
////        sheetContent = {
////            BottomSheetContent(
////                cameraPositionState = cameraPositionState,
////                partiallyExpandedHeight = partiallyExpandedHeight,
////                fullyExpandedHeight = fullyExpandedHeight,
////                onHeightChange = { targetHeight ->
////                    coroutineScope.launch {
////                        if (targetHeight == fullyExpandedHeight) {
////                            scaffoldState.bottomSheetState.expand()
////                        } else {
////                            scaffoldState.bottomSheetState.partialExpand()
////                        }
////                    }
////                }
////            )
////        },
////        sheetContainerColor = MaterialTheme.colorScheme.surface,
////        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
////        modifier = Modifier.fillMaxSize()
////    ) {
////        // Map content
////        Box(modifier = Modifier.fillMaxSize()) {
////            GoogleMap(
////                cameraPositionState = cameraPositionState,
////                modifier = Modifier.fillMaxSize(),
////                googleMapOptionsFactory = { googleMapOptions },
////                properties = MapProperties(isBuildingEnabled = true),
////                uiSettings = uiSettings,
////                onMapClick = { latLng ->
////                    println("Map clicked at: $latLng")
////                },
////                onMapLongClick = { latLng ->
////                    println("Map long clicked at: $latLng")
////                }
////            ) {
////                // Add markers for each friend
////                val loginUser = UserSession.loginUser
////                loginUser?.friendList?.forEach { friend ->
//////                    friend.location?.let { latLng ->
//////                        Marker(
//////                            state = MarkerState(position = latLng),
//////                            title = friend.name,
//////                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
//////                            onClick = {
//////                                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
//////                                true
//////                            }
//////                        )
//////                    }
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun BottomSheetContent(
////    cameraPositionState: CameraPositionState,
////    partiallyExpandedHeight: androidx.compose.ui.unit.Dp,
////    fullyExpandedHeight: androidx.compose.ui.unit.Dp,
////    onHeightChange: (androidx.compose.ui.unit.Dp) -> Unit
////) {
////    val loginUser = UserSession.loginUser
////    val coroutineScope = rememberCoroutineScope()
////
////    Column(
////        modifier = Modifier
////            .fillMaxWidth()
////            .padding(horizontal = 16.dp)
////            .verticalScroll(rememberScrollState())
////    ) {
////        // Header
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(vertical = 8.dp),
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Text(
////                text = "People",
////                style = MaterialTheme.typography.titleLarge,
////                modifier = Modifier.weight(1f)
////            )
////            IconButton(onClick = { /* TODO: Handle add friend */ }) {
////                Icon(Icons.Filled.Add, contentDescription = "Add Friend")
////            }
////        }
////
////        // Friend list
////        if (loginUser == null || loginUser.friendList?.isEmpty() == true) {
////            Text(
////                text = "No friends available",
////                modifier = Modifier.padding(vertical = 16.dp)
////            )
////        } else {
////            loginUser.friendList?.forEach { friend ->
////                FriendItem(
////                    friend = friend,
////                    onClick = {
//////                        friend.location?.let { latLng ->
//////                            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
//////                            // Expand to partially expanded or fully expanded on click
//////                            coroutineScope.launch {
//////                                onHeightChange(partiallyExpandedHeight)
//////                            }
//////                        }
////                    }
////                )
////            }
////        }
////    }
////}
////
////@Composable
////fun FriendItem(friend: FriendModel, onClick: () -> Unit) {
////    Row(
////        modifier = Modifier
////            .fillMaxWidth()
////            .padding(vertical = 8.dp)
////            .clickable { onClick() },
////        verticalAlignment = Alignment.CenterVertically
////    ) {
////        // Avatar placeholder
////        Box(
////            modifier = Modifier
////                .size(40.dp)
////                .clip(CircleShape)
////                .background(Color.Gray),
////            contentAlignment = Alignment.Center
////        ) {
////            Text(
////                text = friend.name.firstOrNull()?.uppercase() ?: "?",
////                color = Color.White,
////                style = MaterialTheme.typography.bodyMedium
////            )
////        }
////
////        // Friend details
////        Column(
////            modifier = Modifier
////                .padding(start = 12.dp)
////                .weight(1f)
////        ) {
////            Text(
////                text = friend.name,
////                style = MaterialTheme.typography.bodyLarge,
////                maxLines = 1,
////                overflow = TextOverflow.Ellipsis
////            )
////            Text(
////                text = "Unknown location",
////                style = MaterialTheme.typography.bodySmall,
////                color = MaterialTheme.colorScheme.onSurfaceVariant
////            )
////        }
////
////        // Distance
////        Text(
////            text = "N/A",
////            style = MaterialTheme.typography.bodyMedium,
////            color = MaterialTheme.colorScheme.onSurfaceVariant
////        )
////    }
////}