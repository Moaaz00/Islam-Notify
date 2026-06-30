package com.islamnotify.intro.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.intro.data.IntroDataStore
import com.islamnotify.intro.domain.IntroPreferencesRepository
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
object IntroModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class IntroPrefs

    @Provides
    @Singleton
    @IntroPrefs
    fun provideIntroPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("intro_cache.preferences_pb")
        }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class IntroRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindIntroRepository(implementation: IntroDataStore): IntroPreferencesRepository
}
