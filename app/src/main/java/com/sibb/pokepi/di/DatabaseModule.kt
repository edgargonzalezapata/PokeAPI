package com.sibb.pokepi.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `user_favorites` (`userId` TEXT NOT NULL, `pokemonId` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `pokemonId`))"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_user_favorites_userId` ON `user_favorites` (`userId`)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_user_favorites_pokemonId` ON `user_favorites` (`pokemonId`)"
            )
        }
    }
    
    @Provides
    @Singleton
    fun providePokeDatabase(@ApplicationContext context: Context): PokeDatabase {
        return Room.databaseBuilder(
            context,
            PokeDatabase::class.java,
            PokeDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
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