package com.sibb.pokepi.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Mostrar error si existe
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un SnackBar o Toast
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Notificaciones",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                NotificationHeaderCard()
                
                // Configuración principal
                NotificationSettingCard(
                    title = "Notificaciones",
                    description = "Activar/desactivar todas las notificaciones",
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )

                // Configuraciones específicas (solo si las notificaciones están activas)
                if (uiState.notificationsEnabled) {
                    NotificationSettingCard(
                        title = "Pokémon del Día",
                        description = "Recibe notificaciones diarias con un Pokémon destacado",
                        emoji = "🌟",
                        checked = uiState.pokemonOfDayEnabled,
                        onCheckedChange = viewModel::togglePokemonOfDay
                    )

                    NotificationSettingCard(
                        title = "Actualizaciones de Favoritos",
                        description = "Notificaciones sobre tus Pokémon favoritos",
                        emoji = "⭐",
                        checked = uiState.favoriteUpdatesEnabled,
                        onCheckedChange = viewModel::toggleFavoriteUpdates
                    )

                    NotificationSettingCard(
                        title = "Actualizaciones de App",
                        description = "Nuevas funciones y actualizaciones importantes",
                        emoji = "🚀",
                        checked = uiState.appUpdatesEnabled,
                        onCheckedChange = viewModel::toggleAppUpdates
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón de prueba (solo en debug o para testing)
                OutlinedButton(
                    onClick = viewModel::sendTestNotification,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.notificationsEnabled
                ) {
                    Text("Enviar Notificación de Prueba")
                }
            }
        }
    }
}

@Composable
private fun NotificationHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔔 Mantente al día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configura qué notificaciones quieres recibir para no perderte ninguna novedad del mundo Pokémon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NotificationSettingCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono o emoji
            if (emoji != null) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.size(32.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}