package com.sibb.pokepi.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.localAuthDataStore: DataStore<Preferences> by preferencesDataStore(name = "local_auth_preferences")

class LocalAuthStorage(private val context: Context) {
    
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("local_username")
        private val PASSWORD_HASH_KEY = stringPreferencesKey("local_password_hash")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        private val LOCAL_AUTH_ENABLED_KEY = booleanPreferencesKey("local_auth_enabled")
        private val IS_LOCAL_LOGGED_IN_KEY = booleanPreferencesKey("is_local_logged_in")
    }
    
    suspend fun saveLocalCredentials(username: String, password: String) {
        context.localAuthDataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[PASSWORD_HASH_KEY] = hashPassword(password)
            preferences[LOCAL_AUTH_ENABLED_KEY] = true
        }
    }
    
    suspend fun validateCredentials(username: String, password: String): Boolean {
        val storedUsername = getStoredUsernameSync()
        val storedPasswordHash = getStoredPasswordHashSync()
        
        return username == storedUsername && hashPassword(password) == storedPasswordHash
    }
    
    suspend fun setLocalLoggedIn(isLoggedIn: Boolean) {
        context.localAuthDataStore.edit { preferences ->
            preferences[IS_LOCAL_LOGGED_IN_KEY] = isLoggedIn
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.localAuthDataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    fun getStoredUsername(): Flow<String?> {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }
    
    private fun getStoredPasswordHash(): Flow<String?> {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[PASSWORD_HASH_KEY]
        }
    }
    
    fun isBiometricEnabled(): Flow<Boolean> {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] ?: false
        }
    }
    
    fun isLocalAuthEnabled(): Flow<Boolean> {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[LOCAL_AUTH_ENABLED_KEY] ?: false
        }
    }
    
    fun isLocalLoggedIn(): Flow<Boolean> {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[IS_LOCAL_LOGGED_IN_KEY] ?: false
        }
    }
    
    suspend fun clearLocalAuth() {
        context.localAuthDataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(PASSWORD_HASH_KEY)
            preferences.remove(BIOMETRIC_ENABLED_KEY)
            preferences.remove(LOCAL_AUTH_ENABLED_KEY)
            preferences.remove(IS_LOCAL_LOGGED_IN_KEY)
        }
    }
    
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    private suspend fun getStoredUsernameSync(): String? {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }.first()
    }
    
    private suspend fun getStoredPasswordHashSync(): String? {
        return context.localAuthDataStore.data.map { preferences ->
            preferences[PASSWORD_HASH_KEY]
        }.first()
    }
}