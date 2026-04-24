package com.islamnotify.sounds.domain

data class SoundsConfig(
    var isAzanEnabled: Boolean = true,
    var isIqamaEnabled: Boolean = false,
    var isPlayWhileMute: Boolean = false,

    var azanSoundUriString: String? = null,
    var iqamaSoundUriString: String? = null,
    var notifySoundUriString: String? = null,

    var fajrSoundState: SoundStates = SoundStates.AZAN,
    var iqamaFajrSoundState: SoundStates = SoundStates.IQAMA,
    var sunriseSoundState: SoundStates = SoundStates.NOTIFY,
    var duhaSoundState: SoundStates = SoundStates.MUTE,
    var zuhrSoundState: SoundStates = SoundStates.AZAN,
    var iqamaZuhrSoundState: SoundStates = SoundStates.IQAMA,
    var asrSoundState: SoundStates = SoundStates.AZAN,
    var iqamaAsrSoundState: SoundStates = SoundStates.IQAMA,
    var sunsetSoundState: SoundStates = SoundStates.AZAN,
    var iqamaSunsetSoundState: SoundStates = SoundStates.IQAMA,
    var ishaSoundState: SoundStates = SoundStates.AZAN,
    var iqamaIshaSoundState: SoundStates = SoundStates.IQAMA,
    var midnightSoundState: SoundStates = SoundStates.MUTE,
    var lastThirdSoundState: SoundStates = SoundStates.NOTIFY
)
