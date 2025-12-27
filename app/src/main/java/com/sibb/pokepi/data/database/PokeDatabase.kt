package com.sibb.pokepi.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.UserStats
import com.sibb.pokepi.data.model.UserFavorite

@Database(
    entities = [Pokemon::class, UserStats::class, UserFavorite::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokeDatabase : RoomDatabase() {
    
    abstract fun pokemonDao(): PokemonDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun userFavoriteDao(): UserFavoriteDao
    
    companion object {
        const val DATABASE_NAME = "poke_database"
    }
}