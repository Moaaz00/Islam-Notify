package com.islamnotify.common.domain

/**
 * Static bridge to the singleton [CrashReporter] for code that cannot use constructor
 * injection — Kotlin `object` utilities and Room `@TypeConverter` classes that Room
 * instantiates itself. [instance] is assigned once from `Application.onCreate()`.
 *
 * Injectable classes (repositories, use-cases, workers, ViewModels, @AndroidEntryPoint
 * receivers/services) should inject [CrashReporter] directly instead of using this.
 */
object CrashReporterProvider {
    @Volatile
    var instance: CrashReporter? = null
}
