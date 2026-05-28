package com.lilbro.picobotella.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.model.Reto

/**
 * Main Room database for the application.
 *
 * Uses a singleton pattern to prevent multiple instances.
 */
@Database(entities = [Reto::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /** Provides access to [Reto] data operations. */
    abstract fun retoDao(): RetoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance, creating it if necessary.
         *
         * @param context Application context used to build the database.
         * @return The singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "picobotella_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}