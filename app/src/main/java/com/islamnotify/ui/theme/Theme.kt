package com.islamnotify.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


//private val OliveColorScheme = lightColorScheme(
//    primary = Color(0xFF064E3B),          // dark_green (Brand identity)
//    onPrimary = Color(0xFFFBFAF7),              // Text on dark green
//
//    secondary = Color(0xFF064E3B),        // active_card_green (Highlights)
//    onSecondary = Color(0xFFFBFAF7),            // Text on active cards
//
//    background = Color(0xFFF4F1EA),       // off_white (The main screen bottom)
//    onBackground = Color(0xFF4E4E4E),     // dark_gray (Main text)
//
//    surface = Color(0xFFFBFAF7),                // Inactive cards
//    onSurface = Color(0xFF4E4E4E),        // Text on inactive cards
//
//    outlineVariant = Color(0x00000000).copy(alpha = 0.05f) // Your border stroke color
//)
//
//val OliveCustomThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF4A6A3F),
//        Color(0xFF3E5C36),
//        Color(0xFF2F4730)
//    )
//)

//private val OliveColorScheme = lightColorScheme(
//    primary = Color(0xFF4A6A3F),          // dark_green (Brand identity)
//    onPrimary = Color(0xFFFBFAF7),              // Text on dark green
//
//    secondary = Color(0xFF55734B),        // active_card_green (Highlights)
//    onSecondary = Color(0xFFFBFAF7),            // Text on active cards
//
//    background = Color(0xFFF4F1EA),       // off_white (The main screen bottom)
//    onBackground = Color(0xFF4E4E4E),     // dark_gray (Main text)
//
//    surface = Color(0xFFFBFAF7),                // Inactive cards
//    onSurface = Color(0xFF4E4E4E),        // Text on inactive cards
//
//    outlineVariant = Color(0x00000000).copy(alpha = 0.05f) // Your border stroke color
//)
//
//val OliveCustomThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF4A6A3F),
//        Color(0xFF3E5C36),
//        Color(0xFF2F4730)
//    )
//)

//private val OliveColorScheme = lightColorScheme(
//    primary = Color(0xFF4A6A3F),          // dark_green (Brand identity)
//    onPrimary = Color(0xFFFBFAF7),              // Text on dark green
//
//    secondary = Color(0xFF55734B),        // active_card_green (Highlights)
//    onSecondary = Color(0xFFFBFAF7),            // Text on active cards
//
//    background = Color(0xFFF1EEE7),       // off_white (The main screen bottom)
//    onBackground = Color(0xFF595959),     // dark_gray (Main text)
//
//    surface = Color(0xFFFBFAF7),                // Inactive cards
//    onSurface = Color(0xFF595959),        // Text on inactive cards
//
//    outlineVariant = Color(0x00000000).copy(alpha = 0.05f) // Your border stroke color
//)
//
//val OliveCustomThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF4A6A3F),
//        Color(0xFF3E5C36),
//        Color(0xFF2F4730)
//    )
//)

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

//private val blueDarkColorScheme = darkColorScheme(
//    // LOGIC: Shifted to a lighter "Mist Blue" (Tonal 80) to pop against dark backgrounds
//    // This matches the luminance of your Green B7CCAD
//    primary = Color(0xFF25383e),
//    onPrimary = Color(0xFFE1E3E4), // Cool-toned off-white for text on primary
//
//    // LOGIC: Active prayer card uses a deep Slate Blue
//    // Matches the 0xFF364431 logic from the green theme
//    secondary = Color(0xFF25383e),
//    onSecondary = Color(0xFFE1E3E4),
//
//    // LOGIC: Deep "Charcoal Slate". Keeps the cool tone of blue light mode
//    // but at the same 10-12% luminance as the green dark mode.
//    background = Color(0xFF191C1E),
//    onBackground = Color(0xFFE1E3E4), // Cool white for readability
//
//    // LOGIC: Slightly lighter than background (Elevation 1)
//    surface = Color(0xFF222628),
//    onSurface = Color(0xFFE1E3E4),
//
//    onSurfaceVariant = Color(0xFFC1C8CC), // Light blue-grey for secondary text
//
//    outline = Color(0xFF8A9296),
//    outlineVariant = Color(0xFF40484B) // Subtle border for cards
//)
//
//val blueDarkThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF2A3F44), // Top: Muted dark teal
//        Color(0xFF22353B), // Transition
//        Color(0xFF1B2E36), // Middle: Deep Slate
//        Color(0xFF16252B), // Lower
//        Color(0xFF16252B), // Lower
//        Color(0xFF0E161B)  // Bottom: Deepest Navy/Charcoal
//    ),
//    // Consistent with your green dark theme
//    bg_alpha = 0.03f
//)

