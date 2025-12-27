package com.sibb.pokepi.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class TokenStorage(private val context: Context) {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_LOGIN_KEY = stringPreferencesKey("user_login")
    }
    
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }
    
    suspend fun saveUserLogin(login: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOGIN_KEY] = login
        }
    }
    
    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }
    
    fun getUserLogin(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_LOGIN_KEY]
        }
    }
    
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(USER_LOGIN_KEY)
        }
    }
}