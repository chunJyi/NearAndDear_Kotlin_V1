package com.tc.nearanddear.common

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object DataStoreManager {
    private val LOGIN_KEY = booleanPreferencesKey("is_user_logged_in")
    private val ONBOARDING_KEY = booleanPreferencesKey("is_onboarding_done")

    fun isUserLoggedIn(context: Context): Boolean = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[LOGIN_KEY] ?: false
        }.first()
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean) = runBlocking {
        context.dataStore.edit { prefs ->
            prefs[LOGIN_KEY] = loggedIn
        }
    }

    fun isOnboardingCompleted(context: Context): Boolean = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_KEY] ?: false
        }.first()
    }

    fun setOnboardingCompleted(context: Context) = runBlocking {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_KEY] = true
        }
    }
}
