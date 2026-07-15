package com.fazli.momentum.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fazli.momentum.MainActivity
import com.fazli.momentum.R

object NotificationHelper {
    const val CHANNEL_DAILY = "daily_reminder"
    const val CHANNEL_WEEKLY = "weekly_review"
    const val CHANNEL_WARNING = "missed_wajib_warning"

    const val NOTIFICATION_ID_DAILY = 1
    const val NOTIFICATION_ID_WEEKLY = 2
    const val NOTIFICATION_ID_WARNING = 3

    fun ensureChannels(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_DAILY, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Reminder Harian")
                .setDescription("Pengingat cek rencana harian")
                .build()
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_WEEKLY, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Weekly Review")
                .setDescription("Pengingat review mingguan")
                .build()
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_WARNING, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Peringatan WAJIB Bolong")
                .setDescription("Peringatan kalau 2 hari beruntun WAJIB belum kecentang")
                .build()
        )
    }

    fun notify(context: Context, channelId: String, notificationId: Int, title: String, text: String) {
        val hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
