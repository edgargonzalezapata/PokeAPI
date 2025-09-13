package com.sibb.pokepi.presentation.auth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sibb.pokepi.data.repository.BiometricCapability
import com.sibb.pokepi.data.repository.LocalAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalAuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLocalLoggedIn: Boolean = false,
    val hasLocalAccount: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val biometricCapability: BiometricCapability = BiometricCapability.UNKNOWN,
    val registrationSuccess: Boolean = false,
    val storedUsername: String? = null,
    val currentUserId: String? = null
)

@HiltViewModel
class LocalAuthViewModel @Inject constructor(
    private val localAuthRepository: LocalAuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LocalAuthUiState())
    val uiState: StateFlow<LocalAuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLocalAuthStatus()
    }
    
    private fun checkLocalAuthStatus() {
        viewModelScope.launch {
            combine(
                localAuthRepository.isLocalAuthEnabled(),
                localAuthRepository.isLocalLoggedIn(),
                localAuthRepository.isBiometricEnabled(),
                localAuthRepository.getStoredUsername()
            ) { isEnabled, isLoggedIn, biometricEnabled, username ->
                _uiState.value = _uiState.value.copy(
                    hasLocalAccount = isEnabled,
                    isLocalLoggedIn = isLoggedIn,
                    isBiometricEnabled = biometricEnabled,
                    storedUsername = username,
                    currentUserId = if (isLoggedIn) username else null
                )
            }.collect()
        }
    }
    
    fun checkBiometricCapability(context: Context) {
        val capability = localAuthRepository.canUseBiometric(context)
        _uiState.value = _uiState.value.copy(biometricCapability = capability)
    }
    
    fun registerLocal(username: String, password: String, enableBiometric: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            localAuthRepository.registerLocalUser(username, password)
                .onSuccess {
                    if (enableBiometric) {
                        localAuthRepository.enableBiometric()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registrationSuccess = true,
                        hasLocalAccount = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun loginLocal(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            localAuthRepository.loginLocal(username, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLocalLoggedIn = true,
                        currentUserId = username,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun authenticateWithBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
        println("DEBUG: ViewModel.authenticateWithBiometric llamado")
        println("DEBUG: Activity: ${activity::class.simpleName}")
        println("DEBUG: UI State antes: isLoading=${_uiState.value.isLoading}, isBiometricEnabled=${_uiState.value.isBiometricEnabled}")
        
        // Clear any previous errors
        _uiState.value = _uiState.value.copy(error = null, isLoading = true)
        println("DEBUG: UI State actualizado a isLoading=true")
        
        localAuthRepository.authenticateWithBiometric(
            activity,
            onSuccess = {
                println("DEBUG: Repository reportó éxito, actualizando estado...")
                viewModelScope.launch {
                    // Set logged in state directly for biometric login
                    localAuthRepository.setLocalLoggedIn(true)
                    _uiState.value = _uiState.value.copy(
                        isLocalLoggedIn = true,
                        isLoading = false,
                        currentUserId = _uiState.value.storedUsername,
                        error = null
                    )
                    println("DEBUG: Estado actualizado, llamando onSuccess")
                    onSuccess()
                }
            },
            onError = { error ->
                println("DEBUG: Repository reportó error: $error")
                _uiState.value = _uiState.value.copy(
                    error = error,
                    isLoading = false
                )
            }
        )
        println("DEBUG: Llamada a repository completada")
    }
    
    fun logoutLocal() {
        viewModelScope.launch {
            localAuthRepository.logoutLocal()
            _uiState.value = _uiState.value.copy(
                isLocalLoggedIn = false,
                currentUserId = null
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }
    
    fun enableBiometricForExistingUser() {
        viewModelScope.launch {
            localAuthRepository.enableBiometric()
            _uiState.value = _uiState.value.copy(isBiometricEnabled = true)
        }
    }
    
    fun disableBiometric() {
        viewModelScope.launch {
            localAuthRepository.disableBiometric()
            _uiState.value = _uiState.value.copy(isBiometricEnabled = false)
        }
    }
}