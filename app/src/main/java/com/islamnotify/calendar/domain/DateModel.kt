package com.islamnotify.calendar.domain

data class DateModel(
    var formatedDayOfMonth: String,
    var dayOfWeek: String,
    var formatedMonthNumber: String,
    var monthName: String,
    var formatedYear: String,
    var dayOfMonth: Int,
    var monthNumber: Int,
    var year: Int
)
