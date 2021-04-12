package com.bluegecko.sieste

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationUtils {
    companion object {
        private const val CHANNEL_ID = "DeviceNapChannel"
        fun createNotificationChannel(context: Context = App.instance) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setSound(null, null)
                    setShowBadge(false)
                    description = context.getString(R.string.channel_description)
                    setBypassDnd(true)
                }
                // Register the channel with the system
                val notificationManager =
                    ContextCompat.getSystemService(context, NotificationManager::class.java)
                notificationManager?.deleteNotificationChannel(CHANNEL_ID)
                notificationManager?.createNotificationChannel(channel)
            }
        }

        // design personnalisé de la notification
        private val remoteViews =
            RemoteViews(App.instance.packageName, R.layout.notification).apply {
                setOnClickPendingIntent(
                    R.id.stop,
                    PendingIntent.getBroadcast(
                        App.instance,
                        System.currentTimeMillis().toInt(),
                        Intent(App.instance, Stop::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }

        // PendingIntent pour pouvoir lancer 'MainActivity' à partir de la notification
        private val mainActivityIntent = PendingIntent.getActivity(
            App.instance,
            System.currentTimeMillis().toInt(),
            Intent(App.instance, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // créer la notification (design, caractéristiques, ...)
        private val notification =
            NotificationCompat.Builder(App.instance, CHANNEL_ID).apply {
                setCustomContentView(remoteViews)                   // design personnalisé
                setSmallIcon(R.drawable.notification_icon)          // icone
                setOngoing(true)                                    // notif non annulable
                setContentIntent(mainActivityIntent)                // au clic on lance 'MainActivity'
                priority = NotificationCompat.PRIORITY_MAX          // afficher tout en haut
            }.build()

        // notifier
        fun notify(time: Long) {
            remoteViews.setTextViewText(R.id.time, time.millisToHMMSS())
            NotificationManagerCompat.from(App.instance).notify(0, notification)    // notifier
        }

        // annuler la notification
        fun cancelNotification() {
            NotificationManagerCompat.from(App.instance).cancel(0)
        }
    }

    /**
     * action du bouton 'STOP' de la notification
     */
    class Stop : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            TimeService.instance?.cancel()                                          // interromptre le décompte
            App.instance.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))  // masquer le volet notifs
        }
    }
}