package com.sibb.pokepi.data.repository

import com.sibb.pokepi.data.api.GitHubApiClient
import com.sibb.pokepi.data.model.GitHubUser
import com.sibb.pokepi.data.network.NetworkModule
import com.sibb.pokepi.data.storage.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepository(private val tokenStorage: TokenStorage) {
    
    private val githubApiService = NetworkModule.githubApiService
    private val githubOAuthService = NetworkModule.githubOAuthService
    
    suspend fun exchangeCodeForToken(code: String): Result<String> {
        return try {
            val response = githubOAuthService.getAccessToken(
                clientId = GitHubApiClient.CLIENT_ID,
                clientSecret = GitHubApiClient.CLIENT_SECRET,
                code = code,
                redirectUri = GitHubApiClient.REDIRECT_URI
            )
            
            if (response.isSuccessful && response.body() != null) {
                val accessToken = response.body()!!.accessToken
                tokenStorage.saveAccessToken(accessToken)
                Result.success(accessToken)
            } else {
                Result.failure(Exception("Error al obtener el token: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<GitHubUser> {
        return try {
            val token = tokenStorage.getAccessToken().first()
            if (token == null) {
                return Result.failure(Exception("No hay token de acceso"))
            }
            
            val response = githubApiService.getCurrentUser("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenStorage.saveUserLogin(user.login)
                Result.success(user)
            } else {
                Result.failure(Exception("Error al obtener usuario: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        tokenStorage.clearTokens()
    }
    
    fun isLoggedIn(): Flow<Boolean> {
        return tokenStorage.getAccessToken().map { token ->
            !token.isNullOrEmpty()
        }
    }
    
    fun getAuthUrl(): String = GitHubApiClient.getAuthUrl()
}