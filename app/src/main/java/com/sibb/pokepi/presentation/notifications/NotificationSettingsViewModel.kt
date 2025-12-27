package com.sibb.pokepi.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sibb.pokepi.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val notificationsEnabled: Boolean = true,
    val pokemonOfDayEnabled: Boolean = true,
    val favoriteUpdatesEnabled: Boolean = true,
    val appUpdatesEnabled: Boolean = true
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Observar cambios en las configuraciones
                launch {
                    notificationRepository.notificationsEnabled.collect { enabled ->
                        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
                    }
                }
                
                launch {
                    notificationRepository.pokemonOfDayEnabled.collect { enabled ->
                        _uiState.value = _uiState.value.copy(pokemonOfDayEnabled = enabled)
                    }
                }
                
                launch {
                    notificationRepository.favoriteUpdatesEnabled.collect { enabled ->
                        _uiState.value = _uiState.value.copy(favoriteUpdatesEnabled = enabled)
                    }
                }
                
                launch {
                    notificationRepository.appUpdatesEnabled.collect { enabled ->
                        _uiState.value = _uiState.value.copy(appUpdatesEnabled = enabled)
                    }
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando configuraciones: ${e.message}"
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationRepository.setNotificationsEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error actualizando notificaciones: ${e.message}"
                )
            }
        }
    }

    fun togglePokemonOfDay(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationRepository.setPokemonOfDayEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error actualizando Pokémon del día: ${e.message}"
                )
            }
        }
    }

    fun toggleFavoriteUpdates(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationRepository.setFavoriteUpdatesEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error actualizando actualizaciones de favoritos: ${e.message}"
                )
            }
        }
    }

    fun toggleAppUpdates(enabled: Boolean) {
        viewModelScope.launch {
            try {
                notificationRepository.setAppUpdatesEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error actualizando actualizaciones de app: ${e.message}"
                )
            }
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            try {
                notificationRepository.sendTestNotification()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error enviando notificación de prueba: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}