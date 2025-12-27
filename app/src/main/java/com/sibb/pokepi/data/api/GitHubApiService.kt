package com.sibb.pokepi.data.api

import com.sibb.pokepi.data.model.GitHubAccessToken
import com.sibb.pokepi.data.model.GitHubUser
import retrofit2.Response
import retrofit2.http.*

interface GitHubApiService {
    
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<GitHubAccessToken>
    
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<GitHubUser>
}

object GitHubApiClient {
    const val BASE_URL = "https://api.github.com/"
    const val OAUTH_BASE_URL = "https://github.com/"
    
    const val CLIENT_ID = "Ov23likls6nNYIm78tC0"
    const val CLIENT_SECRET = "83e46e50a623232952bb793482e297042c3afe90" 
    const val REDIRECT_URI = "pokepi://oauth/callback"
    
    fun getAuthUrl(): String {
        return "${OAUTH_BASE_URL}login/oauth/authorize?" +
                "client_id=${CLIENT_ID}&" +
                "redirect_uri=${REDIRECT_URI}&" +
                "scope=user:email"
    }
}