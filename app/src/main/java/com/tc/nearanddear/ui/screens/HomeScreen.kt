package com.tc.nearanddear.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tc.nearanddear.model.*
import com.tc.nearanddear.session.UserSession
import com.tc.nearanddear.session.UserSession.loginUser

@Composable
fun HomeScreen() {
    val user = UserSession.loginUser ?: loginUser
    val selectedTab = remember { mutableStateOf(0) }
    val tabs = listOf("Friends", "Request", "Pending")

    Column(
        modifier = Modifier
            .fillMaxSize()
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
//        UserProfile(user)
        Spacer(Modifier.height(16.dp))
        FriendsStoryRow()
        Spacer(Modifier.height(16.dp))
        FriendCard(tabs, selectedTab.value) { selectedTab.value = it }
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
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Text("STOP", fontSize = 14.sp)
        }
    }
}

@Composable
private fun UserProfile(user: LoginUser?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user?.name ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "${user?.location_model?.latitude ?: "NA"} / ${user?.location_model?.longitude ?: "NA"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text("Yangon", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun FriendsStoryRow() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            repeat(6) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_search),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text("Title", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun FriendCard(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FriendsTabs(tabs, selectedIndex, onTabSelected)
            Spacer(Modifier.height(16.dp))
            FriendListHeader(tabs[selectedIndex])
            Spacer(Modifier.height(8.dp))
            FriendList(
                when (selectedIndex) {
                    0 -> loginUser?.friendList?.filter { it.friendState == FriendState.FRIEND }
                    1 -> loginUser?.friendList?.filter { it.friendState == FriendState.REQUEST }
                    else -> loginUser?.friendList?.filter { it.friendState == FriendState.PENDING }
                }
            )
        }
    }
}

@Composable
private fun FriendsTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8ECEB)),
        contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .background(if (selectedTab == index) Color(0xFF2563EB) else Color.Transparent)
                    .clip(RoundedCornerShape(8.dp)),
                text = {
                    Text(
                        title,
                        color = if (selectedTab == index) Color.White else Color.Black,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun FriendListHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$title List", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Add", fontSize = 14.sp)
        }
    }
}

@Composable
private fun FriendList(friends: List<FriendModel>?) {
    var expandedIndex by remember { mutableStateOf(-1) }

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
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.friendState) {
                            FriendState.FRIEND -> Icons.Default.Favorite
                            FriendState.REQUEST, FriendState.PENDING -> Icons.Default.Star
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = when (item.friendState) {
                            FriendState.FRIEND -> Color(0xFFF44336)
                            FriendState.REQUEST -> Color(0xFFFFA000)
                            FriendState.PENDING -> Color(0xFF2563EB)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.userName, fontWeight = FontWeight.Bold)
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
                        "Extra actions or details here...",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(user: LoginUser?) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user?.name ?: "Unknown User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            val lat = user?.location_model?.latitude ?: "NA"
            val lon = user?.location_model?.longitude ?: "NA"
            Text(text = "$lat / $lon", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Yangon", fontSize = 14.sp, color = Color.Gray)
        }
    }
}


@Composable
fun UserLocationMapCard(user: LoginUser?) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Replace this with a real MapView or Compose Map if you're using Google Maps Compose
            Text(
                text = "Map: ${user?.location_model?.latitude} / ${user?.location_model?.longitude}",
                modifier = Modifier.align(Alignment.Center),
                color = Color.DarkGray
            )
        }
    }
}

