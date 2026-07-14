package com.islamnotify.common.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.islamnotify.common.domain.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crashlytics-backed [CrashReporter]. [FirebaseCrashlytics.getInstance] is itself a
 * singleton, so this impl holds no state and is safe as an app-wide [Singleton].
 */
@Singleton
class CrashlyticsReporter @Inject constructor() : CrashReporter {

    private val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics.getInstance()

    override fun recordNonFatal(t: Throwable, vararg keys: Pair<String, Any?>) {
        keys.forEach { (key, value) -> setKey(key, value) }
        crashlytics.recordException(t)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setKey(key: String, value: Any?) {
        when (value) {
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            is String -> crashlytics.setCustomKey(key, value)
            null -> crashlytics.setCustomKey(key, "null")
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }
}