private val blueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DB9C9), // Lighter slate blue (better contrast for dark mode)
    onPrimary = Color(0xFFFFFFFF), // Dark navy text on light blue

    secondary = Color(0xFF5D7B8C), // Original slate blue for active prayer card
    onSecondary = Color(0xFFFFFFFF), // White text on active card

    background = Color(0xFF121417), // Deep charcoal blue-black
    onBackground = Color(0xFFE2E2E6), // Soft white text (less eye strain than pure white)


    surface = Color(0xFF1C2024), // Slightly lighter slate-grey for inactive cards
    onSurface = Color(0xFFC4C7CB), // Muted grey text

    onSurfaceVariant = Color(0xFF8E9194), // Muted blue-grey for disabled states

    outline = Color(0xFF44474A), // Dark grey switch head
    outlineVariant = Color(0xFFFFFFFF).copy(0.1f) // Subtle light border for cards
)

// header background (Dark Midnight variant)
val blueDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF3D5361), // Top: Much more visible Slate Blue (distinguishes the header)
        Color(0xFF2D3E49), // Middle-Top: Deep Steel Blue
        Color(0xFF222F38), // Middle: Dark Navy
        Color(0xFF1B252C), // Shadow: Very dark blue
        Color(0xFF1B252C), // Shadow: Very dark blue
        Color(0xFF151D24)  // Bottom: Ends just slightly lighter than the background
    ),
    0.03f
)


private val blueLightColorScheme = lightColorScheme(
    primary = Color(0xFF5D7B8C), // Muted Slate Blue (matches the Sage Green tone)
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF5D7B8C), // active prayer card
    onSecondary = Color(0xFFF9FAFB), // on active card

    background = Color(0xFFEBEEF0), // Cool linen (replaces the greenish-tinted EFEDE4)
    onBackground = Color(0xFF353A3D), // Dark charcoal with a hint of navy


    surface = Color(0xFFF9FAFB), // Very light misty white
    onSurface = Color(0xFF1A1C1E), // Near black with cool undertone

    onSurfaceVariant = Color(0xFF73787B), // Muted blue-grey for disabled states

    outline = Color(0xFFCCD3D6), // Steel-grey switch head
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

// header background (Slate/Midnight variant)
val blueLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF809EB0), // Top: Misty blue
        Color(0xFF5D7B8C), // Middle: Your brand blue
        Color(0xFF4A6575), // Depth: Stormy blue
        Color(0xFF2D3E49), // Shadow: Deep charcoal blue
        Color(0xFF2D3E49), // Shadow: Deep charcoal blue
        Color(0xFF1D2830)  // Bottom: Midnight (grounds the theme)
    ),
    0.05f
)
//private val blueLightColorScheme = lightColorScheme(
//    primary = Color(0xFF326f82), // main brand blue (your provided color)
//    onPrimary = Color(0xFFFFFFFF), // on gradient
//
//    secondary = Color(0xFF2d6473), // active prayer card
//    onSecondary = Color(0xFFF7F9FA), // on active card
//
//    background = Color(0xFFf3f3f1), // prayer list section (cool-toned off-white)
//    onBackground = Color(0xFF353B3C), // on prayer list section
//
//
//    surface = Color(0xFFFFFFFF), // inactive cards above the prayer list (cool white)
//    onSurface = Color(0xFF1D2021), // on inactive cards
//
//    onSurfaceVariant = Color(0xFF70787A), // disabled switch (muted blue-grey)
//
//    outline = Color(0xFFC4CDD1), // switch head (muted cool grey)
//    outlineVariant = Color(0xFF000000).copy(0.1f) // prayer cards border
//)
//
//// header background (30-40% of the screen)
//val blueLightThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF538D93), // Top: Slightly more Cyan/Greenish (Teal)
//        Color(0xFF28697d), // Transition: Muted Blue-Teal
//        Color(0xFF225765), // Middle: Your Base Color (True Blue)
//        Color(0xFF225765), // Middle: Your Base Color (True Blue)
//        Color(0xFF225765), // Middle: Your Base Color (True Blue)
//        Color(0xFF1F4C5B), // Lower: Deeper Slate Blue
//        Color(0xFF1B364A)  // Bottom: Shifted toward Navy/Indigo (Subtle Purple hue)
//    ),
//    0.05f
//)


