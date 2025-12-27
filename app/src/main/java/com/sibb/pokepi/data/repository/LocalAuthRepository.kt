package com.sibb.pokepi.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sibb.pokepi.data.storage.LocalAuthStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAuthRepository @Inject constructor(
    private val localAuthStorage: LocalAuthStorage
) {
    
    suspend fun registerLocalUser(username: String, password: String): Result<Unit> {
        return try {
            if (username.isBlank() || password.length < 4) {
                return Result.failure(Exception("Usuario debe tener contenido y contraseña mínimo 4 caracteres"))
            }
            
            localAuthStorage.saveLocalCredentials(username, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginLocal(username: String, password: String): Result<Unit> {
        return try {
            if (localAuthStorage.validateCredentials(username, password)) {
                localAuthStorage.setLocalLoggedIn(true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logoutLocal() {
        localAuthStorage.setLocalLoggedIn(false)
    }
    
    suspend fun setLocalLoggedIn(isLoggedIn: Boolean) {
        localAuthStorage.setLocalLoggedIn(isLoggedIn)
    }
    
    suspend fun enableBiometric() {
        localAuthStorage.setBiometricEnabled(true)
    }
    
    suspend fun disableBiometric() {
        localAuthStorage.setBiometricEnabled(false)
    }
    
    fun isLocalAuthEnabled(): Flow<Boolean> {
        return localAuthStorage.isLocalAuthEnabled()
    }
    
    fun isLocalLoggedIn(): Flow<Boolean> {
        return localAuthStorage.isLocalLoggedIn()
    }
    
    fun isBiometricEnabled(): Flow<Boolean> {
        return localAuthStorage.isBiometricEnabled()
    }
    
    fun getStoredUsername(): Flow<String?> {
        return localAuthStorage.getStoredUsername()
    }
    
    fun canUseBiometric(context: Context): BiometricCapability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NO_BIOMETRICS_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricCapability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricCapability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricCapability.UNKNOWN
            else -> BiometricCapability.UNKNOWN
        }
    }
    
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        println("DEBUG: Repository.authenticateWithBiometric llamado")
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED -> {
                                onError("Autenticación cancelada")
                            }
                            BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                                onError("No hay biometrías registradas en el dispositivo")
                            }
                            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                                onError("Hardware biométrico no disponible")
                            }
                            else -> {
                                onError("Error de autenticación: $errString")
                            }
                        }
                    }
                    
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Autenticación biométrica fallida. Intenta de nuevo.")
                    }
                })
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Biométrica")
                .setSubtitle("Usa tu huella dactilar o reconocimiento facial para acceder")
                .setNegativeButtonText("Cancelar")
                .build()
            
            println("DEBUG: A punto de llamar biometricPrompt.authenticate()")
            biometricPrompt.authenticate(promptInfo)
            println("DEBUG: biometricPrompt.authenticate() llamado")
        } catch (e: Exception) {
            onError("Error al inicializar autenticación biométrica: ${e.message}")
        }
    }
    
    suspend fun clearAllLocalData() {
        localAuthStorage.clearLocalAuth()
    }
}

enum class BiometricCapability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NO_BIOMETRICS_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}