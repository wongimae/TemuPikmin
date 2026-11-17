package com.example.temupikmin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MoodEntryDao {
    @Insert
    suspend fun insert(entry: MoodEntry): Long

    @Update
    suspend fun update(entry: MoodEntry)

    @Query("SELECT * FROM MoodEntry ORDER BY timestamp DESC")
    suspend fun getAll(): List<MoodEntry>

    @Query("SELECT * FROM MoodEntry WHERE id = :id")
    suspend fun getById(id: Long): MoodEntry?
}