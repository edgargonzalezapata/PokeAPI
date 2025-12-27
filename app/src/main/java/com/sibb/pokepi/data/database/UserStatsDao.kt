package com.sibb.pokepi.data.database

import androidx.room.*
import com.sibb.pokepi.data.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>
    
    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStats?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)
    
    @Update
    suspend fun updateUserStats(userStats: UserStats)
    
    @Query("UPDATE user_stats SET totalPokemonSeen = :count WHERE id = 1")
    suspend fun updatePokemonSeenCount(count: Int)
    
    @Query("UPDATE user_stats SET totalFavorites = :count WHERE id = 1")
    suspend fun updateFavoritesCount(count: Int)
    
    @Query("UPDATE user_stats SET totalTimeSpent = totalTimeSpent + :additionalTime, lastActiveTime = :currentTime WHERE id = 1")
    suspend fun updateTimeSpent(additionalTime: Long, currentTime: Long)
    
    @Query("DELETE FROM user_stats")
    suspend fun clearStats()
}