//val brownDarkColorScheme = darkColorScheme(
//    primary = Color(0xFF5D4037),
//    onPrimary = Color(0xFFFDF8F6), // Tinted White (Cream)
//    secondary = Color(0xFF4E342E),
//    onSecondary = Color(0xFFFDF8F6),
//    background = Color(0xFF1B1614),
//    onBackground = Color(0xFFF5F0EE),
//    surface = Color(0xFF2D2422),
//    onSurface = Color(0xFFF5F0EE),
//    onSurfaceVariant = Color(0xFFA1887F),
//    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
//)
//
//val brownDarkThemeColors = CustomThemeColors(listOf(
//    Color(0xFFA1887F), // Light warm tan (Start)
//    Color(0xFF5D4037), // Your accent (Middle)
//    Color(0xFF3E2723)  // Deep espresso (Bottom)
//))
private val brownDarkColorScheme = darkColorScheme(
    primary = Color(0xFFCBA584), // Lighter tan/sand
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF483727), // Original Ochre
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFF141210), // Near black with warm umber tint
    onBackground = Color(0xFFE6E1DC),

    surface = Color(0xFF1D1A17), // Dark chocolate-grey
    onSurface = Color(0xFFC9C3BD),
    onSurfaceVariant = Color(0xFF918C86),
    outline = Color(0xFF4A4540),
    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
)

val brownDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF5E452E), // Top: Warm clay glow
        Color(0xFF4A3624), // Middle: Deep umber
        Color(0xFF38291B), // Depth
        Color(0xFF291E14), // Shadow
        Color(0xFF291E14), // Shadow
        Color(0xFF1C140D)  // Bottom: Espresso
    ), 0.03f
)

private val brownLightColorScheme = lightColorScheme(
    primary = Color(0xFFA67C52), // Muted ochre (same weight as your green)
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFFA67C52), // active prayer card
    onSecondary = Color(0xFFFDFBF9), // on active card

    background = Color(0xFFF0EBE1), // warm linen (matches the tone of EFEDE4)
    onBackground = Color(0xFF3D3A35), // dark charcoal-brown


    surface = Color(0xFFFDFBF9), // very light cream
    onSurface = Color(0xFF1B1A17), // on inactive cards

    onSurfaceVariant = Color(0xFF7A756D), // muted brownish-grey

    outline = Color(0xFFD4CDBB), // sandy switch head
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

// header background (Earthy Amber/Gold variant)
val brownLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFBC9975), // Top: soft tan
        Color(0xFFA67C52), // Middle: brand ochre
        Color(0xFF8F6642), // Depth: clay
        Color(0xFF755136), // Shadow: burnt umber
        Color(0xFF755136), // Shadow: burnt umber
        Color(0xFF4A3423)  // Bottom: deep espresso
    ),
    0.05f
)




