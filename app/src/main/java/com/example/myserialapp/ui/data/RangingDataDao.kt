package com.example.myserialapp.ui.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
/*
@Dao
interface RangingDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RangingData)
    @Query("SELECT * from RangingData")
    fun getAllEnvironment(): Flow<List<RangingData>>
}

 */