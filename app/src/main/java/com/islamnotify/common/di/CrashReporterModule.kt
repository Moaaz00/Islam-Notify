package com.islamnotify.common.di

import com.islamnotify.common.data.CrashlyticsReporter
import com.islamnotify.common.domain.CrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReporterModule {
    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: CrashlyticsReporter): CrashReporter
}
