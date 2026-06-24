package com.lilbro.picobotella.di

import android.content.Context
import androidx.room.Room
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "picobotella_db"
        ).build()
    }

    @Provides
    fun provideRetoDao(appDatabase: AppDatabase): RetoDao {
        return appDatabase.retoDao()
    }
}
