package com.tc.nearanddear.common

import android.content.Context
import android.widget.Toast
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.FriendModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

//    suspend fun formatTimestamp(isoTimestamp: String): String {
//        val instant = Instant.parse(isoTimestamp)
//        val formatter = DateTimeFormatter.ofPattern("MM-dd-yy: HH:mm")
//            .withZone(ZoneId.systemDefault()) // You can specify a different zone if needed
//
//        return formatter.format(instant)
//    }


}