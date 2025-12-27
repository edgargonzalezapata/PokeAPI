package com.sibb.pokepi.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sibb.pokepi.data.model.GitHubUser
import com.sibb.pokepi.data.repository.BiometricCapability

@Composable
fun ProfileScreen(
    user: GitHubUser? = null,
    localUsername: String? = null,
    onLogout: () -> Unit,
    onNotificationSettingsClick: () -> Unit = {}
) {
    val localAuthViewModel: LocalAuthViewModel = hiltViewModel()
    val localUiState by localAuthViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        localAuthViewModel.checkBiometricCapability(context)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    user != null -> {
                        // GitHub user profile
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Avatar de ${user.login}",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Text(
                            text = user.name ?: user.login,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "@${user.login}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "🔗 Cuenta GitHub",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        
                        if (!user.bio.isNullOrEmpty()) {
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        if (!user.email.isNullOrEmpty()) {
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    localUsername != null -> {
                        // Local user profile
                        Card(
                            modifier = Modifier
                                .size(100.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = localUsername.first().uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Text(
                            text = localUsername,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "📱 Cuenta Local",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        
                        Text(
                            text = "Sesión iniciada localmente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = "No hay información de usuario disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Show GitHub stats only for GitHub users
        if (user != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Repositorios",
                    value = user.publicRepos.toString()
                )
                
                StatCard(
                    title = "Seguidores",
                    value = user.followers.toString()
                )
                
                StatCard(
                    title = "Siguiendo",
                    value = user.following.toString()
                )
            }
        }
        
        // Biometric Settings for local users
        if (localUsername != null && localUiState.biometricCapability == BiometricCapability.AVAILABLE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚙️ Configuración de Seguridad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Autenticación Biométrica",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (localUiState.isBiometricEnabled) "Habilitada" else "Deshabilitada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = localUiState.isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    localAuthViewModel.enableBiometricForExistingUser()
                                } else {
                                    localAuthViewModel.disableBiometric()
                                }
                            }
                        )
                    }
                    
                    if (localUiState.isBiometricEnabled) {
                        Text(
                            text = "🔒 Puedes usar tu huella dactilar o rostro para iniciar sesión",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Notification Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Configurar Notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pokémon del día, favoritos y más",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OutlinedButton(
                    onClick = onNotificationSettingsClick
                ) {
                    Text("Configurar")
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}