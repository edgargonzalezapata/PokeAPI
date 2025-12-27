package com.sibb.pokepi.presentation.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun LocalRegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: LocalAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var enableBiometric by remember { mutableStateOf(false) }
    
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    
    // Check biometric capability
    LaunchedEffect(Unit) {
        viewModel.checkBiometricCapability(context)
    }
    
    // Handle registration success
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegisterSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Crear tu cuenta local",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Crea credenciales para acceder sin conexión",
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
                label = { Text("Contraseña (mín. 4 caracteres)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "👁️" else "🙈",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                enabled = !uiState.isLoading,
                isError = password.isNotEmpty() && password.length < 4
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "👁️" else "🙈",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                enabled = !uiState.isLoading,
                isError = confirmPassword.isNotEmpty() && confirmPassword != password
            )
            
            // Password validation messages
            if (password.isNotEmpty() && password.length < 4) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "La contraseña debe tener al menos 4 caracteres",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Biometric option (if available)
            if (uiState.biometricCapability == BiometricCapability.AVAILABLE) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = enableBiometric,
                        onCheckedChange = { enableBiometric = it },
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habilitar autenticación biométrica",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Register button
            val canRegister = username.isNotBlank() && 
                            password.length >= 4 && 
                            confirmPassword == password && 
                            !uiState.isLoading
            
            Button(
                onClick = { 
                    keyboardController?.hide()
                    viewModel.registerLocal(username, password, enableBiometric)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canRegister
            ) {
                if (uiState.isLoading) {
                    PokeBallLoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        size = 20,
                        showText = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Crear Cuenta")
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
}