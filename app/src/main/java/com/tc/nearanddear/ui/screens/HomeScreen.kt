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
import com.tc.nearanddear.model.FriendModel
import com.tc.nearanddear.model.LocationModel
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.model.State

@Composable
fun HomeScreen() {
    val selectedTab = remember { mutableStateOf(0) }
    val tabItems = listOf("Tab 5", "Tab 6", "Tab 7")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Header()
        Spacer(modifier = Modifier.height(16.dp))
        UserProfile()
        Spacer(modifier = Modifier.height(16.dp))
        FriendsStoryRow()
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                FriendsTabs(tabItems, selectedTab.value) { selectedTab.value = it }
                Spacer(modifier = Modifier.height(16.dp))
                FriendListHeader(tabItems[selectedTab.value])
                Spacer(modifier = Modifier.height(8.dp))
                FriendList(
                    when (selectedTab.value) {
                        0 -> loginUser.friendList.filter { it.state == State.FRIEND }
                        1 -> loginUser.friendList.filter { it.state == State.REQUEST }
                        else -> loginUser.friendList.filter { it.state == State.PENDING }
                    }
                )
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Near & Dear",
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Cursive
        )
        Button(
            onClick = { },
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
fun UserProfile() {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ChunJyi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("50.sldf.sif/345.343.2343", fontSize = 14.sp, color = Color.Gray)
            Text("Yangon", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FriendsStoryRow() {
    Text("Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
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
                            contentDescription = "Avatar",
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
fun FriendsTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
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
                modifier = Modifier.background(
                    if (selectedTab == index) Color(0xFF2563EB) else Color.Transparent
                ).clip(RoundedCornerShape(8.dp)),
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
fun FriendListHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$title List", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = { },
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
fun FriendList(friends: List<FriendModel>) {
    var expandedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        friends.forEachIndexed { index, item ->
            val isExpanded = expandedIndex == index
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.state) {
                            State.FRIEND -> Icons.Default.Favorite
                            State.REQUEST -> Icons.Default.Star
                            State.PENDING -> Icons.Default.Star
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = when (item.state) {
                            State.FRIEND -> Color(0xFFF44336)
                            State.REQUEST -> Color(0xFFFFA000)
                            State.PENDING -> Color(0xFF2563EB)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.userName}", fontWeight = FontWeight.Bold)
                        Text("ID: ${item.userID}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
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

val friendDetailsList = listOf(
    FriendModel("uid_friend1", "Alice", State.FRIEND),
    FriendModel("uid_friend2", "Bob", State.FRIEND),
    FriendModel("uid_friend3", "Charlie", State.FRIEND),
    FriendModel("uid_friend4", "Diana", State.FRIEND),
    FriendModel("uid_friend5", "Ethan", State.FRIEND),
    FriendModel("uid_request1", "Fiona", State.REQUEST),
    FriendModel("uid_request2", "George", State.REQUEST),
    FriendModel("uid_request3", "Hannah", State.REQUEST),
    FriendModel("uid_request4", "Ivan", State.REQUEST),
    FriendModel("uid_pending1", "Jack", State.PENDING),
    FriendModel("uid_pending2", "Kara", State.PENDING),
    FriendModel("uid_pending3", "Leo", State.PENDING),
    FriendModel("uid_pending4", "Maya", State.PENDING),
    FriendModel("uid_pending5", "Nina", State.PENDING),
    FriendModel("uid_pending6", "Oscar", State.PENDING),
    FriendModel("uid_pending7", "Pia", State.PENDING),
    FriendModel("uid_pending8", "Quinn", State.PENDING),
    FriendModel("uid_pending9", "Ravi", State.PENDING),
    FriendModel("uid_pending10", "Sasha", State.PENDING)
)

val loginUser = LoginUser(
    id = 1L,
    created_at = "2025-04-12T10:00:00Z",
    name = "Test User",
    email = "testuser@example.com",
    avatar_url = "https://example.com/avatar/testuser.jpg",
    location_model = LocationModel(12.9716, 77.5946),
    userID = "uid_testuser",
    updated_at = "2025-04-12T12:00:00Z",
    friendList = friendDetailsList
)