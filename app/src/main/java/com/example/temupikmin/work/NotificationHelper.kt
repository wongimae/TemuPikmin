package com.example.temupikmin.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.temupikmin.MainActivity
import com.example.temupikmin.R

const val CHANNEL_ID = "mood_checkin_channel"
const val NOTIFICATION_ID_BASE = 1000
const val EXTRA_ENTRY_ID = "entry_id"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Mood check-ins",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Prompts asking how you're feeling"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

fun showMoodNotification(context: Context, entryId: Long) {
    createNotificationChannel(context)

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_ENTRY_ID, entryId)
    }
    val pendingIntent = PendingIntent.getActivity(
        context, entryId.toInt(), openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("How are you feeling?")
        .setContentText("Tap to log your mood")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(NOTIFICATION_ID_BASE + entryId.toInt(), notification)
}