package com.sibb.pokepi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.database.UserFavoriteDao
import com.sibb.pokepi.data.model.Pokemon

class UserFavoritePokemonPagingSource(
    private val userFavoriteDao: UserFavoriteDao,
    private val pokemonDao: PokemonDao,
    private val userId: String
) : PagingSource<Int, Pokemon>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            println("UserFavoritePokemonPagingSource - Loading for userId: $userId")
            
            // Get user favorites synchronously 
            val favoritesList = userFavoriteDao.getUserFavoritesSync(userId)
            println("UserFavoritePokemonPagingSource - Found ${favoritesList.size} favorites")
            
            // Convert to Pokemon objects
            val pokemonList = favoritesList.mapNotNull { favorite ->
                pokemonDao.getPokemonById(favorite.pokemonId)?.also {
                    println("UserFavoritePokemonPagingSource - Loaded Pokemon: ${it.name}")
                }
            }
            
            println("UserFavoritePokemonPagingSource - Returning ${pokemonList.size} pokemon")

            LoadResult.Page(
                data = pokemonList,
                prevKey = null,
                nextKey = null
            )
        } catch (e: Exception) {
            println("UserFavoritePokemonPagingSource - Error: ${e.message}")
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}