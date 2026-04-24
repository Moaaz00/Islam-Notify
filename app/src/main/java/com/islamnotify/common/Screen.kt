package com.islamnotify.common

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

object Screen {
    @Keep @Serializable object Main
    @Keep @Serializable data object Settings
}