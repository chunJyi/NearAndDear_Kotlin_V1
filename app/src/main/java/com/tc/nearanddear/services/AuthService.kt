package com.tc.nearanddear.services

import android.content.Context
import android.util.Log
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.tc.nearanddear.model.LoginUser
import com.tc.nearanddear.model.LocationModel
import com.tc.nearanddear.data.SupabaseClientProvider
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.*

private const val TAG = "AuthService"

object AuthService {

    suspend fun signInWithGoogle(context: Context): LoginUser? = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("121998306121-veaks2t8uq15kl6uaerqldks28j97aot.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        try {
            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            if (googleIdToken.isNullOrEmpty()) {
                Log.e(TAG, "Google ID token is null or empty")
                return@withContext null
            }

            val client = SupabaseClientProvider.client

            client.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
                nonce = rawNonce
            }

            val session = client.auth.currentSessionOrNull()
            val user = session?.user ?: return@withContext null

            val userId = user.id
            val name = user.userMetadata?.get("name") as? String ?: "No Name"
            val email = user.email ?: "No Email"
            val avatarUrl = user.userMetadata?.get("avatar_url") as? String ?: ""
            val updatedAt = System.currentTimeMillis().toString()

            val newUser = LoginUser(
                userID = userId,
                name = name,
                email = email,
                avatar_url = avatarUrl,
                location_model = LocationModel(0.0, 0.0),
                updated_at = updatedAt
            )

            val existingUsers = client.from("loginUser")
                .select {
                    filter { eq("userID", userId) }
                }
                .decodeList<LoginUser>()

            if (existingUsers.isEmpty()) {
                client.from("loginUser").insert(newUser)
                Log.d(TAG, "User inserted successfully: $userId")
            } else {
                Log.d(TAG, "User already exists: $userId")
            }

            newUser
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled the sign-in")
            null
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Token parsing failed: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in or insert failed: ${e.message}", e)
            null
        }
    }
}
