package com.sibb.pokepi.presentation.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.sibb.pokepi.data.repository.BiometricCapability
import com.sibb.pokepi.ui.components.PokeBallLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalLoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onGithubAuthClick: () -> Unit,
    viewModel: LocalAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fragmentActivity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is FragmentActivity) {
                return@remember ctx
            }
            ctx = ctx.baseContext
        }
        null
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    
    // Check biometric capability
    LaunchedEffect(Unit) {
        println("DEBUG: LocalLoginScreen inicializado")
        println("DEBUG: Context tipo: ${context::class.simpleName}")
        println("DEBUG: FragmentActivity encontrado: ${fragmentActivity != null}")
        viewModel.checkBiometricCapability(context)
    }
    
    // Debug UI state changes
    LaunchedEffect(uiState.biometricCapability, uiState.hasLocalAccount, uiState.isBiometricEnabled) {
        println("DEBUG: UI State cambió - hasLocalAccount: ${uiState.hasLocalAccount}, biometricCapability: ${uiState.biometricCapability}, isBiometricEnabled: ${uiState.isBiometricEnabled}")
    }
    
    
    // Handle login success
    LaunchedEffect(uiState.isLocalLoggedIn) {
        if (uiState.isLocalLoggedIn) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ingresa con tu cuenta local",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(usernameFocusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    keyboardController?.hide()
                    if (username.isNotBlank() && password.isNotBlank()) {
                        viewModel.loginLocal(username, password)
                    }
                }
            ),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "👁️" else "🙈",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login button
        Button(
            onClick = { 
                keyboardController?.hide()
                viewModel.loginLocal(username, password) 
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                PokeBallLoadingIndicator(
                    modifier = Modifier.size(20.dp),
                    size = 20,
                    showText = false
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Iniciar Sesión")
        }
        
        
        // Biometric options
        if (uiState.biometricCapability == BiometricCapability.AVAILABLE && uiState.hasLocalAccount) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.isBiometricEnabled) {
                println("DEBUG: Mostrando botón de biometría - habilitado: ${!uiState.isLoading}")
                // Biometric login button
                OutlinedButton(
                    onClick = {
                        println("DEBUG: Botón biométrico presionado")
                        fragmentActivity?.let { fActivity ->
                            println("DEBUG: FragmentActivity encontrado: ${fActivity::class.simpleName}")
                            viewModel.authenticateWithBiometric(
                                fActivity,
                                onLoginSuccess
                            )
                        } ?: println("DEBUG: FragmentActivity es null")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        PokeBallLoadingIndicator(
                            modifier = Modifier.size(16.dp),
                            size = 16,
                            showText = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autenticando...")
                    } else {
                        Text("🔒")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar Biometría")
                    }
                }
            } else {
                // Enable biometric button
                OutlinedButton(
                    onClick = { viewModel.enableBiometricForExistingUser() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("🔓")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Habilitar Biometría")
                }
            }
        }
        
        // Show biometric capability status for debugging
        if (uiState.biometricCapability != BiometricCapability.AVAILABLE && uiState.hasLocalAccount) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = when (uiState.biometricCapability) {
                        BiometricCapability.NO_HARDWARE -> "⚠️ Este dispositivo no tiene hardware biométrico"
                        BiometricCapability.HARDWARE_UNAVAILABLE -> "⚠️ Hardware biométrico no disponible"
                        BiometricCapability.NO_BIOMETRICS_ENROLLED -> "⚠️ No hay biometrías registradas en el dispositivo"
                        BiometricCapability.SECURITY_UPDATE_REQUIRED -> "⚠️ Se requiere actualización de seguridad"
                        BiometricCapability.UNSUPPORTED -> "⚠️ Biometría no soportada"
                        else -> "🔍 Verificando capacidad biométrica..."
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Instructions and Register link
        if (!uiState.hasLocalAccount) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "💡 Tip: Después de crear tu cuenta, podrás habilitar la autenticación biométrica en tu perfil para acceder más rápidamente.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onRegisterClick,
                enabled = !uiState.isLoading
            ) {
                Text("¿No tienes cuenta? Crear una")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " O ",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // GitHub login button
        OutlinedButton(
            onClick = onGithubAuthClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Continuar con GitHub")
        }
        
        // Error message
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}