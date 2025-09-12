package com.sibb.pokepi.data.repository

import com.sibb.pokepi.data.api.GitHubApiClient
import com.sibb.pokepi.data.api.GitHubApiService
import com.sibb.pokepi.data.model.GitHubUser
import com.sibb.pokepi.data.storage.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val tokenStorage: TokenStorage,
    @Named("github") private val githubApiService: GitHubApiService,
    @Named("github_oauth") private val githubOAuthRetrofit: Retrofit
) {
    
    private val githubOAuthService = githubOAuthRetrofit.create(GitHubApiService::class.java)
    
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
        // Solo limpiar tokens, NO limpiar datos de la base de datos (favoritos, stats, etc.)
        tokenStorage.clearTokens()
    }
    
    fun isLoggedIn(): Flow<Boolean> {
        return tokenStorage.getAccessToken().map { token ->
            !token.isNullOrEmpty()
        }
    }
    
    fun getAuthUrl(): String = GitHubApiClient.getAuthUrl()
}