package com.sibb.pokepi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sibb.pokepi.data.api.PokeApiService
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.PokemonListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class NameSearchPagingSource(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val searchQuery: String
) : PagingSource<Int, Pokemon>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            val page = params.key ?: 0
            
            // First, get the complete list of Pokemon from API (up to 1500)
            val response = pokeApiService.getPokemonList(limit = 1500, offset = 0)
            
            if (response.isSuccessful && response.body() != null) {
                val pokemonListResponse = response.body()!!
                
                // Filter Pokemon by name that contains the search query (case insensitive)
                val filteredPokemon = pokemonListResponse.results.filter { pokemonItem ->
                    pokemonItem.name.contains(searchQuery, ignoreCase = true)
                }
                
                // Calculate pagination for filtered results
                val pageSize = params.loadSize
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
                // Fallback to local database search
                val localResults = try {
                    pokemonDao.searchPokemon(searchQuery).first()
                } catch (e: Exception) {
                    emptyList()
                }
                LoadResult.Page(
                    data = localResults,
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}