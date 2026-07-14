package com.islamnotify.sounds.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.sounds.data.SoundsDataStore
import com.islamnotify.sounds.data.SoundsWorkImpl
import com.islamnotify.sounds.domain.SoundsWork
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
class SoundsModules {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class SoundPrefs

    @Provides
    @Singleton
    @SoundPrefs
    fun provideSoundsPrefs(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("sounds_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun provideSoundsDataStore(
        @SoundPrefs dataStore: DataStore<Preferences>,
        crashReporter: CrashReporter
    ): SoundsDataStore{
        return SoundsDataStore(dataStore, crashReporter)
    }

}


@Module
@InstallIn(SingletonComponent::class)
abstract class SoundsRepositoriesModules {
    @Singleton
    @Binds
    abstract fun bindSoundsWork(implementation: SoundsWorkImpl): SoundsWork
}