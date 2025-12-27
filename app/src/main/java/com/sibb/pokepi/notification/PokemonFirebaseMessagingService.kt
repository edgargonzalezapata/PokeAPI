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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sibb.pokepi.MainActivity
import com.sibb.pokepi.R

class PokemonFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PokemonFCM"
        private const val CHANNEL_ID = "pokemon_notifications"
        private const val CHANNEL_NAME = "Notificaciones Pokémon"
        private const val CHANNEL_DESCRIPTION = "Notificaciones sobre Pokémon del día, favoritos y actualizaciones"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Manejar datos del mensaje
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }

        // Manejar notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Cuerpo del mensaje: ${it.body}")
            sendNotification(
                title = it.title ?: "PokePI",
                body = it.body ?: "Nueva notificación",
                data = remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Token FCM actualizado: $token")
        // Aquí puedes enviar el token a tu servidor si lo necesitas
        sendRegistrationToServer(token)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        val pokemonId = data["pokemon_id"]
        val title = data["title"] ?: "PokePI"
        val body = data["body"] ?: "Nueva notificación"

        when (type) {
            "pokemon_of_the_day" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    notificationId = NotificationType.POKEMON_OF_THE_DAY.id
                )
            }
            "favorite_update" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    notificationId = NotificationType.FAVORITE_UPDATE.id
                )
            }
            "app_update" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    notificationId = NotificationType.APP_UPDATE.id
                )
            }
            else -> {
                sendNotification(title, body, data)
            }
        }
    }

    private fun sendNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Agregar datos extras si es necesario
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.pokemon_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Personalizar según el tipo de notificación
        val type = data["type"]
        when (type) {
            "pokemon_of_the_day" -> {
                notificationBuilder.setColor(getColor(R.color.pokemon_fire))
            }
            "favorite_update" -> {
                notificationBuilder.setColor(getColor(R.color.pokemon_heart))
            }
            "app_update" -> {
                notificationBuilder.setColor(getColor(R.color.pokemon_electric))
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implementar envío del token al servidor si es necesario
        Log.d(TAG, "Token enviado al servidor: $token")
    }

    enum class NotificationType(val id: Int) {
        POKEMON_OF_THE_DAY(1001),
        FAVORITE_UPDATE(1002),
        APP_UPDATE(1003),
        GENERAL(1000)
    }
}