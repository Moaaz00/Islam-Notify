package com.islamnotify.events.domain

import androidx.datastore.preferences.core.booleanPreferencesKey

object EventsPreferenceKeys {
    val IS_ALL_ENABLED = booleanPreferencesKey("ENABLE_ALL")
    val MONDAY_FASTING = booleanPreferencesKey("MONDAY_FASTING")
    val THURSDAY_FASTING = booleanPreferencesKey("THURSDAY_FASTING")
    val WHITE_DAYS_FASTING = booleanPreferencesKey("WHITE_DAYS_FASTING")
    val ARFA_FASTING = booleanPreferencesKey("ARFA_FASTING")
    val TSUA_FASTING = booleanPreferencesKey("TASUA_FASTING")
    val ASHORA_FASTING = booleanPreferencesKey("ASHORA_FASTING")
    val SHAWWAL_FASTING = booleanPreferencesKey("SHAWWAL_FASTING")

    val RAMADAN_EVENT = booleanPreferencesKey("RAMADAN_EVENT")
    val RAMADAN_LAST_10_DAYS_EVENT = booleanPreferencesKey("RAMADAN_LAST_10_DAYS_EVENT")
    val DHU_AL_HIJJA_FIRST_10_DAYS_EVENT = booleanPreferencesKey("DHU_AL_HIJJA_FIRST_10_DAYS_EVENT")

    val EID_AL_FITR_EVENT = booleanPreferencesKey("EID_AL_FITR_EVENT")
    val EID_AL_ADHA_EVENT = booleanPreferencesKey("EID_AL_ADHA_EVENT")
    val FRIDAY_EVENT = booleanPreferencesKey("FRIDAY_EVENT")
    val MUHARRAM_EVENT = booleanPreferencesKey("MUHARRAM_EVENT")
    val RAJAB_EVENT = booleanPreferencesKey("RAJAB_EVENT")
    val DHU_AL_QIDA_EVENT = booleanPreferencesKey("DHU_AL_QIDA_EVENT")
    val DHU_AL_HIJJA_EVENT = booleanPreferencesKey("DHU_AL_HIJJA_EVENT")
}