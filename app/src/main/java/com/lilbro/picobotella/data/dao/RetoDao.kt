package com.lilbro.picobotella.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [Reto] entities.
 */
@Dao
interface RetoDao {

    /**
     * Returns all challenges ordered by creation date descending (newest first).
     */
    @Query("SELECT * FROM retos ORDER BY createdAt DESC")
    fun getAllRetos(): Flow<List<Reto>>

    /**
     * Inserts a new challenge, replacing on conflict.
     *
     * @param reto The challenge to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reto: Reto)

    /**
     * Updates an existing challenge.
     *
     * @param reto The challenge with updated values.
     */
    @Update
    suspend fun update(reto: Reto)

    /**
     * Deletes a challenge from the database.
     *
     * @param reto The challenge to delete.
     */
    @Delete
    suspend fun delete(reto: Reto)
}