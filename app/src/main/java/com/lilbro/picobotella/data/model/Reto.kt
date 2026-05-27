package com.lilbro.picobotella.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a challenge (reto) stored in the local database.
 *
 * @property id Auto-generated primary key.
 * @property descripcion Text content of the challenge.
 * @property createdAt Timestamp in milliseconds when the challenge was created.
 */
@Entity(tableName = "retos")
data class Reto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String,
    val createdAt: Long = System.currentTimeMillis()
)