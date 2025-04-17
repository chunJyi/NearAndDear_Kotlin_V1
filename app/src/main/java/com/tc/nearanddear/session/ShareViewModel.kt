package com.tc.nearanddear.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.model.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    var selectedFriend by mutableStateOf<String?>(null)
        private set

    var selectedUser by mutableStateOf<LoginUser?>(null)
        private set

    var customCameraPosition by mutableStateOf<LatLng?>(null)
        private set

    var customZoomLevel by mutableStateOf<Float?>(null)
        private set

    fun setCustomCamera(latLng: LatLng, zoom: Float) {
        customCameraPosition = latLng
        customZoomLevel = zoom
    }

    private val _userResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val userResults: StateFlow<List<SearchResult>> = _userResults
    fun setFriend(friend: String) {
        selectedFriend = friend
    }

    fun clearFriend() {
        selectedFriend = null
    }

    fun setUser(user: LoginUser?) {
        selectedUser = user;
    }

    fun clearUser() {
        selectedUser = null
    }
}
