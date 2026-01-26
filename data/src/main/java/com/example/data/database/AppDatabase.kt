package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.dao.TripHistoryDao
import com.example.data.entity.TripHistoryEntity


@Database(entities = [TripHistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripHistoryDao(): TripHistoryDao
}