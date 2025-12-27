package com.sibb.pokepi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User statistics entity for tracking user activity and engagement.
 */
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val totalPokemonSeen: Int = 0,
    val totalFavorites: Int = 0,
    val totalTimeSpent: Long = 0, // in milliseconds
    val lastActiveTime: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
