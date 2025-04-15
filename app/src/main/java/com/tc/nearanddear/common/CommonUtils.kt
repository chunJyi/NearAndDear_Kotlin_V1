package com.tc.nearanddear.common

import android.content.Context
import android.widget.Toast
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommonUtils {

//    static suspend fun updateFriendListInDB(userId: String, friendList: List<FriendModel>) {
//        client.from("loginUser").update(mapOf("friendList" to friendList)) {
//            filter { eq("userID", userId) }
//        }
//    }
//
//     suspend fun showToastOnMain(context: Context, message: String) {
//        withContext(Dispatchers.Main) {
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//        }
//    }
}