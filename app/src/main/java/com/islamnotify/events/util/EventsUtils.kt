package com.islamnotify.events.util

import com.islamnotify.events.domain.EventFlags

object EventsUtils {

    const val MONDAY_REQUEST_CODE_NOTIFICATION_ID = 9000
    const val THURSDAY_REQUEST_CODE_NOTIFICATION_ID = 9001
    const val WHITE_DAYS_REQUEST_CODE_NOTIFICATION_ID = 9002
    const val ARAFAH_REQUEST_CODE_NOTIFICATION_ID = 9003
    const val TASUA_REQUEST_CODE_NOTIFICATION_ID = 9004
    const val ASHORA_REQUEST_CODE_NOTIFICATION_ID = 9005
    const val SHAWWAL_REQUEST_CODE_NOTIFICATION_ID = 9006

    const val RAMADAN_REQUEST_CODE_NOTIFICATION_ID = 9007
    const val RAMADAN_LAST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID = 9008

    const val DHU_AL_HIJJA_FIRST_10_DAYS_REQUEST_CODE_NOTIFICATION_ID = 9009

    const val EID_AL_FITR_REQUEST_CODE_NOTIFICATION_ID = 9011
    const val EID_AL_ADHA_REQUEST_CODE_NOTIFICATION_ID = 9012

    const val FRIDAY_REQUEST_CODE_NOTIFICATION_ID = 9013

    const val DHU_AL_QIDA_REQUEST_CODE_NOTIFICATION_ID = 9014
    const val DHU_AL_HIJJA_REQUEST_CODE_NOTIFICATION_ID = 9010
    const val RAJAB_REQUEST_CODE_NOTIFICATION_ID = 9015
    const val MUHARRAM_REQUEST_CODE_NOTIFICATION_ID = 9016

    const val REQUEST_EVENTS_WORKER_ACTION = "REQUEST_EVENTS_WORKER_ACTION"
    const val REQUEST_EVENTS_WORKER_REQUEST_CODE = 9017

    const val EVENTS_ACTION = "EVENTS_ACTION"
    const val INTENT_TITLE_EXTRA = "intent_title_extra"
    const val INTENT_SUBTITLE_EXTRA = "intent_subtitle_extra"
    const val INTENT_NOTIFICATION_ID_EXTRA = "intent_notification_id_extra"
    const val MIDNIGHT_WORK_REQUEST_TAG = "EVENTS_MIDNIGHT_WORK_REQUEST_TAG"
    const val ONE_TIME_WORK_REQUEST_TAG = "ONE_TIME_MIDNIGHT_WORK_REQUEST_TAG"


    fun areAnySubEventsEnabled(flags: EventFlags): Boolean {
        return flags.mondayFasting || flags.thursdayFasting || flags.whiteDaysFasting ||
                flags.arafaFasting || flags.ashoraFasting || flags.tasuaFasting ||
                flags.shawwalFasting || flags.ramadanEvent || flags.ramdanLast10DaysEvent ||
                flags.dhuAlQidaEvent || flags.dhuAlHijjahEvent || flags.dhuAlHijjahFirst10DaysEvent ||
                flags.fridayEvent || flags.eidAlAdhaEvent || flags.eidAlFitrEvent ||
                flags.muharramEvent || flags.rajabEvent
    }

}