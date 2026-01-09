package com.jksalcedo.crontact.di

import android.content.Context
import androidx.room.Room
import com.jksalcedo.crontact.data.local.AppDatabase
import com.jksalcedo.crontact.data.local.CheckInDao
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
            "tend_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCheckInDao(database: AppDatabase): CheckInDao {
        return database.checkInDao()
    }
}
