package com.islamnotify.events.di

import android.app.AlarmManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.events.data.EventsDataStore
import com.islamnotify.events.data.EventsWorkImpl
import com.islamnotify.events.domain.EventsWork
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
class EventsModules {

//    @Provides
//    @Singleton
//    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
//        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class EventsPrefs

    @Provides
    @Singleton
    @EventsPrefs
    fun provideEventsPrefs(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("events_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun provideEventsDataStore(@EventsPrefs dataStore: DataStore<Preferences>): EventsDataStore{
        return EventsDataStore(dataStore)
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class EventsRepositoriesModules {
    @Singleton
    @Binds
    abstract fun bindEventsWork(implementation: EventsWorkImpl): EventsWork

}