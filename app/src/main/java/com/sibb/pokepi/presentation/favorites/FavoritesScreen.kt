package com.sibb.pokepi.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.ui.components.CenterLoading
import com.sibb.pokepi.presentation.detail.PokemonDetailModal
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPokemonClick: (Pokemon) -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel(),
    currentUserId: String? = null,
    pokemonToShow: Int? = null,
    onPokemonShown: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteItems = viewModel.favoritePokemons.collectAsLazyPagingItems()
    var selectedPokemon by remember { mutableStateOf<Pokemon?>(null) }

    LaunchedEffect(currentUserId) {
        println("FavoritesScreen - LaunchedEffect with currentUserId: $currentUserId")
        viewModel.clearFavoriteStatus()
        currentUserId?.let { userId ->
            println("FavoritesScreen - Setting userId in ViewModel: $userId")
            viewModel.setUserId(userId)
        }
    }
    
    // Handle Pokemon to show from notification
    LaunchedEffect(pokemonToShow) {
        println("FavoritesScreen - LaunchedEffect pokemonToShow: $pokemonToShow")
        pokemonToShow?.let { pokemonId ->
            println("FavoritesScreen - Received Pokemon to show from notification: $pokemonId")
            // Obtener el Pokemon del ViewModel/Repository y mostrarlo
            viewModel.getPokemonById(pokemonId) { pokemon ->
                if (pokemon != null) {
                    selectedPokemon = pokemon
                    println("FavoritesScreen - Showing Pokemon from notification: ${pokemon.name}")
                    onPokemonShown() // Limpiar el pending
                } else {
                    println("FavoritesScreen - Could not find Pokemon with ID: $pokemonId")
                    onPokemonShown() // Limpiar el pending incluso si falla
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "❤️ Mis Favoritos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (favoriteItems.itemCount == 0 && favoriteItems.loadState.refresh !is LoadState.Loading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "💔",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "No tienes Pokémon favoritos aún",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ve al feed y marca algunos Pokémon como favoritos",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Pokemon Favorites
                items(
                    count = favoriteItems.itemCount,
                    key = { index -> favoriteItems.peek(index)?.id ?: index }
                ) { index ->
                    val pokemon = favoriteItems[index]
                    pokemon?.let {
                        // Load favorite status if not already loaded
                        LaunchedEffect(it.id, currentUserId) {
                            currentUserId?.let { userId ->
                                if (!uiState.favoriteStatus.containsKey(it.id)) {
                                    viewModel.loadFavoriteStatus(it.id, userId)
                                }
                            }
                        }
                        
                        val isFavorite = uiState.favoriteStatus[it.id] ?: true // Default to true in favorites screen
                        
                        PokemonPostCard(
                            pokemon = it,
                            isFavorite = isFavorite,
                            onFavoriteClick = { 
                                currentUserId?.let { userId ->
                                    viewModel.toggleFavorite(it.id, userId)
                                }
                            },
                            onClick = { selectedPokemon = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Loading states
                when (favoriteItems.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            CenterLoading(text = "Cargando más favoritos...")
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = "Error loading favorites",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    
    // Modal de detalle del Pokémon
    selectedPokemon?.let { pokemon ->
        val isFavorite = uiState.favoriteStatus[pokemon.id] ?: true // Default true en favoritos
        
        PokemonDetailModal(
            pokemon = pokemon,
            isFavorite = isFavorite,
            onDismiss = { selectedPokemon = null },
            onFavoriteClick = {
                currentUserId?.let { userId ->
                    viewModel.toggleFavorite(pokemon.id, userId)
                }
            }
        )
    }
}

@Composable
private fun PokemonPostCard(
    pokemon: Pokemon,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Pokemon Image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = pokemon.sprites.other?.officialArtwork?.frontDefault 
                        ?: pokemon.sprites.frontDefault,
                    contentDescription = pokemon.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 100f
                            )
                        )
                )
                
                // Pokemon name overlay
                Text(
                    text = pokemon.name.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
                
                // Favorite button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }
            
            // Pokemon details
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Types
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pokemon.types.forEach { pokemonType ->
                        PokemonTypeChip(type = pokemonType.type.name)
                    }
                }
                
                // Abilities
                Text(
                    text = "Habilidades: ${pokemon.abilities.take(2).joinToString(", ") { 
                        it.ability.name.replaceFirstChar { 
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                        } 
                    }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Stats preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "HP: ${pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "ATK: ${pokemon.stats.find { it.stat.name == "attack" }?.baseStat ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DEF: ${pokemon.stats.find { it.stat.name == "defense" }?.baseStat ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (pokemon.viewCount > 0) {
                    Text(
                        text = "Visto ${pokemon.viewCount} ${if (pokemon.viewCount == 1) "vez" else "veces"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonTypeChip(
    type: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type.lowercase()) {
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
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = type.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}