package com.sibb.pokepi.data.model

/**
 * API response models for Pokemon Type endpoints.
 */
data class TypeListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TypeListItem>
)

data class TypeListItem(
    val name: String,
    val url: String
)

data class TypeResponse(
    val id: Int,
    val name: String,
    val pokemon: List<TypePokemonEntry>
)

data class TypePokemonEntry(
    val pokemon: PokemonListItem,
    val slot: Int
)
