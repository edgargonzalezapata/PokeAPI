package com.sibb.pokepi.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteStatus: Map<Int, Boolean> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    private val _userId = MutableStateFlow<String?>(null)
    
    val favoritePokemons: Flow<PagingData<Pokemon>> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            pokemonRepository.getFavoritePokemons(userId)
        }
    
    fun setUserId(userId: String) {
        println("FavoritesViewModel - Setting userId: $userId")
        _userId.value = userId
    }
    
    fun toggleFavorite(pokemonId: Int, userId: String) {
        viewModelScope.launch {
            pokemonRepository.toggleFavorite(pokemonId, userId)
                .onSuccess { newFavoriteStatus ->
                    println("FavoritesViewModel - Toggled favorite for Pokemon $pokemonId (User: $userId): $newFavoriteStatus")
                    // Update the local favorite status immediately for UI feedback
                    val currentFavorites = _uiState.value.favoriteStatus.toMutableMap()
                    currentFavorites[pokemonId] = newFavoriteStatus
                    _uiState.value = _uiState.value.copy(favoriteStatus = currentFavorites)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun loadFavoriteStatus(pokemonId: Int, userId: String) {
        viewModelScope.launch {
            try {
                val isFavorite = pokemonRepository.isFavorite(pokemonId, userId)
                val currentFavorites = _uiState.value.favoriteStatus.toMutableMap()
                currentFavorites[pokemonId] = isFavorite
                _uiState.value = _uiState.value.copy(favoriteStatus = currentFavorites)
            } catch (e: Exception) {
                println("FavoritesViewModel - Error loading favorite status: ${e.message}")
            }
        }
    }
    
    fun clearFavoriteStatus() {
        _uiState.value = _uiState.value.copy(favoriteStatus = emptyMap())
        println("FavoritesViewModel - Cleared all favorite status")
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getPokemonById(pokemonId: Int, onResult: (Pokemon?) -> Unit) {
        viewModelScope.launch {
            try {
                pokemonRepository.getPokemonDetails(pokemonId)
                    .onSuccess { pokemon ->
                        onResult(pokemon)
                    }
                    .onFailure { error ->
                        println("FavoritesViewModel - Error getting Pokemon $pokemonId: ${error.message}")
                        onResult(null)
                    }
            } catch (e: Exception) {
                println("FavoritesViewModel - Exception getting Pokemon $pokemonId: ${e.message}")
                onResult(null)
            }
        }
    }
}