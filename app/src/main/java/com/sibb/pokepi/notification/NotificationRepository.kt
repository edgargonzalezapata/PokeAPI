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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessaging
import com.sibb.pokepi.MainActivity
import com.sibb.pokepi.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

@Singleton
class NotificationRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "NotificationRepository"
        
        // Keys para DataStore
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val POKEMON_OF_DAY_ENABLED = booleanPreferencesKey("pokemon_of_day_enabled")
        private val FAVORITE_UPDATES_ENABLED = booleanPreferencesKey("favorite_updates_enabled")
        private val APP_UPDATES_ENABLED = booleanPreferencesKey("app_updates_enabled")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    // Flow para observar configuraciones
    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences -> preferences[NOTIFICATIONS_ENABLED] ?: true }

    val pokemonOfDayEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences -> preferences[POKEMON_OF_DAY_ENABLED] ?: true }

    val favoriteUpdatesEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences -> preferences[FAVORITE_UPDATES_ENABLED] ?: true }

    val appUpdatesEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences -> preferences[APP_UPDATES_ENABLED] ?: true }

    val fcmToken: Flow<String?> = context.notificationDataStore.data
        .map { preferences -> preferences[FCM_TOKEN] }

    // Métodos para actualizar configuraciones
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
        
        if (enabled) {
            subscribeToTopics()
        } else {
            unsubscribeFromAllTopics()
        }
    }

    suspend fun setPokemonOfDayEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[POKEMON_OF_DAY_ENABLED] = enabled
        }
        
        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic("pokemon_of_the_day").await()
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("pokemon_of_the_day").await()
        }
    }

    suspend fun setFavoriteUpdatesEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[FAVORITE_UPDATES_ENABLED] = enabled
        }
        
        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic("favorite_updates").await()
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("favorite_updates").await()
        }
    }

    suspend fun setAppUpdatesEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[APP_UPDATES_ENABLED] = enabled
        }
        
        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic("app_updates").await()
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("app_updates").await()
        }
    }

    // Obtener y guardar token FCM
    suspend fun initializeFirebaseMessaging() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            context.notificationDataStore.edit { preferences ->
                preferences[FCM_TOKEN] = token
            }
            Log.d(TAG, "Token FCM inicializado: $token")
            
            // Suscribirse a topics por defecto si las notificaciones están habilitadas
            val notificationsEnabled = context.notificationDataStore.data.map { 
                it[NOTIFICATIONS_ENABLED] ?: true 
            }
            
            subscribeToTopics()
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando Firebase Messaging", e)
        }
    }

    private suspend fun subscribeToTopics() {
        try {
            val messaging = FirebaseMessaging.getInstance()
            
            // Topic general
            messaging.subscribeToTopic("general").await()
            
            // Topics específicos basados en configuraciones
            val pokemonOfDayEnabled = context.notificationDataStore.data.map { 
                it[POKEMON_OF_DAY_ENABLED] ?: true 
            }
            val favoriteUpdatesEnabled = context.notificationDataStore.data.map { 
                it[FAVORITE_UPDATES_ENABLED] ?: true 
            }
            val appUpdatesEnabled = context.notificationDataStore.data.map { 
                it[APP_UPDATES_ENABLED] ?: true 
            }
            
            // Recopilar valores actuales y suscribirse
            pokemonOfDayEnabled.collect { enabled ->
                if (enabled) {
                    messaging.subscribeToTopic("pokemon_of_the_day").await()
                }
            }
            
            Log.d(TAG, "Suscrito a topics de Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "Error suscribiéndose a topics", e)
        }
    }

    private suspend fun unsubscribeFromAllTopics() {
        try {
            val messaging = FirebaseMessaging.getInstance()
            messaging.unsubscribeFromTopic("general").await()
            messaging.unsubscribeFromTopic("pokemon_of_the_day").await()
            messaging.unsubscribeFromTopic("favorite_updates").await()
            messaging.unsubscribeFromTopic("app_updates").await()
            
            Log.d(TAG, "Desuscrito de todos los topics")
        } catch (e: Exception) {
            Log.e(TAG, "Error desuscribiéndose de topics", e)
        }
    }

    // Método para enviar notificación cuando se agrega un favorito
    suspend fun sendFavoriteAddedNotification(pokemonName: String, pokemonId: Int) {
        try {
            // Verificar si las notificaciones de favoritos están habilitadas
            val favoriteNotificationsEnabled = context.notificationDataStore.data.map { 
                it[FAVORITE_UPDATES_ENABLED] ?: true 
            }
            
            val isEnabled = favoriteNotificationsEnabled.first() // Obtener el valor actual
            if (!isEnabled) {
                Log.d(TAG, "Notificaciones de favoritos deshabilitadas")
                return
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // Agregar extra para navegar directamente a la sección de favoritos
                putExtra("navigate_to", "favorites")
                putExtra("pokemon_id", pokemonId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                pokemonId, // Usar pokemonId como requestCode único
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = "⭐ Nuevo Pokémon Favorito"
            val body = "¡Has agregado a ${pokemonName.replaceFirstChar { it.titlecase() }} a tus favoritos!"

            val notificationBuilder = NotificationCompat.Builder(context, "pokemon_favorites_channel")
                .setSmallIcon(R.drawable.pokemon_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(context.getColor(R.color.pokemon_electric))
                .setGroup("pokemon_favorites_group")

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Crear canal para Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "pokemon_favorites_channel",
                    "Favoritos Pokémon",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando agregas un Pokémon a favoritos"
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Usar pokemonId como notification ID para que cada Pokémon tenga su propia notificación
            notificationManager.notify(1000 + pokemonId, notificationBuilder.build())
            Log.d(TAG, "Notificación de favorito enviada para $pokemonName (ID: $pokemonId)")
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando notificación de favorito para $pokemonName", e)
        }
    }

    // Método para probar notificaciones locales
    suspend fun sendTestNotification() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = "🧪 Notificación de Prueba"
            val body = "¡Las notificaciones de PokePI están funcionando correctamente!"

            val notificationBuilder = NotificationCompat.Builder(context, "pokemon_test_channel")
                .setSmallIcon(R.drawable.pokemon_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(context.getColor(R.color.pokemon_electric))

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Crear canal para Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "pokemon_test_channel",
                    "Notificaciones de Prueba",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Canal para probar notificaciones de PokePI"
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(9999, notificationBuilder.build())
            Log.d(TAG, "Notificación de prueba enviada")
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando notificación de prueba", e)
        }
    }
}