package com.islamnotify.events.domain

data class EventFlags(
    var isAllEnabled: Boolean = true,
    var mondayFasting: Boolean = true,
    var thursdayFasting: Boolean = true,
    var whiteDaysFasting: Boolean = true,
    var arafaFasting: Boolean = true,
    var tasuaFasting: Boolean = true,
    var ashoraFasting: Boolean = true,
    var shawwalFasting: Boolean = true,
    var ramadanEvent: Boolean = true,
    var ramdanLast10DaysEvent: Boolean = true,
    var dhuAlHijjahFirst10DaysEvent: Boolean = true,
    var fridayEvent: Boolean = true,
    var eidAlFitrEvent: Boolean = true,
    var eidAlAdhaEvent: Boolean = true,
    var muharramEvent: Boolean = true,
    var rajabEvent: Boolean = true,
    var dhuAlQidaEvent: Boolean = true,
    var dhuAlHijjahEvent: Boolean = true,
    )