//val redDarkColorScheme = darkColorScheme(
//    primary = Color(0xFF7F1734),
//    onPrimary = Color(0xFFFFF1F2), // Tinted White (Rose)
//    secondary = Color(0xFF7F1734),
//    onSecondary = Color(0xFFFFF1F2),
//    background = Color(0xFF1E1113),
//    onBackground = Color(0xFFFCEFF1),
//    surface = Color(0xFF2D1B1E),
//    onSurface = Color(0xFFFCEFF1),
//    onSurfaceVariant = Color(0xFF9F8B8E),
//    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
//)
//
//val redDarkThemeColors = CustomThemeColors(listOf(
//    Color(0xFFE11D48), // Vibrant Red (Start)
//    Color(0xFF9F1239), // Rich Crimson (Middle)
//    Color(0xFF7F1734)  // Your accent (Bottom)
//))

private val redDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD17E95), // Lighter dusty rose/red
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF4c0f21), // Original Burgundy
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFF171113), // Near black with a wine tint
    onBackground = Color(0xFFE9E0E2),

    surface = Color(0xFF20181A), // Dark wine-grey
    onSurface = Color(0xFFCDC1C4),
    onSurfaceVariant = Color(0xFF968B8E),
    outline = Color(0xFF4D4245),
    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
)

val redDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF5E0C25), // Top: Deep garnet glow
        Color(0xFF4D0A1E), // Middle: Dark wine
        Color(0xFF3B0717), // Depth
        Color(0xFF2B0511), // Shadow
        Color(0xFF2B0511), // Shadow
        Color(0xFF1A030A)  // Bottom: Black-currant
    ), 0.03f
)

private val redLightColorScheme = lightColorScheme(
    primary = Color(0xFF851134), // Your brand red
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF851134), // active prayer card
    onSecondary = Color(0xFFFDF9FA), // on active card (slight rosy tint)

    background = Color(0xFFF2ECEE), // Warm ash with a hint of rose (replaces the green-tinted EFEDE4)
    onBackground = Color(0xFF3D3638), // Dark charcoal with a subtle red undertone


    surface = Color(0xFFFDF9FA), // Clean off-white
    onSurface = Color(0xFF1E1A1B), // Near black with warm undertone

    onSurfaceVariant = Color(0xFF7B7274), // Muted reddish-grey for disabled states

    outline = Color(0xFFD4C9CC), // Soft grey-pink switch head
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

// header background (Deep Red variant)
val redLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFA6445B), // Top: Lighter, softer crimson
        Color(0xFF851134), // Middle: Your brand red
        Color(0xFF6A0D2A), // Depth: Rich maroon
        Color(0xFF520A20), // Shadow: Deep wine
        Color(0xFF520A20), // Shadow: Deep wine
        Color(0xFF320513)  // Bottom: Dark black-currant
    ),
    0.05f
)


//val greenDarkColorScheme = darkColorScheme(
//    primary = Color(0xFF4B5D3F),
//    onPrimary = Color(0xFFF0FDF4), // Tinted White (Mint)
//    secondary = Color(0xFF36422D),
//    onSecondary = Color(0xFFF0FDF4),
//    background = Color(0xFF141A12),
//    onBackground = Color(0xFFECFDF5),
//    surface = Color(0xFF1E241B),
//    onSurface = Color(0xFFECFDF5),
//    onSurfaceVariant = Color(0xFF8B9487),
//    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
//)
//
//val greenDarkThemeColors = CustomThemeColors(listOf(
//    Color(0xFF8BA37E), // Light Sage (Start)
//    Color(0xFF4B5D3F), // Your accent (Middle)
//    Color(0xFF2D3A26)  // Deep Forest (Bottom)
//))

private val greenDarkColorScheme = darkColorScheme(
    // LOGIC: Shifted to a lighter "Sage" (Tonal 80) to pop against dark backgrounds
    primary = Color(0xFFB7CCAD),
    onPrimary = Color(0xFFE2E3DA), // Deep forest green for text on primary

    // LOGIC: Active prayer card uses the "Sage" highlight
    secondary = Color(0xFF364431),
    onSecondary = Color(0xFFE2E3DA),

    // LOGIC: Deep "Charcoal Olive". It keeps the warmth of your light mode
    // but at 10-12% luminance.
    background = Color(0xFF1A1C18),
    onBackground = Color(0xFFE2E3DA), // Creamy white for readability

    // LOGIC: Slightly lighter than background (Elevation 1)
    surface = Color(0xFF23261F),
    onSurface = Color(0xFFE2E3DA),

    onSurfaceVariant = Color(0xFFC5C8BB), // Lighter gray-green for secondary text

    outline = Color(0xFF8F9284),
    outlineVariant = Color(0xFF43493E) // Subtle border for cards
)


val greenDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
//        Color(0xFF4A6A3F),
        Color(0xFF506039),
//        Color(0xFF96AD75),
        Color(0xFF364431),
        Color(0xFF1E2E1A),
        Color(0xFF1E2E1A),
        Color(0xFF121C14)
    ),
    // Increased alpha slightly for the Mosque vector to make it
    // visible against the darker gradient
    bg_alpha = 0.03f
)



private val greenLightColorScheme = lightColorScheme(
    primary = Color(0xFF657D5D), // main brand green
    onPrimary = Color(0xFFFFFFFF), // on gradient

    secondary = Color(0xFF657D5D), // active prayer card
    onSecondary = Color(0xFFFDFCF9), // on active card

    background = Color(0xFFEFEDE4), // prayer list section
    onBackground = Color(0xFF3D3D3D), // on prayer list section


    surface = Color(0xFFFDFCF9), // inactive cards above the prayer list
    onSurface = Color(0xFF1B1B1B), // on inactive cards

    onSurfaceVariant = Color(0xFF797770), // disabled switch

    outline = Color(0xFFD1CEC0), // switch head
    outlineVariant = Color(0xFF000000).copy(0.1f) // prayer cards border
)

// header background (30-40% of the screen)
val greenLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(

//        Color(0xFF4A6A3F),
        Color(0xFF85A05F),
//        Color(0xFF96AD75),
        Color(0xFF5E7756),
        Color(0xFF3F5F36),
        Color(0xFF3F5F36),
        Color(0xFF2C4430)
    ), 0.05f
)


//private val greenLightColorScheme = lightColorScheme(
//    primary = Color(0xFF657d5d),          // main brand green
//    onPrimary = Color(0xFFFFFFFF),
//
//    // Slightly brighter for active states (more contrast)
//    secondary = Color(0xFF657d5d),
//    onSecondary = Color(0xFFFBFAF7),
//
//    background = Color(0xFFF4F1EA),       // slightly warmer (less gray)
//    onBackground = Color(0xFF4E4E4E),
//
//    surface = Color(0xFFFBFAF7),
//    onSurface = Color(0xFF4E4E4E),
//    onSurfaceVariant = Color(0xFFBDBDBD),
//
//    outlineVariant = Color(0xFF000000).copy(0.1f) // cleaner subtle border
//
//)

//private val greenLightColorScheme = lightColorScheme(
//    primary = Color(0xFF4A6A3F),          // main brand green
//    onPrimary = Color(0xFFFBFAF7),
//
//    // Slightly brighter for active states (more contrast)
//    secondary = Color(0xFF537249),
//    onSecondary = Color(0xFFFBFAF7),
//
//    background = Color(0xFFF4F1EA),       // slightly warmer (less gray)
//    onBackground = Color(0xFF4E4E4E),
//
//    surface = Color(0xFFFBFAF7),
//    onSurface = Color(0xFF4E4E4E),
//    onSurfaceVariant = Color(0xFFBDBDBD),
//
//    outlineVariant = Color(0xFF000000).copy(0.05f) // cleaner subtle border
//)
//
//val greenLightThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFF4A6A3F),
//        Color(0xFF3F5F36),
//        Color(0xFF2C4430)
//    )
//)


// -------- Custom41 (Pink Sunset)
//val yellowDarkColorScheme = darkColorScheme(
//    primary = Color(0xFFC5A028),
//    onPrimary = Color(0xFFFFFBEB), // Tinted White (Ivory)
//    secondary = Color(0xFFcf9f30),
//    onSecondary = Color(0xFFFFFBEB),
//    background = Color(0xFF1C1917),
//    onBackground = Color(0xFFFAFAF9),
//    surface = Color(0xFF292524),
//    onSurface = Color(0xFFFAFAF9),
//    onSurfaceVariant = Color(0xFFA8A29E),
//    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
//)
//
//val yellowDarkThemeColors = CustomThemeColors(listOf(
//    Color(0xFFD4AF37),
//    Color(0xFFB45309),
//    Color(0xFF92400E)
//))

