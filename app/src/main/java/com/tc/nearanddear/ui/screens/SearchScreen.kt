package com.tc.nearanddear.ui.screens

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import com.tc.nearanddear.model.FriendState
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.model.SearchResult
import com.tc.nearanddear.session.UserSession
import com.tc.nearanddear.session.UserSession.loginUser
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- ViewModel ---
class SharedViewModel1 : ViewModel() {
    private val _userResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val userResults: StateFlow<List<SearchResult>> = _userResults.asStateFlow()

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _userResults.value = fetchSearchResults(query) ?: emptyList()
        }
    }

    private suspend fun fetchSearchResults(query: String): List<SearchResult>? {
        if (query.isBlank()) {
            return emptyList()
        }
        return searchUsersById(query)
    }

    private suspend fun searchUsersById(userName: String): List<SearchResult> {
        return runCatching {
            client.from("loginUser")
                .select(columns = Columns.list("userID", "name", "avatar_url")) {
                    filter {
                        ilike("name", "%$userName%")
                    }
                }.decodeList<SearchResult>()
        }.getOrElse {
            println("Error fetching user: ${it.message}")
            emptyList()
        }
    }
}

// --- SearchScreen Composable ---
@Composable
fun SearchScreen(context: Context, navController: NavController) {
    val sharedViewModel: SharedViewModel1 = viewModel()

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEDF2))
    ) {
        SearchHeader(onBackClick = { navController.popBackStack() })
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClearQuery = { searchQuery = "" },
            onSearch = { sharedViewModel.searchUsers(searchQuery) })
        SearchResultsList(context, results = sharedViewModel.userResults.collectAsState().value)
    }
}

// --- SearchHeader Composable ---
@Composable
fun SearchHeader(onBackClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(0xFFA8B0D6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Search",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "It's a functional search system.", fontSize = 16.sp, color = Color.White
                )
            }
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "BACK HOME",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- SearchBar Composable ---
@Composable
fun SearchBar(
    query: String, onQueryChange: (String) -> Unit, onClearQuery: () -> Unit, onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFA8B0D6))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_menu_search),
            contentDescription = "Search Icon",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black, fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "What are you looking for?", color = Color.Gray, fontSize = 16.sp
                        )
                    }
                    innerTextField()
                })
        }
        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "Clear Search",
                tint = Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClearQuery() })
        }
    }
}

// --- SearchResultsList Composable ---
@Composable
fun SearchResultsList(context: Context, results: List<SearchResult>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor =Color(0xFFA8B0D6))
    ) {
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(
                        text = "Nothing found...",
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "There's nothing matching the description you're looking for, try a different keyword.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Title
                Text(
                    text = "Search Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Divider
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp, color = Color.LightGray
                )
                // LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .heightIn(min = 300.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(results) { result ->
                        SearchResultItem(context, result = result)
                    }
                }
            }

        }
    }
}

// --- SearchResultItem Composable ---
@Composable
fun SearchResultItem(context: Context, result: SearchResult) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(result.avatar_url),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(30.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = result.userID,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Button(
            onClick = {
                addUser(
                    context, UserSession.loginUser, result
                )
            },
            modifier = Modifier
                .height(30.dp)
                .width(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.width(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun addUser(
    context: Context, currentUser: LoginUser?, friendUser: SearchResult
) {
    if (friendUser.userID.isEmpty() || currentUser == null) return

    CoroutineScope(Dispatchers.IO).launch {
        try {


            val currentUserId = currentUser.userID
            val friendUserId = friendUser.userID

            if (currentUserId == friendUserId) {
                showToastOnMain(context, "can not add your self")
                return@launch
            }
            val currentUserFriendList = fetchUserById(currentUserId)?.friendList
            val friendUserFriendList = fetchUserById(friendUserId)?.friendList

            if (currentUserFriendList == null || friendUserFriendList === null) {
                showToastOnMain(context, "bad request")
                return@launch
            }

            if (isAlreadyFriend(currentUserFriendList, friendUserId)) {
                showToastOnMain(context, "User already in friend list")
                return@launch
            }

            if (isAlreadyFriend(friendUserFriendList, currentUserId)) {
                showToastOnMain(context, "User already in friend list")
                return@launch
            }

            val updatedCurrentList = currentUserFriendList.toMutableList().apply {
                add(createFriendEntry(friendUserId, friendUser.name, FriendState.PENDING))
            }

            val updatedFriendList = friendUserFriendList.toMutableList().apply {
                add(createFriendEntry(currentUserId, currentUser.name, FriendState.REQUEST))
            }

            updateFriendListInDB(currentUserId, updatedCurrentList)
            updateFriendListInDB(friendUserId, updatedFriendList)
            loginUser = fetchUserById(currentUserId);

            showToastOnMain(context, "Friend added successfully!")
        } catch (e: Exception) {
            showToastOnMain(context, "Error adding user: ${e.message}")
        }
    }
}

private fun isAlreadyFriend(friendList: List<FriendModel>, userIdToCheck: String): Boolean {
    return friendList.any { userIdToCheck == it.userID }
}

private fun createFriendEntry(userID: String, name: String, state: FriendState): FriendModel {
    return FriendModel(userID = userID, name = name, friendState = state)
}

internal suspend fun updateFriendListInDB(userId: String, friendList: List<FriendModel>) {
    client.from("loginUser").update(mapOf("friendList" to friendList)) {
        filter { eq("userID", userId) }
    }
}

internal suspend fun showToastOnMain(context: Context, message: String) {
    withContext(Dispatchers.Main) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}





