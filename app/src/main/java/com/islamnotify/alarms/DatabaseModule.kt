package com.islamnotify.alarms

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This module lives as long as the application does
object DatabaseModule {

    @Provides
    @Singleton // Guarantees Hilt only creates one database instance
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prayer_alarms_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun providePrayerAlarmDao(appDatabase: AppDatabase): PrayerAlarmDao {
        return appDatabase.prayerAlarmDao()
    }
}