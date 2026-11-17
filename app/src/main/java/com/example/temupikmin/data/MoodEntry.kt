package com.example.temupikmin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val latitude: Double?,   // where the user was when prompted
    val longitude: Double?,
    val mood: String?        // null until the user actually responds
)