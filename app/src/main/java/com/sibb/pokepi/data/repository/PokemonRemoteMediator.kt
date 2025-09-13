package com.sibb.pokepi.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.sibb.pokepi.data.api.PokeApiService
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.model.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao
) : RemoteMediator<Int, Pokemon>() {
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Pokemon>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        0
                    } else {
                        lastItem.id
                    }
                }
            }
            
            // For REFRESH, check if we have local data first
            if (loadType == LoadType.REFRESH) {
                val localDataCount = pokemonDao.getPokemonCount()
                if (localDataCount > 0) {
                    // We have local data, load from API in background but don't block UI
                    try {
                        loadAndUpdateInBackground(loadKey, state.config.pageSize)
                    } catch (e: Exception) {
                        // If background update fails, that's okay - user still sees cached data
                    }
                    // Return success immediately so UI shows cached data
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
            }
            
            val response = pokeApiService.getPokemonList(
                limit = state.config.pageSize,
                offset = loadKey
            )
            
            if (response.isSuccessful && response.body() != null) {
                val pokemonListResponse = response.body()!!
                
                // Fetch details for each Pokemon in parallel
                val pokemonDetails = coroutineScope {
                    pokemonListResponse.results.map { pokemonItem ->
                        async {
                            try {
                                // Check if we already have this Pokemon locally
                                val existingPokemon = pokemonDao.getPokemonById(pokemonItem.id)
                                if (existingPokemon != null) {
                                    existingPokemon
                                } else {
                                    val detailResponse = pokeApiService.getPokemonDetails(pokemonItem.id)
                                    if (detailResponse.isSuccessful && detailResponse.body() != null) {
                                        detailResponse.body()!!
                                    } else null
                                }
                            } catch (e: Exception) {
                                // If API fails, try to get from local cache
                                pokemonDao.getPokemonById(pokemonItem.id)
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
                
                // Only clear data if this is a fresh start with no local data
                if (loadType == LoadType.REFRESH && pokemonDao.getPokemonCount() == 0) {
                    pokemonDao.clearAll()
                }
                
                pokemonDao.insertAllPokemon(pokemonDetails)
                
                MediatorResult.Success(
                    endOfPaginationReached = pokemonListResponse.next == null
                )
            } else {
                // If API fails, check if we have local data to show
                if (loadType == LoadType.REFRESH && pokemonDao.getPokemonCount() > 0) {
                    // We have local data, return success so user sees cached data
                    MediatorResult.Success(endOfPaginationReached = false)
                } else {
                    MediatorResult.Error(Exception("Failed to load data: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            // If there's an exception, check if we have local data to fall back to
            if (loadType == LoadType.REFRESH && pokemonDao.getPokemonCount() > 0) {
                MediatorResult.Success(endOfPaginationReached = false)
            } else {
                MediatorResult.Error(e)
            }
        }
    }
    
    private suspend fun loadAndUpdateInBackground(offset: Int, pageSize: Int) {
        try {
            val response = pokeApiService.getPokemonList(
                limit = pageSize,
                offset = offset
            )
            
            if (response.isSuccessful && response.body() != null) {
                val pokemonListResponse = response.body()!!
                
                // Update existing Pokemon data in background
                coroutineScope {
                    pokemonListResponse.results.map { pokemonItem ->
                        async {
                            try {
                                val detailResponse = pokeApiService.getPokemonDetails(pokemonItem.id)
                                if (detailResponse.isSuccessful && detailResponse.body() != null) {
                                    val updatedPokemon = detailResponse.body()!!
                                    // Preserve view count from local data
                                    val existingPokemon = pokemonDao.getPokemonById(pokemonItem.id)
                                    val finalPokemon = if (existingPokemon != null) {
                                        updatedPokemon.copy(viewCount = existingPokemon.viewCount)
                                    } else {
                                        updatedPokemon
                                    }
                                    pokemonDao.insertPokemon(finalPokemon)
                                }
                            } catch (e: Exception) {
                                // Ignore individual failures in background update
                            }
                        }
                    }.awaitAll()
                }
            }
        } catch (e: Exception) {
            // Ignore background update failures
        }
    }
}