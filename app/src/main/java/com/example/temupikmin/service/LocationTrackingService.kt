package com.example.temupikmin.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.temupikmin.R
import com.example.temupikmin.data.AppDatabase
import com.example.temupikmin.data.LocationPoint
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 2000
        const val ACTION_START = "START_TRACKING"
        const val ACTION_STOP = "STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                serviceScope.launch {
                    val db = AppDatabase.getInstance(applicationContext)
                    db.locationPointDao().insert(
                        LocationPoint(
                            timestamp = System.currentTimeMillis(),
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                return START_NOT_STICKY
            }
            else -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        createNotificationChannel()
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 15_000L // every 15 seconds
        ).setMinUpdateIntervalMillis(10_000L).build()

        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopTracking() {
        fusedClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking your path")
            .setContentText("GPS tracking is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Location tracking", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
    }
}