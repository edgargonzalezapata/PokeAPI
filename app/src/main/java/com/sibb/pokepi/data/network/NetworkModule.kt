package com.sibb.pokepi.data.network

import com.sibb.pokepi.data.api.GitHubApiService
import com.sibb.pokepi.data.api.GitHubApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val githubApiService: GitHubApiService by lazy {
        createRetrofit(GitHubApiClient.BASE_URL).create(GitHubApiService::class.java)
    }
    
    val githubOAuthService: GitHubApiService by lazy {
        createRetrofit(GitHubApiClient.OAUTH_BASE_URL).create(GitHubApiService::class.java)
    }
}