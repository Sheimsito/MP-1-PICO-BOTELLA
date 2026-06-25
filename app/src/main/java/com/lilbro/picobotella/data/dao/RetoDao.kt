package com.lilbro.picobotella.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.flow.Flow

@Dao
interface RetoDao {
    @Query("SELECT * FROM retos ORDER BY createdAt DESC")
    fun getAllRetos(): Flow<List<Reto>>

    @Query("SELECT * FROM retos ORDER BY RANDOM() LIMIT 1")
    suspend fun getRetoAleatorio(): Reto?

    @Query("SELECT * FROM retos WHERE id = :id LIMIT 1")
    suspend fun getRetoById(id: Int): Reto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reto: Reto): Long

    @Update
    suspend fun update(reto: Reto)

    @Delete
    suspend fun delete(reto: Reto)
}
