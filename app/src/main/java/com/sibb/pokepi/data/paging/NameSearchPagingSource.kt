package com.sibb.pokepi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sibb.pokepi.data.api.PokeApiService
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.PokemonListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NameSearchPagingSource(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val searchQuery: String
) : PagingSource<Int, Pokemon>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            // CACHE-FIRST STRATEGY: Try local database first
            val localResults = try {
                pokemonDao.searchPokemon(searchQuery).first()
            } catch (e: Exception) {
                emptyList()
            }
            
            if (localResults.isNotEmpty()) {
                // We have local data, return it immediately and update in background
                val startIndex = page * pageSize
                val endIndex = minOf(startIndex + pageSize, localResults.size)
                
                if (startIndex >= localResults.size) {
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = null
                    )
                }
                
                val localPageData = localResults.subList(startIndex, endIndex)
                
                // Update data in background without blocking UI
                CoroutineScope(Dispatchers.IO).launch {
                    updateNameSearchDataInBackground()
                }
                
                return LoadResult.Page(
                    data = localPageData,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (endIndex >= localResults.size) null else page + 1
                )
            }
            
            // No local data, fetch from API
            val response = pokeApiService.getPokemonList(limit = 1500, offset = 0)
            
            if (response.isSuccessful && response.body() != null) {
                val pokemonListResponse = response.body()!!
                
                // Filter Pokemon by name that contains the search query (case insensitive)
                val filteredPokemon = pokemonListResponse.results.filter { pokemonItem ->
                    pokemonItem.name.contains(searchQuery, ignoreCase = true)
                }
                
                // Calculate pagination for filtered results
                val startIndex = page * pageSize
                val endIndex = minOf(startIndex + pageSize, filteredPokemon.size)
                
                if (startIndex >= filteredPokemon.size) {
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = null
                    )
                }
                
                val pokemonForPage = filteredPokemon.subList(startIndex, endIndex)
                
                // Fetch detailed Pokemon data for this page
                val pokemonList = mutableListOf<Pokemon>()
                pokemonForPage.forEach { pokemonItem ->
                    try {
                        // Try to get from local cache first
                        val localPokemon = pokemonDao.getPokemonById(pokemonItem.id)
                        if (localPokemon != null) {
                            pokemonList.add(localPokemon)
                        } else {
                            // Fetch from API if not in cache
                            val pokemonResponse = pokeApiService.getPokemonDetails(pokemonItem.id)
                            if (pokemonResponse.isSuccessful && pokemonResponse.body() != null) {
                                val pokemon = pokemonResponse.body()!!
                                // Save to local database preserving local data (favorites, etc.)
                                pokemonDao.insertPokemonPreservingLocalData(pokemon)
                                // Get the pokemon with preserved local data
                                val savedPokemon = pokemonDao.getPokemonById(pokemon.id)
                                if (savedPokemon != null) {
                                    pokemonList.add(savedPokemon)
                                } else {
                                    pokemonList.add(pokemon)
                                }
                            }
                        }
                        // Small delay to avoid hitting API rate limits
                        delay(50)
                    } catch (e: Exception) {
                        // Continue with other Pokemon if one fails
                        e.printStackTrace()
                    }
                }
                
                LoadResult.Page(
                    data = pokemonList,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (endIndex >= filteredPokemon.size) null else page + 1
                )
            } else {
                // API failed, try local database as final fallback
                val fallbackResults = try {
                    pokemonDao.searchPokemon(searchQuery).first()
                } catch (e: Exception) {
                    emptyList()
                }
                
                val startIndex = page * pageSize
                val endIndex = minOf(startIndex + pageSize, fallbackResults.size)
                
                val fallbackPageData = if (startIndex < fallbackResults.size) {
                    fallbackResults.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                LoadResult.Page(
                    data = fallbackPageData,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (endIndex >= fallbackResults.size) null else page + 1
                )
            }
        } catch (e: Exception) {
            // If there's an exception, try local database as final fallback
            try {
                val fallbackResults = pokemonDao.searchPokemon(searchQuery).first()
                val startIndex = (params.key ?: 0) * params.loadSize
                val endIndex = minOf(startIndex + params.loadSize, fallbackResults.size)
                
                val fallbackPageData = if (startIndex < fallbackResults.size) {
                    fallbackResults.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                LoadResult.Page(
                    data = fallbackPageData,
                    prevKey = if ((params.key ?: 0) == 0) null else (params.key ?: 0) - 1,
                    nextKey = if (endIndex >= fallbackResults.size) null else (params.key ?: 0) + 1
                )
            } catch (localException: Exception) {
                LoadResult.Error(e)
            }
        }
    }
    
    private suspend fun updateNameSearchDataInBackground() {
        try {
            val response = pokeApiService.getPokemonList(limit = 1500, offset = 0)
            if (response.isSuccessful && response.body() != null) {
                val pokemonListResponse = response.body()!!
                
                // Filter Pokemon by name that contains the search query (case insensitive)
                val filteredPokemon = pokemonListResponse.results.filter { pokemonItem ->
                    pokemonItem.name.contains(searchQuery, ignoreCase = true)
                }
                
                // Update Pokemon data in background
                filteredPokemon.forEach { pokemonItem ->
                    try {
                        val pokemonResponse = pokeApiService.getPokemonDetails(pokemonItem.id)
                        if (pokemonResponse.isSuccessful && pokemonResponse.body() != null) {
                            val pokemon = pokemonResponse.body()!!
                            // Preserve local data when updating
                            pokemonDao.insertPokemonPreservingLocalData(pokemon)
                        }
                        delay(100) // Longer delay for background updates
                    } catch (e: Exception) {
                        // Ignore individual failures in background update
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore background update failures
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}