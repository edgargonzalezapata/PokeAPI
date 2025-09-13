package com.sibb.pokepi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import com.sibb.pokepi.presentation.auth.AuthViewModel
import com.sibb.pokepi.presentation.auth.LoginScreen
import com.sibb.pokepi.presentation.auth.LocalAuthViewModel
import com.sibb.pokepi.presentation.auth.LocalLoginScreen
import com.sibb.pokepi.presentation.auth.LocalRegisterScreen
import com.sibb.pokepi.presentation.auth.ProfileScreen
import com.sibb.pokepi.presentation.home.HomeScreen
import com.sibb.pokepi.presentation.feed.FeedScreen
import com.sibb.pokepi.presentation.favorites.FavoritesScreen
import com.sibb.pokepi.presentation.notifications.NotificationSettingsScreen
import com.sibb.pokepi.ui.theme.PokePITheme
import com.sibb.pokepi.ui.components.PokeBallLoadingIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokePITheme {
                PokeApp()
            }
        }
        
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    lateinit var authViewModel: AuthViewModel
    
    // Callback para manejar navegación desde notificaciones
    var onNavigationFromNotification: ((String, Int?) -> Unit)? = null
    
    // Variables para almacenar navegación pendiente
    internal var pendingNavigationTarget: String? = null
    internal var pendingNavigationPokemonId: Int? = null
    
    private fun handleIntent(intent: Intent) {
        println("MainActivity.handleIntent - Processing intent: $intent")
        println("MainActivity.handleIntent - Intent extras: ${intent.extras?.keySet()}")
        
        val uri = intent.data
        println("MainActivity.handleIntent - URI: $uri")
        if (uri != null && uri.scheme == "pokepi" && uri.host == "oauth") {
            val code = uri.getQueryParameter("code")
            println("MainActivity.handleIntent - OAuth code: $code")
            code?.let { authCode ->
                if (::authViewModel.isInitialized) {
                    println("MainActivity.handleIntent - Processing code with ViewModel")
                    authViewModel.handleAuthorizationCode(authCode)
                } else {
                    println("MainActivity.handleIntent - ViewModel not initialized, storing code")
                    pendingOauthCode = authCode
                }
            }
        }
        
        // Manejar navegación desde notificaciones - usando callback directo o almacenar si no está disponible
        val navigateTo = intent.getStringExtra("navigate_to")
        val pokemonId = intent.getIntExtra("pokemon_id", -1)
        
        println("MainActivity.handleIntent - navigateTo: $navigateTo, pokemonId: $pokemonId")
        
        if (navigateTo != null) {
            println("MainActivity.handleIntent - Navigation from notification: $navigateTo, Pokemon ID: $pokemonId")
            val finalPokemonId = if (pokemonId != -1) pokemonId else null
            
            if (onNavigationFromNotification != null) {
                println("MainActivity.handleIntent - Triggering navigation callback immediately: $navigateTo, $finalPokemonId")
                onNavigationFromNotification?.invoke(navigateTo, finalPokemonId)
            } else {
                println("MainActivity.handleIntent - Callback not available, storing for later: $navigateTo, $finalPokemonId")
                pendingNavigationTarget = navigateTo
                pendingNavigationPokemonId = finalPokemonId
            }
        } else {
            println("MainActivity.handleIntent - No navigation data found in intent")
        }
    }
    
    companion object {
        var pendingOauthCode: String? = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokeApp() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val localAuthViewModel: LocalAuthViewModel = hiltViewModel()
    
    // Navigation state - inicio siempre va al feed
    var currentScreen by remember { mutableStateOf("feed") }
    var authScreen by remember { mutableStateOf("local_login") } // "local_login", "local_register", "github_auth"
    var pendingPokemonToShow by remember { mutableStateOf<Int?>(null) }
    
    val uiState by authViewModel.uiState.collectAsState()
    val localUiState by localAuthViewModel.uiState.collectAsState()
    
    // Determine if user is logged in through any method
    val isLoggedIn = uiState.isLoggedIn || localUiState.isLocalLoggedIn
    
    // Asignar el ViewModel a la Activity y configurar callback de navegación
    LaunchedEffect(authViewModel) {
        val activity = context as? MainActivity
        activity?.authViewModel = authViewModel
        
        // Configurar callback para navegación desde notificaciones
        activity?.onNavigationFromNotification = { target, pokemonId ->
            println("MainActivity - Received navigation callback: target='$target', pokemonId=$pokemonId")
            if (isLoggedIn && target.isNotEmpty()) {
                when (target) {
                    "favorites" -> {
                        println("MainActivity - Navigating to favorites from callback")
                        currentScreen = "favorites"
                        pokemonId?.let { id ->
                            pendingPokemonToShow = id
                            println("MainActivity - Setting pending Pokemon ID: $id")
                        }
                    }
                }
            }
        }
        
        // Procesar navegación pendiente si existe
        activity?.let { act ->
            if (act.pendingNavigationTarget != null) {
                val target = act.pendingNavigationTarget!!
                val pokemonId = act.pendingNavigationPokemonId
                println("MainActivity - Processing pending navigation: $target, $pokemonId")
                
                act.onNavigationFromNotification?.invoke(target, pokemonId)
                
                // Limpiar navegación pendiente
                act.pendingNavigationTarget = null
                act.pendingNavigationPokemonId = null
            }
        }
        
        // Procesar código pendiente si existe
        MainActivity.pendingOauthCode?.let { code ->
            println("MainActivity - Processing pending OAuth code: $code")
            authViewModel.handleAuthorizationCode(code)
            MainActivity.pendingOauthCode = null
        }
    }
    
    // Manejar el botón de atrás - volver a feed (inicio) o perfil desde notificaciones
    BackHandler(enabled = currentScreen != "feed" && isLoggedIn) {
        currentScreen = if (currentScreen == "notifications") "profile" else "feed"
    }
    
    // Handle auth screen back navigation
    BackHandler(enabled = authScreen != "local_login" && !isLoggedIn) {
        authScreen = "local_login"
    }
    
    when {
        uiState.isLoading -> {
            println("MainActivity - Showing loading screen")
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    PokeBallLoadingIndicator(
                        size = 80,
                        text = "Autenticando..."
                    )
                }
            }
        }
        
        isLoggedIn -> {
            println("MainActivity - User is logged in")
            
            // Determine which user data to show
            val displayName = when {
                uiState.isLoggedIn && uiState.user != null -> uiState.user?.name ?: uiState.user?.login
                localUiState.isLocalLoggedIn -> localUiState.storedUsername
                else -> "Usuario"
            } ?: "Usuario"
            
            // Determine current user ID for favorites
            val currentUserId = when {
                uiState.isLoggedIn && uiState.user != null -> uiState.user?.login
                localUiState.isLocalLoggedIn -> localUiState.currentUserId
                else -> null
            }
            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "¡Hola, $displayName!",
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        actions = {
                            IconButton(onClick = { 
                                // Logout from both systems
                                authViewModel.logout()
                                localAuthViewModel.logoutLocal()
                                authScreen = "local_login"
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                                label = { Text("Inicio") },
                                selected = currentScreen == "feed",
                                onClick = { currentScreen = "feed" }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                                label = { Text("Favoritos") },
                                selected = currentScreen == "favorites",
                                onClick = { currentScreen = "favorites" }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                label = { Text("Perfil") },
                                selected = currentScreen == "profile",
                                onClick = { currentScreen = "profile" }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "feed" -> FeedScreen(
                                onPokemonClick = { /* TODO: Navigate to details */ },
                                currentUserId = currentUserId,
                                pokemonToShow = if (currentScreen == "feed") pendingPokemonToShow else null,
                                onPokemonShown = { pendingPokemonToShow = null }
                            )
                            "favorites" -> FavoritesScreen(
                                onPokemonClick = { /* TODO: Navigate to details */ },
                                currentUserId = currentUserId,
                                pokemonToShow = if (currentScreen == "favorites") pendingPokemonToShow else null,
                                onPokemonShown = { pendingPokemonToShow = null }
                            )
                            "profile" -> ProfileScreen(
                                user = uiState.user,
                                localUsername = localUiState.storedUsername,
                                onLogout = { 
                                    authViewModel.logout()
                                    localAuthViewModel.logoutLocal()
                                    authScreen = "local_login"
                                },
                                onNotificationSettingsClick = { 
                                    currentScreen = "notifications" 
                                }
                            )
                            "notifications" -> NotificationSettingsScreen(
                                onBackClick = { currentScreen = "profile" }
                            )
                        }
                        
                        uiState.error?.let { error ->
                            Snackbar(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                action = {
                                    TextButton(onClick = authViewModel::clearError) {
                                        Text("OK")
                                    }
                                }
                            ) {
                                Text(error)
                            }
                        }
                    }
                }
        }
        
        else -> {
            println("MainActivity - Showing auth screens")
            when (authScreen) {
                "local_login" -> LocalLoginScreen(
                    onLoginSuccess = { currentScreen = "feed" },
                    onRegisterClick = { authScreen = "local_register" },
                    onGithubAuthClick = { authScreen = "github_auth" }
                )
                "local_register" -> LocalRegisterScreen(
                    onBackClick = { authScreen = "local_login" },
                    onRegisterSuccess = { 
                        currentScreen = "feed" 
                        authScreen = "local_login"
                    }
                )
                "github_auth" -> {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            LoginScreen(
                                onAuthCodeReceived = authViewModel::handleAuthorizationCode,
                                authUrl = authViewModel.getAuthUrl()
                            )
                        }
                    }
                }
            }
        }
    }
}