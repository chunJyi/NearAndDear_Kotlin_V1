package com.tc.nearanddear.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tc.nearanddear.model.*
import com.tc.nearanddear.session.UserSession
import com.tc.nearanddear.session.UserSession.loginUser
import com.tc.nearanddear.R
import com.tc.nearanddear.session.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController, context: Context, sharedViewModel: SharedViewModel) {
    val user = UserSession.loginUser ?: loginUser
    val selectedTab = remember { mutableStateOf(0) }
    val tabs = listOf("Friends", "Request", "Pending")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEEF3)) // <-- Background color here
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Header()
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            UserProfileCard(user)
            Spacer(modifier = Modifier.width(12.dp))
            UserLocationMapCard(user)
        }
        Spacer(Modifier.height(16.dp))
        FriendsStoryRow(user?.friendList)
        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(16.dp))
        FriendCard(navController, context, sharedViewModel, tabs, selectedTab.value) {
            selectedTab.value = it
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Near & Dear", fontSize = 30.sp, fontFamily = FontFamily.Cursive)
        Button(
            onClick = {}, colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red, contentColor = Color.White
            ), modifier = Modifier.height(40.dp)
        ) {
            Text("STOP", fontSize = 14.sp)
        }
    }
}

@Composable
fun UserProfileCard(user: LoginUser?) {
    // Define the gradient brush
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1934), Color(0xFF1F0812)) // Example colors (Green shades)
    )

    Card(
        modifier = Modifier
            .width(310.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Make container transparent to show gradient
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient) // Apply gradient as background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = user?.name ?: "Unknown User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                val lat = user?.location_model?.latitude ?: "NA"
                val lon = user?.location_model?.longitude ?: "NA"
                Text(text = "$lat / $lon", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Yangon", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun UserLocationMapCard(user: LoginUser?) {
    val lat = user?.location_model?.latitude ?: 0.0
    val lon = user?.location_model?.longitude ?: 0.0
    val userLatLng = LatLng(lat, lon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = userLatLng),
                title = user?.name ?: "You",
                snippet = "Current Location"
            )
        }
    }
}

@Composable
private fun FriendsStoryRow(friendList: List<FriendModel>?) {
    // Filter friends with friendState "FRIEND"
    val friends = friendList?.filter { FriendState.FRIEND.equals(it.friendState) } ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Horizontal scrollable row to show friend images
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            // Iterate through the filtered friends and display them
            friends.take(6).forEachIndexed { index, friend ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .widthIn(max = 60.dp) // Flexible width but constrained to a max of 80.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_photo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        friend.name, fontSize = 12.sp, maxLines = 1, // Limit to 1 line
                        overflow = TextOverflow.Ellipsis, // Add ellipsis when text overflows
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun FriendCard(
    navController: NavController,
    context: Context,
    sharedViewModel: SharedViewModel,
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFD)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FriendsTabs(tabs, selectedIndex, onTabSelected)
            Spacer(Modifier.height(8.dp))
            FriendListHeader(navController, tabs[selectedIndex])
            Spacer(Modifier.height(8.dp))
            when (selectedIndex) {
                0 -> FriendList(
                    navController,
                    sharedViewModel,
                    loginUser?.friendList?.filter { it.friendState == FriendState.FRIEND })

                1 -> RequestList(
                    context,
                    loginUser?.friendList?.filter { it.friendState == FriendState.REQUEST })

                else -> PendingList(
                    loginUser?.friendList?.filter { it.friendState == FriendState.PENDING })

            }
        }
    }
}

@Composable
private fun FriendsTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab, indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(3.dp),
                color = Color(0xFF2563EB) // Blue indicator
            )
        }, divider = {},   // Remove default divider
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF000000)), // Outer tab row background
        containerColor = Color.Transparent, contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .background(
                        if (selectedTab == index) Color(0xFF2563EB) // Selected tab color
                        else Color(0xFFE0E0E0) // Unselected tab color
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .height(36.dp),
                text = {
                    Text(
                        title,
                        color = if (selectedTab == index) Color.White else Color.Black,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                })
        }
    }
}


@Composable
private fun FriendListHeader(navController: NavController, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$title List", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = { navController.navigate("search") }, colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black, contentColor = Color.White
            ), modifier = Modifier.height(38.dp)
        ) {
            Text("Add", fontSize = 14.sp)
        }
    }
}

@Composable
private fun PendingList(friends: List<FriendModel>?) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        friends?.forEachIndexed { index, item ->
            val isExpanded = expandedIndex == index
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
                    .padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.friendState) {
                            FriendState.FRIEND -> Icons.Default.Favorite
                            FriendState.REQUEST, FriendState.PENDING -> Icons.Default.Star
                            else -> Icons.Default.Person
                        }, contentDescription = null, tint = when (item.friendState) {
                            FriendState.FRIEND -> Color(0xFFF44336)
                            FriendState.REQUEST -> Color(0xFFFFA000)
                            FriendState.PENDING -> Color(0xFF2563EB)
                            else -> Color.Gray
                        }, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("ID: ${item.userID}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Extra actions or details here...", fontSize = 13.sp, color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestList(context: Context, friends: List<FriendModel>?) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        friends?.forEachIndexed { index, item ->
            val isExpanded = expandedIndex == index
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
                    .padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar image
                    Image(
                        painter = rememberAsyncImagePainter("https://ui-avatars.com/api/?name=" + item.name), // Assuming the URL of the avatar
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    // Name and ID
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(formatUserId(item.userID), fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.Gray
                    )

                }
                // Show extra details when expanded
                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { removeFriend(context, item.userID) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD90000))
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(15.dp))

                        Button(
                            onClick = { confirmFriend(context, item.userID) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0542F6)) // Green
                        ) {
                            Text("Confirm")
                        }
                    }

                }
            }
        }
    }
}

