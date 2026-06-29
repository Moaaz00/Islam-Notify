package com.islamnotify.alarms

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmRepositoryModules {
    @Singleton
    @Binds
    abstract fun bindAlarmRepository(implementation: PrayerAlarmsScheduler): AlarmsRepository
}

