package com.example.temupikmin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
)