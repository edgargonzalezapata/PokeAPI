package com.sibb.pokepi.di

import android.content.Context
import androidx.room.Room
import com.sibb.pokepi.data.database.PokeDatabase
import com.sibb.pokepi.data.database.PokemonDao
import com.sibb.pokepi.data.database.UserStatsDao
import com.sibb.pokepi.data.database.UserFavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun providePokeDatabase(@ApplicationContext context: Context): PokeDatabase {
        return Room.databaseBuilder(
            context,
            PokeDatabase::class.java,
            PokeDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun providePokemonDao(database: PokeDatabase): PokemonDao {
        return database.pokemonDao()
    }
    
    @Provides
    fun provideUserStatsDao(database: PokeDatabase): UserStatsDao {
        return database.userStatsDao()
    }
    
    @Provides
    fun provideUserFavoriteDao(database: PokeDatabase): UserFavoriteDao {
        return database.userFavoriteDao()
    }
}