private val yellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4B46C), // Lighter gold for readability
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF4e4126), // Original Mustard Gold
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFF14130F), // Near black with a hint of mustard
    onBackground = Color(0xFFE6E2D6),

    surface = Color(0xFF1E1C16), // Dark bronze-grey
    onSurface = Color(0xFFCAC5B9),
    onSurfaceVariant = Color(0xFF918D82),
    outline = Color(0xFF4A473E),
    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
)

val yellowDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF5E4E29), // Top: Deep bronze glow
        Color(0xFF473B1F), // Middle: Dark mustard
        Color(0xFF332A16), // Depth
        Color(0xFF241D0F), // Shadow
        Color(0xFF241D0F), // Shadow
        Color(0xFF1A150B)  // Bottom: Darkest earth
    ), 0.03f
)

//private val yellowLightColorScheme = lightColorScheme(
//    primary = Color(0xFFA88C3D), // Muted Mustard / Old Gold
//    onPrimary = Color(0xFFFFFFFF),
//
//    secondary = Color(0xFFA88C3D), // active prayer card
//    onSecondary = Color(0xFFFDFCF7), // on active card
//
//    background = Color(0xFFF1EEE1), // Warm vanilla (matches the tone of EFEDE4)
//    onBackground = Color(0xFF3D3A30), // Dark charcoal with a hint of gold
//
//
//    surface = Color(0xFFFDFCF7), // Creamy white
//    onSurface = Color(0xFF1B1B18), // on inactive cards
//
//    onSurfaceVariant = Color(0xFF7A776B), // Muted gold-grey
//
//    outline = Color(0xFFD4CFBC), // soft straw-colored outline
//    outlineVariant = Color(0xFF000000).copy(0.1f)
//)
//
//// header background (Mustard/Gold variant)
//val yellowLightThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFFCBB26A), // Top: Soft, dusty yellow
//        Color(0xFFA88C3D), // Middle: Brand Mustard
//        Color(0xFF917833), // Depth: Darker honey
//        Color(0xFF78632A), // Shadow: Deep bronze
//        Color(0xFF78632A), // Shadow: Deep bronze
//        Color(0xFF53451E)  // Bottom: Roasted earth
//    ),
//    0.05f
//)

private val yellowLightColorScheme = lightColorScheme(
    primary = Color(0xFFA88C3D), // Muted Mustard
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFFA88C3D),
    onSecondary = Color(0xFFFDFCF7),

    background = Color(0xFFF1EEE1),
    onBackground = Color(0xFF3D3A30),

    surface = Color(0xFFFDFCF7),
    onSurface = Color(0xFF1B1B18),

    onSurfaceVariant = Color(0xFF7A776B),

    outline = Color(0xFFD4CFBC),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val yellowLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFBBA77B), // Top: DUSTY Straw (Reduced glare)
        Color(0xFFA88C3D), // Middle: Brand Mustard
        Color(0xFF8E7735), // Depth: Darker honey
        Color(0xFF6B5926), // Shadow: Deep bronze
        Color(0xFF6B5926), // Shadow: Deep bronze
        Color(0xFF453A1B)  // Bottom: Roasted earth
    ),
    0.05f
)

//private val pinkDarkColorScheme = darkColorScheme(
//    primary = Color(0xFFFDA4AF), // main color accent
//    onPrimary = Color(0xFFFFF1F2), // texts above the header
//    secondary = Color(0xFFFB7185), // active card
//    onSecondary = Color(0xFFFFF1F2), // on active card
//    background = Color(0xFF332127), // populate the remaining screen space below the header
////    background = Color(0xFF1E293B),
//    onBackground = Color(0xFFFCEFF1), // text on the background
////    onBackground = Color(0xFFF1F5F9),
//    surface = Color(0xFF3E2D33), // inactive cards
////    surface = Color(0xFF334155),
//    onSurface = Color(0xFFFCEFF1), // on inactive cards
////    onSurface = Color(0xFFF1F5F9),
//    onSurfaceVariant = Color(0xFFB09EA3), // disabled switches
//    outlineVariant = Color(0xFFFFFFFF).copy(0.1f) // card strokes
//)
//
//// main screen header (30-40% of screen)
//val pinkDarkThemeColors = CustomThemeColors(listOf(
//    Color(0xFFFB7185),
//    Color(0xFFE11D48),
//    Color(0xFFE11D48 )
//))

