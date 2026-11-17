package com.example.temupikmin.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

val CHECKIN_HOURS = listOf(8, 10, 12, 14, 16, 18, 20)

fun scheduleNextCheckIn(context: Context) {
    val delay = millisUntilNextSlot()

    val request = OneTimeWorkRequestBuilder<CheckInWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "mood_checkin_work",
        ExistingWorkPolicy.REPLACE,
        request
    )
}

private fun millisUntilNextSlot(): Long {
    val now = Calendar.getInstance()
    val next = now.clone() as Calendar

    // Find the next hour in CHECKIN_HOURS that's still ahead of now today
    val upcomingToday = CHECKIN_HOURS.firstOrNull { hour ->
        next.set(Calendar.HOUR_OF_DAY, hour)
        next.set(Calendar.MINUTE, 0)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        next.timeInMillis > now.timeInMillis
    }

    if (upcomingToday == null) {
        // No more slots today — jump to tomorrow's first slot (8am)
        next.add(Calendar.DAY_OF_YEAR, 1)
        next.set(Calendar.HOUR_OF_DAY, CHECKIN_HOURS.first())
        next.set(Calendar.MINUTE, 0)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
    } else {
        next.set(Calendar.HOUR_OF_DAY, upcomingToday)
    }

    return next.timeInMillis - now.timeInMillis
}