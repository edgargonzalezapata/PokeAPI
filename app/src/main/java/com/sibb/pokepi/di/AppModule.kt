package com.sibb.pokepi.di

import android.content.Context
import com.sibb.pokepi.data.storage.TokenStorage
import com.sibb.pokepi.data.storage.LocalAuthStorage
import com.sibb.pokepi.notification.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage {
        return TokenStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideLocalAuthStorage(@ApplicationContext context: Context): LocalAuthStorage {
        return LocalAuthStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: Context): NotificationRepository {
        return NotificationRepository(context)
    }
}