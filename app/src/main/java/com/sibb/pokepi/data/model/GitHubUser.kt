package com.sibb.pokepi.data.model

import com.google.gson.annotations.SerializedName

data class GitHubUser(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("login")
    val login: String,
    
    @SerializedName("avatar_url")
    val avatarUrl: String,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("bio")
    val bio: String?,
    
    @SerializedName("public_repos")
    val publicRepos: Int,
    
    @SerializedName("followers")
    val followers: Int,
    
    @SerializedName("following")
    val following: Int
)

data class GitHubAccessToken(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("token_type")
    val tokenType: String,
    
    @SerializedName("scope")
    val scope: String?
)