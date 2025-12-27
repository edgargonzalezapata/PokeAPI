package com.sibb.pokepi.data.database

import androidx.paging.PagingSource
import androidx.room.*
import com.sibb.pokepi.data.model.Pokemon
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    
    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun getAllPokemonPaged(): PagingSource<Int, Pokemon>
    
    @Query("SELECT * FROM pokemon WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoritesPokemon(): Flow<List<Pokemon>>
    
    @Query("SELECT * FROM pokemon WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoritePokemonPaged(): PagingSource<Int, Pokemon>
    
    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemonById(id: Int): Pokemon?
    
    @Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchPokemon(query: String): Flow<List<Pokemon>>
    
    @Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchPokemonByNamePaged(query: String): PagingSource<Int, Pokemon>
    
    @Query("SELECT * FROM pokemon WHERE types LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchPokemonByTypePaged(query: String): PagingSource<Int, Pokemon>
    
    @Query("SELECT * FROM pokemon WHERE types LIKE '%' || :query || '%' ORDER BY id ASC")
    suspend fun searchPokemonByType(query: String): List<Pokemon>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: Pokemon)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPokemon(pokemon: List<Pokemon>)
    
    // Insert Pokemon preserving local data (favorites, view count, etc.)
    suspend fun insertPokemonPreservingLocalData(pokemon: Pokemon) {
        val existingPokemon = getPokemonById(pokemon.id)
        if (existingPokemon != null) {
            // Preserve local data when updating from API
            val pokemonWithLocalData = pokemon.copy(
                isFavorite = existingPokemon.isFavorite,
                viewCount = existingPokemon.viewCount,
                firstSeenAt = existingPokemon.firstSeenAt
            )
            insertPokemon(pokemonWithLocalData)
        } else {
            // First time inserting, use API data as-is
            insertPokemon(pokemon)
        }
    }
    
    @Update
    suspend fun updatePokemon(pokemon: Pokemon)
    
    @Transaction
    @Query("UPDATE pokemon SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
    
    @Query("UPDATE pokemon SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: Int)
    
    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getTotalPokemonCount(): Int
    
    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getPokemonCount(): Int
    
    @Query("SELECT COUNT(*) FROM pokemon WHERE isFavorite = 1")
    suspend fun getFavoritesCount(): Int
    
    @Query("SELECT SUM(viewCount) FROM pokemon")
    suspend fun getTotalViewCount(): Int
    
    @Query("SELECT DISTINCT types FROM pokemon WHERE types IS NOT NULL AND types != ''")
    suspend fun getAllTypes(): List<String>
    
    @Delete
    suspend fun deletePokemon(pokemon: Pokemon)
    
    @Query("DELETE FROM pokemon")
    suspend fun clearAll()
}