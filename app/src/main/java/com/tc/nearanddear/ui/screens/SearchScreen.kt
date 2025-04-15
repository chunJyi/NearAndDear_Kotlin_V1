package com.tc.nearanddear.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import com.tc.nearanddear.model.SearchResult
import com.tc.nearanddear.session.UserSession
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
                }
                .decodeList<SearchResult>()
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
            .background(Color.White)
    ) {
        SearchHeader(onBackClick = { navController.popBackStack() })
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClearQuery = { searchQuery = "" },
            onSearch = { sharedViewModel.searchUsers(searchQuery) }
        )
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
            .background(Color.DarkGray)
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
                    text = "It's a functional search system.",
                    fontSize = 16.sp,
                    color = Color.White
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
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
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
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "What are you looking for?",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "Clear Search",
                tint = Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClearQuery() }
            )
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .heightIn(min = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
                    context, result.userID,
                    friendUserId = UserSession.loginUser?.userID ?: ""
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
                    Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.width(10.dp)
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

fun addUser(context: Context, currentUserId: String, friendUserId: String) {
    if (friendUserId?.isEmpty() == true) {
        return
    }
    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            // Step 1: Fetch current user's friendList
            val response = client.from("loginUser")
                .select(columns = Columns.list("friendList")) {
                    filter {
                        eq("userID", currentUserId)
                    }
                }
                .decodeSingle<Map<String, List<FriendModel>>>()

            val currentList =
                (response["friendList"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            // Step 2: Check if friend already added
            if (friendUserId in currentList) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "User already in friend list", Toast.LENGTH_SHORT)
                        .show()
                }
                return@launch
            }

            // Step 3: Add friend to list
            val updatedList = currentList.toMutableList().apply { add(friendUserId) }

            // Step 4: Update Supabase
            client.from("loginUser")
                .update(mapOf("friendList" to updatedList)) {
                    filter {
                        eq("userID", currentUserId)
                    }
                }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error adding user: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}





