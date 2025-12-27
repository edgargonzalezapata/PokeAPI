package com.sibb.pokepi.data.model

import androidx.room.Entity
import androidx.room.Index

/**
 * User-specific favorites entity for tracking which Pokemon each user has favorited.
 */
@Entity(
    tableName = "user_favorites",
    primaryKeys = ["userId", "pokemonId"],
    indices = [Index(value = ["userId"]), Index(value = ["pokemonId"])]
)
data class UserFavorite(
    val userId: String,
    val pokemonId: Int,
    val addedAt: Long = System.currentTimeMillis()
)