private fun confirmFriend(context: Context, friendUserId: String) {

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val currentUserId = UserSession.loginUser?.userID ?: "";
            val currentUserFriendList = fetchUserById(currentUserId)?.friendList
            val friendUserFriendList = fetchUserById(friendUserId)?.friendList

            if (currentUserFriendList == null || friendUserFriendList === null) {
                showToastOnMain(context, "bad request")
                return@launch
            }

            if (!isIncludeFriend(currentUserFriendList, friendUserId)) {
                showToastOnMain(context, "User already in friend list")
                return@launch
            }

            if (!isIncludeFriend(friendUserFriendList, currentUserId)) {
                showToastOnMain(context, "User already in friend list")
                return@launch
            }

            val updatedCurrentList = currentUserFriendList.toMutableList().apply {
                find { it.userID == friendUserId }?.let {
                    it.friendState = FriendState.FRIEND // or whatever state you want
                }
            }

            val updatedFriendList = friendUserFriendList.toMutableList().apply {
                find { it.userID == currentUserId }?.let {
                    it.friendState = FriendState.FRIEND // or whatever state you want
                }
            }

            updateFriendListInDB(currentUserId, updatedCurrentList)
            updateFriendListInDB(friendUserId, updatedFriendList)

            showToastOnMain(context, "Friend added successfully!")
            loginUser = fetchUserById(currentUserId);

        } catch (e: Exception) {
            showToastOnMain(context, "Error adding user: ${e.message}")
        }
    }
}

private fun removeFriend(context: Context, friendUserId: String) {

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val currentUserId = loginUser?.userID ?: "";
            val currentUserFriendList = fetchUserById(currentUserId)?.friendList
            val friendUserFriendList = fetchUserById(friendUserId)?.friendList

            if (currentUserFriendList == null || friendUserFriendList === null) {
                showToastOnMain(context, "bad request")
                return@launch
            }

//            if (!isIncludeFriend(currentUserFriendList, friendUserId)) {
//                showToastOnMain(context, "User does not request in friend list")
//                return@launch
//            }
//
//            if (!isIncludeFriend(friendUserFriendList, currentUserId)) {
//                showToastOnMain(context, "User does not request in friend list")
//                return@launch
//            }

            val updatedCurrentList = currentUserFriendList.toMutableList().apply {
                removeIf { it.userID == friendUserId }
            }

            val updatedFriendList = friendUserFriendList.toMutableList().apply {
                removeIf { it.userID == currentUserId }
            }

            updateFriendListInDB(currentUserId, updatedCurrentList)
            updateFriendListInDB(friendUserId, updatedFriendList)

            showToastOnMain(context, "Friend added successfully!")

            loginUser = fetchUserById(currentUserId);

        } catch (e: Exception) {
            showToastOnMain(context, "Error adding user: ${e.message}")
        }
    }
}

private fun isIncludeFriend(friendList: List<FriendModel>, userIdToCheck: String): Boolean {
    return friendList.any { userIdToCheck == it.userID }
}

fun formatUserId(userId: String): String {
    return if (userId.length <= 10) {
        userId
    } else {
        "${userId.take(5)}*****${userId.takeLast(5)}"
    }
}


@SuppressLint("ContextCastToActivity")
@Composable
private fun FriendList(
    navController: NavController, sharedViewModel: SharedViewModel, friends: List<FriendModel>?
) {
    var selectedFriend by remember { mutableStateOf<FriendModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        friends?.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFriend = item }
                    .padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.friendState) {
                            FriendState.FRIEND -> Icons.Default.Favorite
                            FriendState.REQUEST, FriendState.PENDING -> Icons.Default.Star
                            else -> Icons.Default.Person
                        }, contentDescription = null, tint = when (item.friendState) {
                            FriendState.FRIEND -> Color(0xFFF44336)
                            FriendState.REQUEST -> Color(0xFFFFA000)
                            FriendState.PENDING -> Color(0xFF2563EB)
                            else -> Color.Gray
                        }, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("ID: ${item.userID}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Details",
                        tint = Color.Gray
                    )
                }
            }
        }
    }

    selectedFriend?.let { friend ->

        AlertDialog(
            onDismissRequest = { selectedFriend = null },
            confirmButton = {}, // All UI inside text block
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {

                    // Top-right Close Icon
                    IconButton(
                        onClick = { selectedFriend = null },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Close"
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?background=random",
                            contentDescription = "Friend Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Friend Info
                        Text(friend.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("ID: ${friend.name}", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            "Status: ${friend.friendState.name}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(20.dp))

                        // Action Buttons (Message + Map)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Message Icon
                            IconButton(onClick = {
                                // TODO: Handle message action here
                                selectedFriend = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Message"
                                )
                            }

                            // Map Icon
                            IconButton(onClick = {
                                // Navigate to map page with friend
                                sharedViewModel.setFriend(friend.userID)
                                navController.navigate("map")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Map"
                                )
                            }
                        }
                    }
                }
            })
    }
}



