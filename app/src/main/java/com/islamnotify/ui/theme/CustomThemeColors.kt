package com.islamnotify.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Theme colours that have no Material 3 role.
 *
 * @param backgroundGradient vertical gradient behind the whole screen. Only the top ~30 % is
 *   visible (the header) — the prayer list sheet covers the rest, so the first two stops are
 *   what the user actually sees.
 * @param onHeader content colour for text/icons drawn on [backgroundGradient]. This cannot be
 *   `onPrimary`: the header is dark in *both* light and dark themes, whereas `primary` is dark
 *   in light themes and a light tone in dark ones, so a single role cannot serve both.
 * @param switchTrackOff track of an unchecked switch. Also has no Material role that fits: M3's
 *   unchecked track is a light surface in light themes and a dark one in dark, but this app draws
 *   a solid mid-grey pill, so the value has to invert per mode rather than follow a surface.
 * @param switchThumbOff knob of an unchecked switch — light against [switchTrackOff] in both modes.
 * @param bg_alpha opacity of the mosque vector and the scrim over the gradient.
 */
data class CustomThemeColors(
    var backgroundGradient: List<Color>,
    var onHeader: Color,
    var switchTrackOff: Color,
    var switchThumbOff: Color,
    var bg_alpha: Float = 0.075f
)
