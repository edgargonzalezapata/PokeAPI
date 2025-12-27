package com.sibb.pokepi.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sibb.pokepi.data.model.*

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPokemonSprites(sprites: PokemonSprites): String {
        return gson.toJson(sprites)
    }

    @TypeConverter
    fun toPokemonSprites(spritesJson: String): PokemonSprites {
        return gson.fromJson(spritesJson, PokemonSprites::class.java)
    }

    @TypeConverter
    fun fromPokemonAbilityList(abilities: List<PokemonAbility>): String {
        return gson.toJson(abilities)
    }

    @TypeConverter
    fun toPokemonAbilityList(abilitiesJson: String): List<PokemonAbility> {
        val listType = object : TypeToken<List<PokemonAbility>>() {}.type
        return gson.fromJson(abilitiesJson, listType)
    }

    @TypeConverter
    fun fromPokemonStatList(stats: List<PokemonStat>): String {
        return gson.toJson(stats)
    }

    @TypeConverter
    fun toPokemonStatList(statsJson: String): List<PokemonStat> {
        val listType = object : TypeToken<List<PokemonStat>>() {}.type
        return gson.fromJson(statsJson, listType)
    }

    @TypeConverter
    fun fromPokemonTypeList(types: List<PokemonType>): String {
        return gson.toJson(types)
    }

    @TypeConverter
    fun toPokemonTypeList(typesJson: String): List<PokemonType> {
        val listType = object : TypeToken<List<PokemonType>>() {}.type
        return gson.fromJson(typesJson, listType)
    }
}