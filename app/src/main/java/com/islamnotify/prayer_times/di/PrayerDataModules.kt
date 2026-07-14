package com.islamnotify.prayer_times.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.location.domain.LocationRepository
import com.islamnotify.prayer_times.data.PrayerDataLocal
import com.islamnotify.prayer_times.data.PrayerDataRepositoryImpl
import com.islamnotify.prayer_times.data.PrayerDataStore
import com.islamnotify.prayer_times.domain.PrayerDataRepository
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
object PrayerDataModules {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class PrayerPrefs

    @Provides
    @Singleton
    fun providePrayerLocal(crashReporter: CrashReporter): PrayerDataLocal {
        return PrayerDataLocal(crashReporter)
    }

    @Provides
    @Singleton
    @PrayerPrefs
    fun providePrayerPreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.dataStoreFile("prayer_cache.preferences_pb")
        }

    @Provides
    @Singleton
    fun providePrayerDataStore(
        @PrayerPrefs dataStore: DataStore<Preferences>,
        crashReporter: CrashReporter
    ): PrayerDataStore {
        return PrayerDataStore(dataStore, crashReporter)
    }

    @Provides
    @Singleton
    fun providePrayerDataUseCase(
        prayerDataRepository: PrayerDataRepository,
        locationRepository: LocationRepository,
        crashReporter: CrashReporter
    ): PrayerDataUseCase {
        return PrayerDataUseCase(
            prayerDataRepository = prayerDataRepository,
            locationRepository = locationRepository,
            crashReporter = crashReporter
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PrayerDataGateWayModules {
    @Singleton
    @Binds
    abstract fun bindPrayerDataGateWay(repository: PrayerDataRepositoryImpl): PrayerDataRepository
}




//    @Qualifier
//    @Retention(AnnotationRetention.RUNTIME)
//    annotation class PrayerRetrofit

//    @Provides
//    @Singleton
//    @PrayerRetrofit
//    fun provideRetrofitInstance(): Retrofit {
//        val okHttpClient = OkHttpClient.Builder()
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://api.aladhan.com/")
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//
//    @Provides
//    @Singleton
//    fun providePrayerApi(@PrayerRetrofit retrofit: Retrofit): PrayersDataApi {
//        return retrofit.create(PrayersDataApi::class.java)
//    }
