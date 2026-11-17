package com.example.temupikmin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationPointDao {
    @Insert
    suspend fun insert(point: LocationPoint): Long

    @Query("SELECT * FROM LocationPoint ORDER BY timestamp ASC")
    suspend fun getAllPoints(): List<LocationPoint>

    @Query("SELECT * FROM LocationPoint WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getPointsSince(since: Long): List<LocationPoint>
}