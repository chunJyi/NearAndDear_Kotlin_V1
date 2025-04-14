package com.tc.nearanddear.session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tc.nearanddear.model.LoginUser

object UserSession {
    var loginUser by mutableStateOf<LoginUser?>(null)

}