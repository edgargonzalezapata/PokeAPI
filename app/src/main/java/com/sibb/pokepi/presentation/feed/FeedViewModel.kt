package com.sibb.pokepi.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.UserStats
import com.sibb.pokepi.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val userStats: UserStats = UserStats(),
    val searchQuery: String = "",
    val searchType: String = "name",
    val availableTypes: List<String> = emptyList(),
    val selectedType: String = "",
    val favoriteStatus: Map<Int, Boolean> = emptyMap()
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _searchType = MutableStateFlow("name")
    private val _selectedType = MutableStateFlow("")
    
    val pokemonFeed: Flow<PagingData<Pokemon>> = combine(
        _searchQuery, 
        _searchType, 
        _selectedType
    ) { query, type, selectedType ->
        Triple(query, type, selectedType)
    }.debounce(300) // Esperar 300ms antes de buscar
    .flatMapLatest { (query, type, selectedType) ->
        when {
            query.isNotBlank() && type == "name" -> pokemonRepository.searchPokemon(query, type)
            selectedType.isNotBlank() && type == "type" -> pokemonRepository.searchPokemon(selectedType, type)
            else -> pokemonRepository.getPokemonFeed()
        }
    }
    
    val userStats: Flow<UserStats> = pokemonRepository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )
    
    init {
        initializeUserStats()
        loadUserStats()
        loadTypes()
        startTimeTracking()
    }
    
    private fun initializeUserStats() {
        viewModelScope.launch {
            pokemonRepository.initializeUserStats()
        }
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                userStats.collect { stats ->
                    _uiState.value = _uiState.value.copy(userStats = stats)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleFavorite(pokemonId: Int, userId: String) {
        viewModelScope.launch {
            pokemonRepository.toggleFavorite(pokemonId, userId)
                .onSuccess { newFavoriteStatus ->
                    println("FeedViewModel - Toggled favorite for Pokemon $pokemonId (User: $userId): $newFavoriteStatus")
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
                println("FeedViewModel - Loaded favorite status for Pokemon $pokemonId (User: $userId): $isFavorite")
            } catch (e: Exception) {
                println("FeedViewModel - Error loading favorite status: ${e.message}")
            }
        }
    }
    
    fun loadFavoriteStatusBatch(pokemonIds: List<Int>, userId: String) {
        viewModelScope.launch {
            try {
                val currentFavorites = _uiState.value.favoriteStatus.toMutableMap()
                pokemonIds.forEach { pokemonId ->
                    if (!currentFavorites.containsKey(pokemonId)) {
                        val isFavorite = pokemonRepository.isFavorite(pokemonId, userId)
                        currentFavorites[pokemonId] = isFavorite
                        println("FeedViewModel - Batch loaded favorite status for Pokemon $pokemonId: $isFavorite")
                    }
                }
                _uiState.value = _uiState.value.copy(favoriteStatus = currentFavorites)
            } catch (e: Exception) {
                println("FeedViewModel - Error loading batch favorite status: ${e.message}")
            }
        }
    }
    
    fun clearFavoriteStatus() {
        _uiState.value = _uiState.value.copy(favoriteStatus = emptyMap())
        println("FeedViewModel - Cleared all favorite status")
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun updateSearchQuery(query: String, searchType: String) {
        // Show searching indicator for type searches or non-empty name searches
        val isSearching = searchType == "type" || query.isNotBlank()
        _uiState.value = _uiState.value.copy(isSearching = isSearching)
        
        _searchQuery.value = query
        _searchType.value = searchType
        _uiState.value = _uiState.value.copy(searchQuery = query, searchType = searchType)
        
        // Clear type selection when switching to name search
        if (searchType == "name") {
            _selectedType.value = ""
            _uiState.value = _uiState.value.copy(selectedType = "")
        }
    }
    
    fun updateSelectedType(type: String) {
        // Show searching indicator when selecting a type
        _uiState.value = _uiState.value.copy(isSearching = true)
        
        _selectedType.value = type
        _searchType.value = "type"
        _uiState.value = _uiState.value.copy(selectedType = type, searchType = "type")
        
        // Clear text query when selecting type
        _searchQuery.value = ""
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _selectedType.value = ""
        _uiState.value = _uiState.value.copy(searchQuery = "", selectedType = "", isSearching = false)
    }
    
    fun setSearchingState(isSearching: Boolean) {
        _uiState.value = _uiState.value.copy(isSearching = isSearching)
    }
    
    fun getPokemonById(pokemonId: Int, onResult: (Pokemon?) -> Unit) {
        viewModelScope.launch {
            try {
                pokemonRepository.getPokemonDetails(pokemonId)
                    .onSuccess { pokemon ->
                        onResult(pokemon)
                    }
                    .onFailure { error ->
                        println("FeedViewModel - Error getting Pokemon $pokemonId: ${error.message}")
                        onResult(null)
                    }
            } catch (e: Exception) {
                println("FeedViewModel - Exception getting Pokemon $pokemonId: ${e.message}")
                onResult(null)
            }
        }
    }
    
    private fun loadTypes() {
        viewModelScope.launch {
            try {
                val types = pokemonRepository.getAllTypes()
                _uiState.value = _uiState.value.copy(availableTypes = types)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun startTimeTracking() {
        val startTime = System.currentTimeMillis()
        
        // Track time spent when ViewModel is cleared
        viewModelScope.launch {
            // This will run when the ViewModel is cleared
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Update time spent when leaving the screen
        viewModelScope.launch {
            pokemonRepository.updateTimeSpent(60000) // 1 minute example
        }
    }
}