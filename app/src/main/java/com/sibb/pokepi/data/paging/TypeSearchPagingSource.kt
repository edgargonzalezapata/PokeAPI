package com.sibb.pokepi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sibb.pokepi.data.api.PokeApiService
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.model.Pokemon
import kotlinx.coroutines.delay

class TypeSearchPagingSource(
    private val pokeApiService: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val typeName: String
) : PagingSource<Int, Pokemon>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            val page = params.key ?: 0
            
            // Get Pokemon of the specified type from API
            val response = pokeApiService.getPokemonByType(typeName)
            
            if (response.isSuccessful && response.body() != null) {
                val typeData = response.body()!!
                val pokemonEntries = typeData.pokemon
                
                // Calculate pagination
                val pageSize = params.loadSize
                val startIndex = page * pageSize
                val endIndex = minOf(startIndex + pageSize, pokemonEntries.size)
                
                if (startIndex >= pokemonEntries.size) {
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = null
                    )
                }
                
                val pokemonForPage = pokemonEntries.subList(startIndex, endIndex)
                
                // Fetch detailed Pokemon data for this page
                val pokemonList = mutableListOf<Pokemon>()
                pokemonForPage.forEach { entry ->
                    try {
                        // Try to get from local cache first
                        val localPokemon = pokemonDao.getPokemonById(entry.pokemon.id)
                        if (localPokemon != null) {
                            pokemonList.add(localPokemon)
                        } else {
                            // Fetch from API if not in cache
                            val pokemonResponse = pokeApiService.getPokemonDetails(entry.pokemon.id)
                            if (pokemonResponse.isSuccessful && pokemonResponse.body() != null) {
                                val pokemon = pokemonResponse.body()!!
                                // Save to local database for future use
                                pokemonDao.insertPokemon(pokemon)
                                pokemonList.add(pokemon)
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
                    nextKey = if (endIndex >= pokemonEntries.size) null else page + 1
                )
            } else {
                // Fallback to local database search
                val localResults = pokemonDao.searchPokemonByType(typeName)
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