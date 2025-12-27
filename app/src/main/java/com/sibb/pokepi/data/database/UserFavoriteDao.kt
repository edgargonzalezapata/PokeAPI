package com.sibb.pokepi.data.database

import androidx.paging.PagingSource
import androidx.room.*
import com.sibb.pokepi.data.model.UserFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFavoriteDao {
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserFavorites(userId: String): Flow<List<UserFavorite>>
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId ORDER BY addedAt DESC")
    suspend fun getUserFavoritesSync(userId: String): List<UserFavorite>
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserFavoritesPaged(userId: String): PagingSource<Int, UserFavorite>
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId AND pokemonId = :pokemonId")
    suspend fun getUserFavorite(userId: String, pokemonId: Int): UserFavorite?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFavorite(userFavorite: UserFavorite)
    
    @Delete
    suspend fun deleteUserFavorite(userFavorite: UserFavorite)
    
    @Query("DELETE FROM user_favorites WHERE userId = :userId AND pokemonId = :pokemonId")
    suspend fun deleteUserFavorite(userId: String, pokemonId: Int)
    
    @Query("SELECT COUNT(*) FROM user_favorites WHERE userId = :userId")
    suspend fun getUserFavoritesCount(userId: String): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND pokemonId = :pokemonId)")
    suspend fun isFavorite(userId: String, pokemonId: Int): Boolean
    
    @Query("DELETE FROM user_favorites WHERE userId = :userId")
    suspend fun clearUserFavorites(userId: String)
}