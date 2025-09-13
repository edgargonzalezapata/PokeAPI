package com.sibb.pokepi.data.api

import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.PokemonListResponse
import com.sibb.pokepi.data.model.TypeResponse
import com.sibb.pokepi.data.model.TypeListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PokemonListResponse>
    
    @GET("pokemon/{id}")
    suspend fun getPokemonDetails(
        @Path("id") id: Int
    ): Response<Pokemon>
    
    @GET("pokemon/{name}")
    suspend fun getPokemonByName(
        @Path("name") name: String
    ): Response<Pokemon>
    
    @GET("type")
    suspend fun getAllTypes(): Response<TypeListResponse>
    
    @GET("type/{name}")
    suspend fun getPokemonByType(
        @Path("name") typeName: String
    ): Response<TypeResponse>
    
    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}