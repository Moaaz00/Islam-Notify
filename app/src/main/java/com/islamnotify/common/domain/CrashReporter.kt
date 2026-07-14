package com.islamnotify.common.domain

/**
 * Central seam for reporting non-fatal errors and breadcrumbs to the crash backend
 * (Crashlytics). Keeps the Firebase SDK out of feature code.
 *
 * - [recordNonFatal] reports a caught throwable that represents unexpected behaviour
 *   (a swallowed exception, a silent fallback). It shows up in Crashlytics as a
 *   non-fatal issue, separate from hard crashes.
 * - [log] adds a breadcrumb to the current session; it is attached to whatever crash
 *   or non-fatal is reported next. Use it for expected-but-notable conditions.
 */
interface CrashReporter {
    fun recordNonFatal(t: Throwable, vararg keys: Pair<String, Any?>)
    fun log(message: String)
    fun setKey(key: String, value: Any?)
}
