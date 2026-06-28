package com.dominik.bluepuls.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Definiramo kako se naše boje mapiraju na Material 3 komponente
private val BluePulseColorScheme = darkColorScheme(
    primary = SurfaceBlue01,
    secondary = SurfaceAccent,
    background = SurfaceBlue03,
    surface = SurfaceBlue02,
    onPrimary = TextWhite,
    onSecondary = TextBlue04,
    onBackground = TextWhite,
    onSurface = TextWhite
)

/**
 * Tema aplikacije. Boju i svjetlinu statusne/navigacijske trake postavlja
 * tema u `res/values/themes.xml` (tamna pozadina + svijetle ikone), pa ovdje
 * nema potrebe za zastarjelim `window.statusBarColor` API-jem.
 */
@Composable
fun BluePulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BluePulseColorScheme,
        typography = Typography,
        content = content
    )
}