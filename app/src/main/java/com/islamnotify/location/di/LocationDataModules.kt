package com.islamnotify.location.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.islamnotify.location.data.LocationDataStore
import com.islamnotify.location.data.LocationRepositoryImpl
import com.islamnotify.location.domain.LocationRepository
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
object LocationDataModules {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocationPrefs

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }


    @Provides
    @Singleton
    @LocationPrefs
    fun provideLocationPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("location_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun provideLocationDataStore(@LocationPrefs dataStore: DataStore<Preferences>): LocationDataStore {
        return LocationDataStore(dataStore)
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationGateWayModules {

    @Binds
    @Singleton
    abstract fun provideLocationClient(locationClient: LocationRepositoryImpl): LocationRepository

}