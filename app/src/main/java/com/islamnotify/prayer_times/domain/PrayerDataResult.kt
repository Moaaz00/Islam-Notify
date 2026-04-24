package com.islamnotify.prayer_times.domain

import com.islamnotify.prayer_times.domain.model.PrayerTimes

sealed interface PrayerDataResult {
    data class Success(val prayerTimes: PrayerTimes) : PrayerDataResult
    object Error : PrayerDataResult
}