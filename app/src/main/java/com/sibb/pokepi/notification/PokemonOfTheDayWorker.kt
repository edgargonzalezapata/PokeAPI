package com.sibb.pokepi.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sibb.pokepi.MainActivity
import com.sibb.pokepi.R
import com.sibb.pokepi.data.repository.PokemonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random

@HiltWorker
class PokemonOfTheDayWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pokemonRepository: PokemonRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "PokemonOfTheDayWorker"
        private const val CHANNEL_ID = "pokemon_of_the_day"
        private const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "pokemon_of_the_day_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // Verificar si las notificaciones están habilitadas
            val notificationsEnabled = notificationRepository.notificationsEnabled.first()
            val pokemonOfDayEnabled = notificationRepository.pokemonOfDayEnabled.first()
            
            if (!notificationsEnabled || !pokemonOfDayEnabled) {
                Log.d(TAG, "Notificaciones deshabilitadas, saltando trabajo")
                return Result.success()
            }

            // Obtener un Pokémon aleatorio
            val randomId = Random.nextInt(1, 1011) // Pokédex hasta la generación 7
            val pokemonResult = pokemonRepository.getPokemonDetails(randomId)
            
            pokemonResult.fold(
                onSuccess = { pokemon ->
                    val pokemonName = pokemon.name.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    }
                    
                    val types = pokemon.types.joinToString(", ") { 
                        it.type.name.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase() else char.toString() 
                        }
                    }
                    
                    sendPokemonOfTheDayNotification(
                        pokemonName = pokemonName,
                        pokemonId = pokemon.id,
                        types = types
                    )
                    
                    Log.d(TAG, "Notificación enviada para Pokémon: $pokemonName")
                },
                onFailure = { error ->
                    Log.e(TAG, "Error obteniendo Pokémon del día", error)
                    return Result.retry()
                }
            )
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en PokemonOfTheDayWorker", e)
            Result.failure()
        }
    }

    private fun sendPokemonOfTheDayNotification(
        pokemonName: String,
        pokemonId: Int,
        types: String
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("pokemon_of_the_day_id", pokemonId)
            putExtra("open_detail", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🌟 Pokémon del Día"
        val body = "$pokemonName te espera! Un Pokémon tipo $types listo para ser descubierto."

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.pokemon_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(applicationContext.getColor(R.color.pokemon_fire))
            .addAction(
                R.drawable.pokemon_icon,
                "Ver Pokémon",
                pendingIntent
            )

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal para Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pokémon del Día",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones diarias con un Pokémon destacado"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}