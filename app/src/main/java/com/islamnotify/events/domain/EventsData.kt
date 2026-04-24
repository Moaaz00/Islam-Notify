package com.islamnotify.events.domain

data class EventsData(
    var title: String,
    var subtitle: String,
    var requestCode: Int,
    var isToday: Boolean,
    var triggerTime: Long,
    var isEnabled: Boolean
)