private val pinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFDA4AF), // Lighter soft pink
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF5e2f35), // Original Coral Pink
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFF1A1315), // Near black with a mauve tint
    onBackground = Color(0xFFECE0E2),

    surface = Color(0xFF241C1E), // Dark plum-grey
    onSurface = Color(0xFFD0BFC3),
    onSurfaceVariant = Color(0xFF998A8E),
    outline = Color(0xFF524145),
    outlineVariant = Color(0xFFFFFFFF).copy(0.1f)
)

val pinkDarkThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFF7A3741), // Top: Muted rose glow
        Color(0xFF632D35), // Middle: Deep dusty rose
        Color(0xFF4D2329), // Depth
        Color(0xFF38191E), // Shadow
        Color(0xFF38191E), // Shadow
        Color(0xFF261114)  // Bottom: Deep mauve-black
    ), 0.03f
)

//private val pinkLightColorScheme = lightColorScheme(
//    primary = Color(0xFFFB7185), // Your brand pink/coral
//    onPrimary = Color(0xFFFFFFFF),
//
//    secondary = Color(0xFFef6e82), // active prayer card
//    onSecondary = Color(0xFFFFF9F9), // on active card
//
//    background = Color(0xFFF5F0EE), // Soft linen with a hint of warmth (matches the EFEDE4 vibe)
//    onBackground = Color(0xFF3D3738), // Deep charcoal with a warm plum undertone
//
//
//    surface = Color(0xFFFFF9F9), // Very light petal white
//    onSurface = Color(0xFF1F1A1B), // Near black with warm undertone
//
//    onSurfaceVariant = Color(0xFF7B7374), // Muted rose-grey for disabled states
//
//    outline = Color(0xFFD9D0D1), // Soft clay-pink switch head
//    outlineVariant = Color(0xFF000000).copy(0.1f)
//)

//// header background (Pink/Coral variant)
//val pinkLightThemeColors = CustomThemeColors(
//    backgroundGradient = listOf(
//        Color(0xFFFDA4AF), // Top: Soft, airy pink
//        Color(0xFFFB7185), // Middle: Your brand pink
//        Color(0xFFE15B6F), // Depth: Muted raspberry
//        Color(0xFFB94455), // Shadow: Deep rosewood
//        Color(0xFFB94455), // Shadow: Deep rosewood
//        Color(0xFF8D3441)  // Bottom: Dark wine (grounds the theme)
//    ),
//    0.05f
//)

private val pinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF7A84), // Muted Pink (Tone-matched to your Green 0xFF657D5D)
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFFBF7A84),
    onSecondary = Color(0xFFFFF9F9),

    background = Color(0xFFF5F0EE),
    onBackground = Color(0xFF3D3738),

    surface = Color(0xFFFFF9F9),
    onSurface = Color(0xFF1F1A1B),

    onSurfaceVariant = Color(0xFF7B7374),

    outline = Color(0xFFD9D0D1),
    outlineVariant = Color(0xFF000000).copy(0.1f)
)

val pinkLightThemeColors = CustomThemeColors(
    backgroundGradient = listOf(
        Color(0xFFD1919A), // Top: DUSTY Rose (Softer, matte feel)
        Color(0xFFBF7A84), // Middle: Muted Pink
        Color(0xFFA66670), // Depth: Muted raspberry
        Color(0xFF8A4E58), // Shadow: Deep rosewood
        Color(0xFF8A4E58), // Shadow: Deep rosewood
        Color(0xFF5C3339)  // Bottom: Dark wine
    ),
    0.05f
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
        else -> greenLightThemeColors
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
