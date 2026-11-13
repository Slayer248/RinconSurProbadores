package com.rinconsur.probadores.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rinconsur.probadores.R

object NotificationHelper {

    const val CHANNEL_ID = "probadores_channel"
    const val CHANNEL_NAME = "Notificaciones de Probadores"
    const val CHANNEL_DESC = "Alertas de turnos próximos y probadores liberados"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    /**
     * API Nativa de Vibración (Feedback Háptico)
     */
    fun vibrar(context: Context, duracionMs: Long = 500L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(duracionMs)
            }
        } catch (_: Exception) {
            // Ignorar si el dispositivo no posee motor de vibración
        }
    }

    /**
     * API Nativa de Notificaciones del Sistema
     */
    fun enviarNotificacion(
        context: Context,
        titulo: String,
        mensaje: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        crearCanalNotificaciones(context)

        // Verificar permiso POST_NOTIFICATIONS para Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }

        // Emitir vibración háptica asociada a la notificación
        vibrar(context, 400L)
    }
}
