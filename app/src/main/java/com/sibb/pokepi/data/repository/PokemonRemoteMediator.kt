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
                                val detailResponse = pokeApiService.getPokemonDetails(pokemonItem.id)
                                if (detailResponse.isSuccessful && detailResponse.body() != null) {
                                    detailResponse.body()!!
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
                
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.clearAll()
                }
                
                pokemonDao.insertAllPokemon(pokemonDetails)
                
                MediatorResult.Success(
                    endOfPaginationReached = pokemonListResponse.next == null
                )
            } else {
                MediatorResult.Error(Exception("Failed to load data: ${response.message()}"))
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}