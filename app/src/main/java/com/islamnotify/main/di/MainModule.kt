package com.islamnotify.main.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.main.data.MainDataStore
import com.islamnotify.main.domain.MainPreferencesRepository
import com.islamnotify.notification.data.NotificationWorkImpl
import com.islamnotify.notification.domain.NotificationWork
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
object MainModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MainPrefs

    @Provides
    @Singleton
    @MainPrefs
    fun provideMainPreferences(@ApplicationContext context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.dataStoreFile("main_cache.preferences_pb")
    }
}


@Module
@InstallIn(SingletonComponent::class)
abstract class MainRepositoryModules {
    @Singleton
    @Binds
    abstract fun bindMainRepository(implementation: MainDataStore): MainPreferencesRepository
}
