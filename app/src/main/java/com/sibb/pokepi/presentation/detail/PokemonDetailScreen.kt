package com.sibb.pokepi.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.sibb.pokepi.data.model.Pokemon
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailModal(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con imagen y controles
                PokemonDetailHeader(
                    pokemon = pokemon,
                    isFavorite = isFavorite,
                    onDismiss = onDismiss,
                    onFavoriteClick = onFavoriteClick
                )
                
                // Content scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información básica
                    PokemonBasicInfo(pokemon = pokemon)
                    
                    // Types
                    PokemonTypesSection(pokemon = pokemon)
                    
                    // Stats
                    PokemonStatsSection(pokemon = pokemon)
                    
                    // Habilidades
                    PokemonAbilitiesSection(pokemon = pokemon)
                }
            }
        }
    }
}

@Composable
private fun PokemonDetailHeader(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Background con gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            getTypeColor(pokemon.types.firstOrNull()?.type?.name ?: "normal"),
                            getTypeColor(pokemon.types.firstOrNull()?.type?.name ?: "normal").copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Botón cerrar
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.Black
            )
        }
        
        // Botón favorito
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorito",
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
        
        // Imagen del Pokémon
        AsyncImage(
            model = pokemon.sprites.other?.officialArtwork?.frontDefault 
                ?: pokemon.sprites.frontDefault,
            contentDescription = pokemon.name,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )
        
        // Nombre y número
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = pokemon.name.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                },
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PokemonBasicInfo(pokemon: Pokemon) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(
                title = "Altura",
                value = "${pokemon.height / 10.0}m",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                title = "Peso", 
                value = "${pokemon.weight / 10.0}kg",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                title = "Experiencia",
                value = "${pokemon.baseExperience ?: 0}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PokemonTypesSection(pokemon: Pokemon) {
    Column {
        Text(
            text = "Tipos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pokemon.types.forEach { pokemonType ->
                PokemonTypeChip(
                    type = pokemonType.type.name,
                    isLarge = true
                )
            }
        }
    }
}

@Composable
private fun PokemonStatsSection(pokemon: Pokemon) {
    Column {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        pokemon.stats.forEach { stat ->
            StatBar(
                name = getStatDisplayName(stat.stat.name),
                value = stat.baseStat,
                maxValue = 255
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBar(
    name: String,
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value.toFloat() / maxValue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = getStatColor(value),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PokemonAbilitiesSection(pokemon: Pokemon) {
    Column {
        Text(
            text = "Habilidades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        pokemon.abilities.forEach { ability ->
            AbilityChip(
                name = ability.ability.name,
                isHidden = ability.isHidden
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun AbilityChip(
    name: String,
    isHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isHidden) 
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (isHidden) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(Oculta)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun PokemonTypeChip(
    type: String,
    isLarge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getTypeColor(type)
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(if (isLarge) 20.dp else 16.dp)
    ) {
        Text(
            text = type.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            },
            modifier = Modifier.padding(
                horizontal = if (isLarge) 16.dp else 12.dp, 
                vertical = if (isLarge) 8.dp else 4.dp
            ),
            style = if (isLarge) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Color(0xFFFF6B6B)
        "water" -> Color(0xFF4ECDC4)
        "grass" -> Color(0xFF95E1D3)
        "electric" -> Color(0xFFFAD02E)
        "psychic" -> Color(0xFFEE5A6F)
        "ice" -> Color(0xFF6FBFFF)
        "dragon" -> Color(0xFF8B9DC3)
        "dark" -> Color(0xFF5D4E75)
        "fairy" -> Color(0xFFFFAAE0)
        "normal" -> Color(0xFFBDBDBD)
        "fighting" -> Color(0xFFFF8A80)
        "poison" -> Color(0xFFBA68C8)
        "ground" -> Color(0xFFBCAAA4)
        "flying" -> Color(0xFF90CAF9)
        "bug" -> Color(0xFFA5D6A7)
        "rock" -> Color(0xFFD7CCC8)
        "ghost" -> Color(0xFFB39DDB)
        "steel" -> Color(0xFFCFD8DC)
        else -> Color(0xFFE0E0E0)
    }
}

private fun getStatColor(value: Int): Color {
    return when {
        value >= 120 -> Color(0xFF4CAF50) // Verde
        value >= 80 -> Color(0xFFFF9800)  // Naranja
        value >= 50 -> Color(0xFFFFC107)  // Amarillo
        else -> Color(0xFFF44336)         // Rojo
    }
}

private fun getStatDisplayName(statName: String): String {
    return when (statName) {
        "hp" -> "HP"
        "attack" -> "Ataque"
        "defense" -> "Defensa"
        "special-attack" -> "Ataque Esp."
        "special-defense" -> "Defensa Esp."
        "speed" -> "Velocidad"
        else -> statName.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
}