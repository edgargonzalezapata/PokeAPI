package com.sibb.pokepi.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.sibb.pokepi.R
import com.sibb.pokepi.ui.components.CenterLoading
import com.sibb.pokepi.ui.components.PokeBallLoadingIndicator
import com.sibb.pokepi.ui.components.FullScreenLoading
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.sibb.pokepi.data.model.Pokemon
import com.sibb.pokepi.data.model.UserStats
import com.sibb.pokepi.presentation.detail.PokemonDetailModal
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPokemonClick: (Pokemon) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel(),
    currentUserId: String? = null,
    pokemonToShow: Int? = null,
    onPokemonShown: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pokemonItems = viewModel.pokemonFeed.collectAsLazyPagingItems()
    val userStats by viewModel.userStats.collectAsState(initial = UserStats())
    
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("name") } // "name", "type"
    var showTypeDropdown by remember { mutableStateOf(false) }
    var selectedPokemon by remember { mutableStateOf<Pokemon?>(null) }
    
    // Monitor loading state to control pokeball visibility
    val isLoadingResults = pokemonItems.loadState.refresh is LoadState.Loading
    LaunchedEffect(isLoadingResults, uiState.selectedType, uiState.searchQuery) {
        if (!isLoadingResults && (uiState.selectedType.isNotEmpty() || uiState.searchQuery.isNotEmpty())) {
            // Hide pokeball when loading is complete and we have search results
            viewModel.setSearchingState(false)
        }
    }

    // Clear favorites when user changes
    LaunchedEffect(currentUserId) {
        println("FeedScreen - User changed to: $currentUserId, clearing favorite states")
        viewModel.clearFavoriteStatus()
    }
    
    // Handle Pokemon to show from notification
    LaunchedEffect(pokemonToShow) {
        println("FeedScreen - LaunchedEffect pokemonToShow: $pokemonToShow")
        pokemonToShow?.let { pokemonId ->
            println("FeedScreen - Received Pokemon to show from notification: $pokemonId")
            // Obtener el Pokemon del ViewModel/Repository y mostrarlo
            viewModel.getPokemonById(pokemonId) { pokemon ->
                if (pokemon != null) {
                    selectedPokemon = pokemon
                    println("FeedScreen - Showing Pokemon from notification: ${pokemon.name}")
                    onPokemonShown() // Limpiar el pending
                } else {
                    println("FeedScreen - Could not find Pokemon with ID: $pokemonId")
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
                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = "🔥 Feed Pokémon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Search Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search input based on type
                when (searchType) {
                    "name" -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.updateSearchQuery(it, searchType)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar por nombre...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { 
                                            searchQuery = ""
                                            viewModel.clearSearch()
                                        }
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }
                    "type" -> {
                        // Type dropdown
                        Box {
                            OutlinedTextField(
                                value = uiState.selectedType.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                },
                                onValueChange = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTypeDropdown = true },
                                placeholder = { Text("Seleccionar tipo...") },
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                trailingIcon = {
                                    Row {
                                        if (uiState.selectedType.isNotEmpty()) {
                                            IconButton(
                                                onClick = { 
                                                    viewModel.clearSearch()
                                                }
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                            }
                                        }
                                        Icon(
                                            Icons.Default.ArrowDropDown, 
                                            contentDescription = "Abrir lista",
                                            modifier = Modifier.clickable { showTypeDropdown = true }
                                        )
                                    }
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                uiState.availableTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                getTypeDisplayName(type)
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.updateSelectedType(type)
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Search type selector and loading indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            onClick = { 
                                searchType = "name"
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.updateSearchQuery(searchQuery, searchType)
                                }
                            },
                            label = { Text("Nombre") },
                            selected = searchType == "name"
                        )
                        
                        FilterChip(
                            onClick = { 
                                searchType = "type"
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.updateSearchQuery(searchQuery, searchType)
                                }
                            },
                            label = { Text("Tipo") },
                            selected = searchType == "type"
                        )
                    }
                    
                    // Searching indicator - positioned to the right
                    if (uiState.isSearching) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            PokeBallLoadingIndicator()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Buscando...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        // Show loading screen when refreshing (initial load or filter changes)
        when (pokemonItems.loadState.refresh) {
            is LoadState.Loading -> {
                FullScreenLoading(
                    text = if (uiState.selectedType.isNotEmpty() || uiState.searchQuery.isNotEmpty()) {
                        "Aplicando filtros..."
                    } else {
                        "Cargando Pokémon..."
                    }
                )
            }
            is LoadState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "😞",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error al cargar Pokémon",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { pokemonItems.retry() }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // User Stats Header
                    item {
                        UserStatsCard(
                            userStats = userStats,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Pokemon Feed
                    items(
                        count = pokemonItems.itemCount,
                        key = { index -> pokemonItems.peek(index)?.id ?: index }
                    ) { index ->
                        val pokemon = pokemonItems[index]
                        pokemon?.let {
                            // Load favorite status if not already loaded
                            LaunchedEffect(it.id, currentUserId) {
                                currentUserId?.let { userId ->
                                    if (!uiState.favoriteStatus.containsKey(it.id)) {
                                        viewModel.loadFavoriteStatus(it.id, userId)
                                    }
                                }
                            }
                            
                            val isFavorite = uiState.favoriteStatus[it.id] ?: false
                            
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

                    // Loading states for pagination
                    when (pokemonItems.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                CenterLoading(text = "Cargando más Pokémon...")
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
                                        text = "Error loading more Pokémon",
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
    }
    
    // Modal de detalle del Pokémon
    selectedPokemon?.let { pokemon ->
        val isFavorite = uiState.favoriteStatus[pokemon.id] ?: false
        
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
private fun UserStatsCard(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Tus Estadísticas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    title = "Vistos",
                    value = userStats.totalPokemonSeen.toString(),
                    icon = "👁️",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    title = "Favoritos",
                    value = userStats.totalFavorites.toString(),
                    icon = "❤️",
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    title = "Tiempo",
                    value = formatTime(userStats.totalTimeSpent),
                    icon = "⏱️",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
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

private fun formatTime(milliseconds: Long): String {
    val minutes = milliseconds / 60000
    return when {
        minutes < 60 -> "${minutes}m"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}


private fun getTypeDisplayName(type: String): String {
    return when (type) {
        "fire" -> "🔥 Fuego"
        "water" -> "💧 Agua"
        "grass" -> "🌱 Planta"
        "electric" -> "⚡ Eléctrico"
        "psychic" -> "🔮 Psíquico"
        "ice" -> "❄️ Hielo"
        "dragon" -> "🐉 Dragón"
        "dark" -> "🌙 Siniestro"
        "fairy" -> "🧚 Hada"
        "normal" -> "⚪ Normal"
        "fighting" -> "👊 Lucha"
        "poison" -> "☠️ Veneno"
        "ground" -> "🌍 Tierra"
        "flying" -> "🦅 Volador"
        "bug" -> "🐛 Bicho"
        "rock" -> "🗿 Roca"
        "ghost" -> "👻 Fantasma"
        "steel" -> "⚙️ Acero"
        else -> type.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}