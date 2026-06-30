package com.islamnotify.sounds.domain

import com.islamnotify.R

data class SoundOption(val rawResId: Int, val nameResId: Int)

val AZAN_SOUNDS = listOf(
    SoundOption(R.raw.azhan_nasser_alqatamy, R.string.sound_azhan_nasser_alqatamy),
    SoundOption(R.raw.full_azhan_nasser_alqatamy, R.string.sound_full_azhan_nasser_alqatamy),
    SoundOption(R.raw.azhan_alminshawy, R.string.sound_azhan_alminshawy),
    SoundOption(R.raw.full_azhan_alminshawy, R.string.sound_full_azhan_alminshawy),
    SoundOption(R.raw.azhan_alhusary, R.string.sound_azhan_alhusary),
    SoundOption(R.raw.full_azhan_alhusary, R.string.sound_full_azhan_alhusary),
    SoundOption(R.raw.azhan_adham_alsharqawy, R.string.sound_azhan_adham_alsharqawy),
    SoundOption(R.raw.full_azhan_adham_alsharqawy, R.string.sound_full_azhan_adham_alsharqawy),
    SoundOption(R.raw.azhan_mashary_alafasy, R.string.sound_azhan_mashary_alafasy),
    SoundOption(R.raw.full_azhan_mashary_alafasy, R.string.sound_full_azhan_mashary_alafasy),
)

val IQAMA_SOUNDS = listOf(
    SoundOption(R.raw.iqama_nasser_alqatamy, R.string.sound_iqama_nasser_alqatamy),
    SoundOption(R.raw.iqama_alhusary, R.string.sound_iqama_alhusary),
    SoundOption(R.raw.iqama_mashary_alafasy, R.string.sound_iqama_mashary_alafasy),
    SoundOption(R.raw.iqama_hashem_alsaqqaf, R.string.sound_iqama_hashem_alsaqqaf),
    SoundOption(R.raw.iqama_yasser_aldousary, R.string.sound_iqama_yasser_aldousary),
)

val NOTIFY_SOUNDS = listOf(
    SoundOption(R.raw.notify_sound, R.string.sound_notify_1),
    SoundOption(R.raw.notify_sound1, R.string.sound_notify_2),
    SoundOption(R.raw.notify_sound2, R.string.sound_notify_3),
    SoundOption(R.raw.notify_sound3, R.string.sound_notify_4),
)
