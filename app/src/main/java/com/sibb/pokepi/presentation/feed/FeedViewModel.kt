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
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userStats: UserStats = UserStats(),
    val searchQuery: String = "",
    val searchType: String = "name",
    val availableTypes: List<String> = emptyList(),
    val selectedType: String = ""
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
    }.cachedIn(viewModelScope)
    
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
    
    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            pokemonRepository.toggleFavorite(pokemonId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun updateSearchQuery(query: String, searchType: String) {
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
        _uiState.value = _uiState.value.copy(searchQuery = "", selectedType = "")
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