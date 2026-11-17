package com.example.temupikmin.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.example.temupikmin.data.AppDatabase
import com.example.temupikmin.data.MoodEntry
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CheckInWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val location = getLastKnownLocation()
        val db = AppDatabase.getInstance(applicationContext)

        val entryId = db.moodEntryDao().insert(
            MoodEntry(
                timestamp = System.currentTimeMillis(),
                latitude = location?.latitude,
                longitude = location?.longitude,
                mood = null
            )
        )

        showMoodNotification(applicationContext, entryId)

        // Schedule the next fixed-time check-in
        com.example.temupikmin.work.scheduleNextCheckIn(applicationContext)

        return Result.success()
    }

    private suspend fun getLastKnownLocation() = suspendCancellableCoroutine<android.location.Location?> { cont ->
        val hasPermission = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val client = LocationServices.getFusedLocationProviderClient(applicationContext)
        client.lastLocation
            .addOnSuccessListener { location -> cont.resume(location) }
            .addOnFailureListener { cont.resume(null) }
    }
}