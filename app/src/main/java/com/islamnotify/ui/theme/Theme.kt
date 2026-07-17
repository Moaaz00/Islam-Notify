package com.islamnotify.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * ---------------------------------------------------------------------------
 * Theme system
 * ---------------------------------------------------------------------------
 * Every theme is GREEN_LIGHT's structure re-hued. GREEN_LIGHT is the reference
 * and is reproduced here verbatim; the rest are derived from it so they carry
 * the same weight and contrast.
 *
 * The design is "warm parchment paper + one brand hue". The neutrals below are
 * hue 45-49 (cream) and contain no green at all — the brand colour appears ONLY
 * in primary/secondary and the header gradient. That separation is what makes
 * the reference read like a printed almanac rather than a colour-washed app, so
 * the neutrals are shared verbatim by all six light themes; only `primary` and
 * `backgroundGradient` change.
 *
 * Light themes, per role:
 *   primary/secondary  brand hue at L* 49.8  -> exactly 4.5:1 against white
 *   gradient L*        62.5 / 47.3 / 36.9 / 36.9 / 26.4   (an even ramp)
 *   gradient hue       rotated toward yellow at the top (sunlit) and away from it at the
 *                      foot (shadow). Green swings 85 -> 130 deg; that rotation is what
 *                      gives the header depth. The amount is clamped per hue so the top
 *                      stop cannot leave its own colour family.
 *   gradient sat       primary_sat + k * (1 - primary_sat), k = .127/.017/.151/.151/.079
 *
 * Dark themes reuse the same hues and saturations, dropping the gradient to
 * L* 38.5/27.1/17/17/9.1 and lifting `primary` to L* 79.8 (Material tone-80) so
 * the accent stays luminous on a near-black surface.
 *
 * Saturation is tuned per hue rather than fixed: cool hues keep their identity when muted
 * (blue holds at 30%), but warm hues collapse into mud below ~30%, so gold carries 58%.
 * Brown IS a low-saturation orange, which is why it sits at 32%.
 *
 * Two themes deviate from the L* 49.8 accent on purpose, because the hue cannot express
 * its own identity at that lightness:
 *   RED  sits at L* 24.8 (#78092A). A red at L* 49.8 is a brick/terracotta, not a wine.
 *        Its whole ramp shifts down with it; only its top stop is lifted back to L* 44
 *        for a crimson glow.
 *   PINK keeps the L* 49.8 accent but lifts its top stop to L* 68, because cotton-candy
 *        pink is *defined* by being light -- at L* 49.8 any pink reads raspberry. The body
 *        stays dark enough to carry white text; the candy lives in the top stop.
 *
 * Red and pink sit only 10 deg apart on the wheel, so in light mode they are told apart by
 * depth (wine vs candy). Dark mode would collapse that difference, so red's dark ramp is
 * based at L* 43 rather than 49.8 -- it stays the deep theme in both modes.
 *
 * Superseded commented-out palettes were removed; `git show HEAD~1 -- <file>`
 * has them if any are still wanted.
 */

// Unreachable fallbacks (`themeType` covers all twelve enum entries) kept for
// the dynamic-colour / no-theme path.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/*Possible primary colors
BROWNS:
1- 6b4c3a
2- 5d4037
3- 4e342e
4- 611c1c (could be red)
5- 7d532c
6- 704241
7- ab9889


PURPLES:
1- 33366c (could be blue)
2- 373d7d
3- 7360ba
4- 433245

BLUES:
1- 1a4862
2- 1d5063
3- 0e4d64
4- 2e3b55
5- 28697d
6- 234e52

GREENS:
1- 4b5d3f
2- 3d5245
3- 3a4a31
4- 4a5d23
5- 095f54 (could be blue)
6- 2b5c56 (could be blue)
7- 397a40

YELLOWS && ORANGES:
1- 8c550b
2- 966014
3- 9bb02f (could be green)
4- dc9e51


REDS:
1- 7f1734
2- 8c1a3c
3- e32c42
4- fc6063 (could be orange)
*/

// ---------------------------------------------------------------- GREEN
private val greenLightColorScheme = lightColorScheme(
    primary = Color(0xFF657D5D),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF657D5D),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val greenLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF85A05F),
        Color(0xFF5E7756),
        Color(0xFF3F5F36),
        Color(0xFF3F5F36),
        Color(0xFF2C4430),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val greenDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB7CCAD),
    onPrimary = Color(0xFF1E2E1A),

    secondary = Color(0xFF364431),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF1A1C18),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF23261F),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF43493E)
)

val greenDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF506039),
        Color(0xFF364431),
        Color(0xFF1E2E1A),
        Color(0xFF1E2E1A),
        Color(0xFF121C14),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF4B5344),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

// ---------------------------------------------------------------- BLUE
private val blueLightColorScheme = lightColorScheme(
    primary = Color(0xFF5779A1),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF5779A1),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val blueLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF5EA0B8),
        Color(0xFF51729A),
        Color(0xFF375983),
        Color(0xFF375983),
        Color(0xFF363871),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val blueDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB5C7DD),
    onPrimary = Color(0xFF1B2B3F),

    secondary = Color(0xFF2E4158),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF191B1D),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF202627),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF41494C)
)

val blueDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF326071),
        Color(0xFF2E4158),
        Color(0xFF1B2B3F),
        Color(0xFF1B2B3F),
        Color(0xFF16172F),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF475256),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

// ---------------------------------------------------------------- BROWN
private val brownLightColorScheme = lightColorScheme(
    primary = Color(0xFF9B6C50),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF9B6C50),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val brownLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFB89056),
        Color(0xFF95654B),
        Color(0xFF7C4B32),
        Color(0xFF7C4B32),
        Color(0xFF642E31),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val brownDarkColorScheme = darkColorScheme(
    primary = Color(0xFFDCC0B0),
    onPrimary = Color(0xFF3C2418),

    secondary = Color(0xFF553A2B),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF1D1B18),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF272420),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF4B4740)
)

val brownDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF705630),
        Color(0xFF553A2B),
        Color(0xFF3C2418),
        Color(0xFF3C2418),
        Color(0xFF2B1315),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF554F46),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

// ---------------------------------------------------------------- YELLOW
private val yellowLightColorScheme = lightColorScheme(
    primary = Color(0xFF8C7425),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF8C7425),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val yellowLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFA99826),
        Color(0xFF856D23),
        Color(0xFF6A5417),
        Color(0xFF6A5417),
        Color(0xFF623217),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val yellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD3C599),
    onPrimary = Color(0xFF33280B),

    secondary = Color(0xFF4C3E14),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF1B1B18),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF25251F),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF48473D)
)

val yellowDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF665C17),
        Color(0xFF4C3E14),
        Color(0xFF33280B),
        Color(0xFF33280B),
        Color(0xFF29140A),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF515142),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

// ---------------------------------------------------------------- RED
private val redLightColorScheme = lightColorScheme(
    primary = Color(0xFF78092A),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF78092A),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val redLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFCF0E2E),
        Color(0xFF6D0827),
        Color(0xFF45041A),
        Color(0xFF45041A),
        Color(0xFF2A031F),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val redDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE3BCC8),
    onPrimary = Color(0xFF3E0417),

    secondary = Color(0xFF650824),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF1F1A1B),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF2A2323),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF504446)
)

val redDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF980A21),
        Color(0xFF650824),
        Color(0xFF3E0417),
        Color(0xFF3E0417),
        Color(0xFF2A031F),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF5C4C4D),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

// ---------------------------------------------------------------- PINK
private val pinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFCD4182),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFFCD4182),
    onSecondary = Color(0xFFFDFCF9),

    background = Color(0xFFEFEDE4),
    onBackground = Color(0xFF3D3D3D),

    surface = Color(0xFFFDFCF9),
    onSurface = Color(0xFF1B1B1B),

    onSurfaceVariant = Color(0xFF797770),
    outline = Color(0xFFD1CEC0),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val pinkLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFE58BAC),
        Color(0xFFC9347B),
        Color(0xFF9F2361),
        Color(0xFF9F2361),
        Color(0xFF6D1A63),
    ),
    onHeader = Color(0xFFFFFFFF),
    switchTrackOff = Color(0xFF797770),
    switchThumbOff = Color(0xFFFFFFFF),
    bg_alpha = 0.05f
)

private val pinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2BBCD),
    onPrimary = Color(0xFF501130),

    secondary = Color(0xFF741E47),
    onSecondary = Color(0xFFE2E3DA),

    background = Color(0xFF1F1A1B),
    onBackground = Color(0xFFE2E3DA),

    surface = Color(0xFF2A2324),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB),
    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF504448)
)

val pinkDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFA82655),
        Color(0xFF741E47),
        Color(0xFF501130),
        Color(0xFF501130),
        Color(0xFF2F0B2B),
    ),
    onHeader = Color(0xFFE2E3DA),
    switchTrackOff = Color(0xFF5C4B4F),
    switchThumbOff = Color(0xFFC5C8BB),
    bg_alpha = 0.03f
)

val LocalCustomColors = staticCompositionLocalOf {
    greenLightThemeColors
}


@Composable
fun IslamNotifyTheme(
    themeType: AppThemeTypes = AppThemeTypes.GREEN_LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        themeType == AppThemeTypes.GREEN_LIGHT -> greenLightColorScheme
        themeType == AppThemeTypes.GREEN_DARK -> greenDarkColorScheme

        themeType == AppThemeTypes.BLUE_DARK -> blueDarkColorScheme
        themeType == AppThemeTypes.BLUE_LIGHT -> blueLightColorScheme

        themeType == AppThemeTypes.YELLOW_DARK -> yellowDarkColorScheme
        themeType == AppThemeTypes.YELLOW_LIGHT -> yellowLightColorScheme

        themeType == AppThemeTypes.BROWN_DARK -> brownDarkColorScheme
        themeType == AppThemeTypes.BROWN_LIGHT -> brownLightColorScheme

        themeType == AppThemeTypes.PINK_DARK -> pinkDarkColorScheme
        themeType == AppThemeTypes.PINK_LIGHT -> pinkLightColorScheme

        themeType == AppThemeTypes.RED_DARK -> redDarkColorScheme
        themeType == AppThemeTypes.RED_LIGHT -> redLightColorScheme


        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = when (themeType) {
        AppThemeTypes.GREEN_LIGHT -> greenLightThemeColors
        AppThemeTypes.GREEN_DARK -> greenDarkThemeColors

        AppThemeTypes.BLUE_DARK -> blueDarkThemeColors
        AppThemeTypes.BLUE_LIGHT -> blueLightThemeColors

        AppThemeTypes.YELLOW_DARK -> yellowDarkThemeColors
        AppThemeTypes.YELLOW_LIGHT -> yellowLightThemeColors

        AppThemeTypes.BROWN_DARK -> brownDarkThemeColors
        AppThemeTypes.BROWN_LIGHT -> brownLightThemeColors

        AppThemeTypes.PINK_DARK -> pinkDarkThemeColors
        AppThemeTypes.PINK_LIGHT -> pinkLightThemeColors

        AppThemeTypes.RED_DARK -> redDarkThemeColors
        AppThemeTypes.RED_LIGHT -> redLightThemeColors
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: CustomThemeColors
        @Composable
        get() = LocalCustomColors.current
}
