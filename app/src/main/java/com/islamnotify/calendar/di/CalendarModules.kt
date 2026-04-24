package com.islamnotify.calendar.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.calendar.data.CalendarDataStore
import com.islamnotify.calendar.data.CalendarRepositoryImpl
import com.islamnotify.calendar.domain.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CalendarModules {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class CalendarPrefs

    @Provides
    @Singleton
    @CalendarPrefs
    fun provideCalendarPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("calendar_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun provideCalendarDataStore(@CalendarPrefs dataStore: DataStore<Preferences>): CalendarDataStore{
        return CalendarDataStore(dataStore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarRepositoryModules {
    @Singleton
    @Binds
    abstract fun bindCalendarRepository(repository: CalendarRepositoryImpl): CalendarRepository
}