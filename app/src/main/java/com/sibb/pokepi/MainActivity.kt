package com.sibb.pokepi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sibb.pokepi.data.repository.AuthRepository
import com.sibb.pokepi.data.storage.TokenStorage
import com.sibb.pokepi.presentation.auth.AuthViewModel
import com.sibb.pokepi.presentation.auth.LoginScreen
import com.sibb.pokepi.presentation.auth.ProfileScreen
import com.sibb.pokepi.presentation.home.HomeScreen
import com.sibb.pokepi.ui.theme.PokePITheme

class MainActivity : ComponentActivity() {
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
    
    private fun handleIntent(intent: Intent) {
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
    }
    
    companion object {
        var pendingOauthCode: String? = null
    }
}

@Composable
fun PokeApp() {
    val context = LocalContext.current
    val tokenStorage = remember { TokenStorage(context) }
    val authRepository = remember { AuthRepository(tokenStorage) }
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    
    // Asignar el ViewModel a la Activity
    LaunchedEffect(authViewModel) {
        val activity = context as? MainActivity
        activity?.authViewModel = authViewModel
        
        // Procesar código pendiente si existe
        MainActivity.pendingOauthCode?.let { code ->
            println("MainActivity - Processing pending OAuth code: $code")
            authViewModel.handleAuthorizationCode(code)
            MainActivity.pendingOauthCode = null
        }
    }
    
    val uiState by authViewModel.uiState.collectAsState()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    println("MainActivity - Showing loading screen")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Autenticando...")
                        }
                    }
                }
                
                uiState.isLoggedIn -> {
                    println("MainActivity - User is logged in, user: ${uiState.user?.login}")
                    uiState.user?.let { user ->
                        HomeScreen(
                            user = user,
                            onLogout = authViewModel::logout
                        )
                    } ?: run {
                        println("MainActivity - User is logged in but user data is null")
                        LoginScreen(
                            onAuthCodeReceived = authViewModel::handleAuthorizationCode,
                            authUrl = authViewModel.getAuthUrl()
                        )
                    }
                }
                
                else -> {
                    println("MainActivity - Showing login screen")
                    LoginScreen(
                        onAuthCodeReceived = authViewModel::handleAuthorizationCode,
                        authUrl = authViewModel.getAuthUrl()
                    )
                }
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