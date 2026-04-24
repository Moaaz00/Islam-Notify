package com.islamnotify.events.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.islamnotify.events.di.EventsModules
import com.islamnotify.events.domain.EventFlags
import com.islamnotify.events.domain.EventsPreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventsDataStore @Inject constructor(
    @param:EventsModules.EventsPrefs val dataStore: DataStore<Preferences>
) {

//    val eventsList = listOf(
//        EventsPreferenceKeys.MONDAY_FASTING,
//        EventsPreferenceKeys.THURSDAY_FASTING,
//        EventsPreferenceKeys.WHITE_DAYS_FASTING,
//        EventsPreferenceKeys.ARFA_FASTING,
//        EventsPreferenceKeys.TSUA_FASTING,
//        EventsPreferenceKeys.ASHORA_FASTING,
//        EventsPreferenceKeys.SHAWWAL_FASTING,
//        EventsPreferenceKeys.RAMADAN_EVENT,
//        EventsPreferenceKeys.RAMADAN_LAST_10_DAYS_EVENT,
//        EventsPreferenceKeys.DHU_AL_HIJJA_FIRST_10_DAYS_EVENT,
//        EventsPreferenceKeys.EID_AL_FITR_EVENT,
//        EventsPreferenceKeys.EID_AL_ADHA_EVENT,
//        EventsPreferenceKeys.FRIDAY_EVENT,
//        EventsPreferenceKeys.MUHARRAM_EVENT,
//        EventsPreferenceKeys.RAJAB_EVENT,
//        EventsPreferenceKeys.DHU_AL_QIDA_EVENT,
//        EventsPreferenceKeys.DHU_AL_HIJJA_EVENT
//    )

    suspend fun toggleEvent(key: Preferences.Key<Boolean>, enable: Boolean) {
        dataStore.edit { preferences ->
            preferences[key] = enable
        }
    }

    fun getEventsFlags(): Flow<EventFlags> {
        return dataStore.data.map { preferences ->
                EventFlags(
                    isAllEnabled = preferences[EventsPreferenceKeys.IS_ALL_ENABLED]?: true,
                    mondayFasting = preferences[EventsPreferenceKeys.MONDAY_FASTING] ?: true,
                    thursdayFasting = preferences[EventsPreferenceKeys.THURSDAY_FASTING] ?: true,
                    whiteDaysFasting = preferences[EventsPreferenceKeys.WHITE_DAYS_FASTING] ?: true,
                    arafaFasting = preferences[EventsPreferenceKeys.ARFA_FASTING] ?: true,
                    tasuaFasting = preferences[EventsPreferenceKeys.TSUA_FASTING] ?: true,
                    ashoraFasting = preferences[EventsPreferenceKeys.ASHORA_FASTING] ?: true,
                    shawwalFasting = preferences[EventsPreferenceKeys.SHAWWAL_FASTING] ?: true,
                    ramadanEvent = preferences[EventsPreferenceKeys.RAMADAN_EVENT] ?: true,
                    ramdanLast10DaysEvent = preferences[EventsPreferenceKeys.RAMADAN_LAST_10_DAYS_EVENT] ?: true,
                    dhuAlHijjahFirst10DaysEvent = preferences[EventsPreferenceKeys.DHU_AL_HIJJA_FIRST_10_DAYS_EVENT] ?: true,
                    fridayEvent = preferences[EventsPreferenceKeys.FRIDAY_EVENT] ?: true,
                    eidAlFitrEvent = preferences[EventsPreferenceKeys.EID_AL_FITR_EVENT] ?: true,
                    eidAlAdhaEvent = preferences[EventsPreferenceKeys.EID_AL_ADHA_EVENT] ?: true,
                    muharramEvent = preferences[EventsPreferenceKeys.MUHARRAM_EVENT] ?: true,
                    rajabEvent = preferences[EventsPreferenceKeys.RAJAB_EVENT] ?: true,
                    dhuAlQidaEvent = preferences[EventsPreferenceKeys.DHU_AL_QIDA_EVENT] ?: true,
                    dhuAlHijjahEvent = preferences[EventsPreferenceKeys.DHU_AL_HIJJA_EVENT] ?: true
                )
            }
    }


//    suspend fun toggleAll(enable: Boolean) {
//        dataStore.edit { preferences ->
//            preferences
//        }
//    }


}