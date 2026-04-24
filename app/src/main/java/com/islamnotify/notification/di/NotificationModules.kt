package com.islamnotify.notification.di

import android.app.AlarmManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.calendar.domain.CalendarRepository
import com.islamnotify.notification.data.NotificationDataStore
import com.islamnotify.notification.data.NotificationWorkHandler
import com.islamnotify.notification.data.NotificationWorkImpl
import com.islamnotify.notification.domain.NotificationWork
import com.islamnotify.prayer_times.domain.PrayerDataUseCase
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
object NotificationModules {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class NotificationPrefs

    @Provides
    @Singleton
    @NotificationPrefs
    fun provideNotificationPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("notification_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun provideNotificationDataStore(@NotificationPrefs dataStore: DataStore<Preferences>): NotificationDataStore {
        return NotificationDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideNotificationWorkHandler(@ApplicationContext context: Context, prayerDataUseCase: PrayerDataUseCase, alarmManager: AlarmManager, calendarRepository: CalendarRepository): NotificationWorkHandler {
        return NotificationWorkHandler(context, prayerDataUseCase, alarmManager, calendarRepository)
    }


    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModules {
    @Singleton
    @Binds
    abstract fun bindNotificationWork(implementation: NotificationWorkImpl): NotificationWork
}

