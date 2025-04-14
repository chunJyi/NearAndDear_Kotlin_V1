package com.tc.nearanddear.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tc.nearanddear.model.LoginUser

class SharedViewModel : ViewModel() {
    var selectedFriend by mutableStateOf<String?>(null)
        private set

    var selectedUser by mutableStateOf<LoginUser?>(null)
        private set

    fun setFriend(friend: String) {
        selectedFriend = friend
    }

    fun clearFriend() {
        selectedFriend = null
    }

    fun setUser(user: LoginUser) {
        selectedUser = user;
    }

    fun clearUser() {
        selectedUser = null
    }
}
