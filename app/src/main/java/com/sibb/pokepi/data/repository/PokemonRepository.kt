package com.sibb.pokepi.data.repository

import androidx.paging.*
import com.sibb.pokepi.data.api.PokeApiService
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.database.UserStatsDao
import com.sibb.pokepi.data.database.UserFavoriteDao
import com.sibb.pokepi.data.model.UserFavorite
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.UserStats
import com.sibb.pokepi.data.paging.TypeSearchPagingSource
import com.sibb.pokepi.data.paging.UserFavoritePokemonPagingSource
import com.sibb.pokepi.data.paging.NameSearchPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.sibb.pokepi.notification.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val userStatsDao: UserStatsDao,
    private val userFavoriteDao: UserFavoriteDao,
    private val notificationRepository: NotificationRepository
) {
    
    @OptIn(ExperimentalPagingApi::class)
    fun getPokemonFeed(): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = PokemonRemoteMediator(pokeApiService, pokemonDao),
            pagingSourceFactory = { pokemonDao.getAllPokemonPaged() }
        ).flow
    }
    
    suspend fun getPokemonDetails(id: Int): Result<Pokemon> {
        return try {
            // First try to get from local database
            val cachedPokemon = pokemonDao.getPokemonById(id)
            if (cachedPokemon != null) {
                // Increment view count
                pokemonDao.incrementViewCount(id)
                updateUserStats()
                
                // Update data in background without blocking UI
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val response = pokeApiService.getPokemonDetails(id)
                        if (response.isSuccessful && response.body() != null) {
                            val updatedPokemon = response.body()!!.copy(
                                viewCount = cachedPokemon.viewCount + 1, // Preserve incremented view count
                                isFavorite = cachedPokemon.isFavorite, // Preserve favorite status
                                firstSeenAt = cachedPokemon.firstSeenAt // Preserve first seen timestamp
                            )
                            pokemonDao.insertPokemon(updatedPokemon)
                        }
                    } catch (e: Exception) {
                        // Ignore background update failures
                    }
                }
                
                return Result.success(cachedPokemon.copy(viewCount = cachedPokemon.viewCount + 1))
            }
            
            // If not in cache, fetch from API
            val response = pokeApiService.getPokemonDetails(id)
            if (response.isSuccessful && response.body() != null) {
                val pokemon = response.body()!!.copy(viewCount = 1)
                pokemonDao.insertPokemon(pokemon)
                updateUserStats()
                Result.success(pokemon)
            } else {
                Result.failure(Exception("Failed to fetch Pokemon: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(pokemonId: Int, userId: String): Result<Boolean> {
        return try {
            val existingFavorite = userFavoriteDao.getUserFavorite(userId, pokemonId)
            val newFavoriteStatus = if (existingFavorite != null) {
                userFavoriteDao.deleteUserFavorite(userId, pokemonId)
                false
            } else {
                userFavoriteDao.insertUserFavorite(UserFavorite(userId, pokemonId))
                true
            }
            println("PokemonRepository - Toggled favorite for Pokemon $pokemonId (User: $userId): $newFavoriteStatus")
            updateUserStats(userId)
            
            // Enviar notificación solo cuando se agrega un favorito (no cuando se quita)
            if (newFavoriteStatus) {
                // Obtener información del Pokémon para la notificación
                val pokemon = pokemonDao.getPokemonById(pokemonId)
                if (pokemon != null) {
                    // Enviar notificación en segundo plano
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            notificationRepository.sendFavoriteAddedNotification(pokemon.name, pokemonId)
                        } catch (e: Exception) {
                            println("PokemonRepository - Error enviando notificación: ${e.message}")
                        }
                    }
                }
            }
            
            Result.success(newFavoriteStatus)
        } catch (e: Exception) {
            println("PokemonRepository - Error toggling favorite: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun getFavoritesPokemon(userId: String): Flow<List<Pokemon>> {
        return userFavoriteDao.getUserFavorites(userId).map { favorites ->
            favorites.mapNotNull { favorite ->
                pokemonDao.getPokemonById(favorite.pokemonId)
            }
        }
    }
    
    fun getFavoritePokemons(userId: String): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UserFavoritePokemonPagingSource(userFavoriteDao, pokemonDao, userId) }
        ).flow
    }
    
    suspend fun isFavorite(pokemonId: Int, userId: String): Boolean {
        return userFavoriteDao.isFavorite(userId, pokemonId)
    }
    
    fun searchPokemon(query: String): Flow<List<Pokemon>> {
        return pokemonDao.searchPokemon(query)
    }
    
    fun searchPokemon(query: String, searchType: String): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { 
                when (searchType) {
                    "type" -> TypeSearchPagingSource(pokeApiService, pokemonDao, query)
                    "name" -> NameSearchPagingSource(pokeApiService, pokemonDao, query)
                    else -> NameSearchPagingSource(pokeApiService, pokemonDao, query)
                }
            }
        ).flow
    }
    
    fun getUserStats(): Flow<UserStats> {
        return userStatsDao.getUserStats().map { stats ->
            stats ?: UserStats()
        }
    }
    
    suspend fun initializeUserStats() {
        val existingStats = userStatsDao.getUserStatsSync()
        if (existingStats == null) {
            userStatsDao.insertUserStats(UserStats())
        }
        // Actualizar estadísticas basadas en datos existentes
        updateUserStats()
    }
    
    suspend fun updateTimeSpent(additionalTime: Long) {
        userStatsDao.updateTimeSpent(additionalTime, System.currentTimeMillis())
    }
    
    private suspend fun updateUserStats(userId: String? = null) {
        val totalSeen = pokemonDao.getTotalPokemonCount()
        val totalFavorites = if (userId != null) {
            userFavoriteDao.getUserFavoritesCount(userId)
        } else {
            pokemonDao.getFavoritesCount()
        }
        
        println("PokemonRepository - Updating stats: seen=$totalSeen, favorites=$totalFavorites")
        userStatsDao.updatePokemonSeenCount(totalSeen)
        userStatsDao.updateFavoritesCount(totalFavorites)
    }
    
    suspend fun getAllTypes(): List<String> {
        return try {
            // First try to get from local database (cache-first)
            val typesStrings = pokemonDao.getAllTypes()
            val allTypes = mutableSetOf<String>()
            
            typesStrings.forEach { typesJson ->
                val typeNames = extractTypeNamesFromJson(typesJson)
                allTypes.addAll(typeNames)
            }
            
            val cachedTypes = allTypes.toList().sorted()
            
            // If we have cached types, return them immediately and update in background
            if (cachedTypes.isNotEmpty()) {
                // Update data in background
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val response = pokeApiService.getAllTypes()
                        // Background update completed successfully, but we already returned cached data
                    } catch (e: Exception) {
                        // Ignore background update failures
                    }
                }
                return cachedTypes
            }
            
            // If no local data, fetch from API
            val response = pokeApiService.getAllTypes()
            if (response.isSuccessful && response.body() != null) {
                val apiTypes = response.body()!!.results
                    .map { it.name }
                    .filter { it in listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy") }
                    .sorted()
                
                return apiTypes
            } else {
                // API failed and no local data
                emptyList()
            }
        } catch (e: Exception) {
            // If there's an exception, try local database as final fallback
            try {
                val typesStrings = pokemonDao.getAllTypes()
                val allTypes = mutableSetOf<String>()
                
                typesStrings.forEach { typesJson ->
                    val typeNames = extractTypeNamesFromJson(typesJson)
                    allTypes.addAll(typeNames)
                }
                
                allTypes.toList().sorted()
            } catch (localException: Exception) {
                emptyList()
            }
        }
    }
    
    private fun extractTypeNamesFromJson(typesJson: String): List<String> {
        // Simple regex to extract type names from JSON like [{"slot":1,"type":{"name":"grass","url":"..."}}]
        val typeNameRegex = """"name":"([^"]+)"""".toRegex()
        return typeNameRegex.findAll(typesJson)
            .map { it.groupValues[1] }
            .filter { it in listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy") }
            .toList()
    }
}