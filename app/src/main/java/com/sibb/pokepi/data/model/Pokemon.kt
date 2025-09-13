package com.sibb.pokepi.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerializedName("base_experience")
    val baseExperience: Int?,
    val sprites: PokemonSprites,
    val abilities: List<PokemonAbility>,
    val stats: List<PokemonStat>,
    val types: List<PokemonType>,
    val isFavorite: Boolean = false,
    val viewCount: Int = 0,
    val firstSeenAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class PokemonSprites(
    @SerializedName("front_default")
    val frontDefault: String?,
    @SerializedName("front_shiny")
    val frontShiny: String?,
    @SerializedName("back_default")
    val backDefault: String?,
    val other: PokemonSpritesOther?
) : Parcelable

@Parcelize
data class PokemonSpritesOther(
    @SerializedName("official-artwork")
    val officialArtwork: PokemonOfficialArtwork?,
    @SerializedName("dream_world")
    val dreamWorld: PokemonDreamWorld?
) : Parcelable

@Parcelize
data class PokemonOfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String?
) : Parcelable

@Parcelize
data class PokemonDreamWorld(
    @SerializedName("front_default")
    val frontDefault: String?
) : Parcelable

@Parcelize
data class PokemonAbility(
    val ability: AbilityInfo,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
) : Parcelable

@Parcelize
data class AbilityInfo(
    val name: String,
    val url: String
) : Parcelable

@Parcelize
data class PokemonStat(
    @SerializedName("base_stat")
    val baseStat: Int,
    val effort: Int,
    val stat: StatInfo
) : Parcelable

@Parcelize
data class StatInfo(
    val name: String,
    val url: String
) : Parcelable

@Parcelize
data class PokemonType(
    val slot: Int,
    val type: TypeInfo
) : Parcelable

@Parcelize
data class TypeInfo(
    val name: String,
    val url: String
) : Parcelable

// Response models
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.split("/").dropLast(1).last().toInt()
}

// User statistics
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val totalPokemonSeen: Int = 0,
    val totalFavorites: Int = 0,
    val totalTimeSpent: Long = 0, // in milliseconds
    val lastActiveTime: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

// User-specific favorites
@Entity(
    tableName = "user_favorites",
    primaryKeys = ["userId", "pokemonId"],
    indices = [androidx.room.Index(value = ["userId"]), androidx.room.Index(value = ["pokemonId"])]
)
data class UserFavorite(
    val userId: String,
    val pokemonId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

// Type API responses
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