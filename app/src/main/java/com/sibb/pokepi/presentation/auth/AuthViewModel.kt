package com.sibb.pokepi.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sibb.pokepi.data.model.GitHubUser
import com.sibb.pokepi.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: GitHubUser? = null,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val showWelcome: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                println("AuthViewModel - isLoggedIn: $isLoggedIn")
                if (isLoggedIn && _uiState.value.user == null) {
                    println("AuthViewModel - Getting current user")
                    getCurrentUser()
                } else if (!isLoggedIn) {
                    // Solo limpiar estado de UI, NO datos persistentes
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = false,
                        user = null,
                        showWelcome = false
                    )
                }
            }
        }
    }
    
    fun handleAuthorizationCode(code: String) {
        viewModelScope.launch {
            println("AuthViewModel - Handling authorization code: $code")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.exchangeCodeForToken(code)
                .onSuccess { 
                    println("AuthViewModel - Token exchange successful")
                    getCurrentUser()
                }
                .onFailure { error ->
                    println("AuthViewModel - Token exchange failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    private fun getCurrentUser() {
        viewModelScope.launch {
            println("AuthViewModel - Getting current user...")
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    println("AuthViewModel - Got user: ${user.login}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        isLoggedIn = true,
                        showWelcome = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    println("AuthViewModel - Failed to get user: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            // Solo cerrar sesión, NO limpiar datos del usuario (favoritos, stats, etc.)
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearData() {
        viewModelScope.launch {
            // Este método puede ser usado para limpiar todo si es necesario en el futuro
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
    
    fun getAuthUrl(): String = authRepository.getAuthUrl()
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun dismissWelcome() {
        _uiState.value = _uiState.value.copy(showWelcome = false)
    }
}