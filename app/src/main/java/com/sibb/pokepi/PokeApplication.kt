package com.sibb.pokepi

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.sibb.pokepi.notification.NotificationRepository
import com.sibb.pokepi.notification.PokemonOfTheDayWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PokeApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var notificationRepository: NotificationRepository
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase and notifications
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.initializeFirebaseMessaging()
        }
        
        // Schedule Pokemon of the Day notifications
        schedulePokemonOfTheDayNotification()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    private fun schedulePokemonOfTheDayNotification() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val pokemonOfTheDayRequest = PeriodicWorkRequestBuilder<PokemonOfTheDayWorker>(
            24, TimeUnit.HOURS // Una vez al día
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Esperar 1 hora antes de la primera notificación
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PokemonOfTheDayWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            pokemonOfTheDayRequest
        )
    }
}