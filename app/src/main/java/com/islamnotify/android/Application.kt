package com.islamnotify.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.islamnotify.common.NotificationChannelInitializer
import com.islamnotify.common.domain.CrashReporter
import com.islamnotify.common.domain.CrashReporterProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), Configuration.Provider{
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    lateinit var channelInitializer: NotificationChannelInitializer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Bridge the injected reporter to static/Room-instantiated code (see CrashReporterProvider).
        CrashReporterProvider.instance = crashReporter
        // Create channels here (not just in the UI) so background workers that post foreground
        // notifications don't crash with "Bad notification for startForeground" on a fresh install.
        channelInitializer.createNotificationChannels()
    }
}