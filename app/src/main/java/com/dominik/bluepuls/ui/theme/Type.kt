package com.dominik.bluepuls.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.dominik.bluepuls.R

// Google Fonts provider - fontovi se preuzimaju preko Google Play Servicesa
// (nije potrebno bundlati .ttf datoteke). Ako preuzimanje ne uspije,
// Compose se elegantno vrati na sistemski font (bez pada).
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// --- Font obitelji iz design systema ---
val LexendFamily = FontFamily(
    Font(GoogleFont("Lexend"), googleFontProvider, FontWeight.Normal),
    Font(GoogleFont("Lexend"), googleFontProvider, FontWeight.Medium),
    Font(GoogleFont("Lexend"), googleFontProvider, FontWeight.SemiBold),
    Font(GoogleFont("Lexend"), googleFontProvider, FontWeight.Bold)
)

val RobotoFamily = FontFamily(
    Font(GoogleFont("Roboto"), googleFontProvider, FontWeight.Normal),
    Font(GoogleFont("Roboto"), googleFontProvider, FontWeight.Medium),
    Font(GoogleFont("Roboto"), googleFontProvider, FontWeight.Bold)
)

val TekturFamily = FontFamily(
    Font(GoogleFont("Tektur"), googleFontProvider, FontWeight.Normal),
    Font(GoogleFont("Tektur"), googleFontProvider, FontWeight.Bold)
)

/**
 * Tipografija Blue Pulsea:
 *  - Display (Tektur)  -> veliki sportski naslovi
 *  - Headline/Title/Label (Lexend) -> naslovi i naglasci
 *  - Body (Roboto)     -> tekst
 */
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = TekturFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp),
    displayMedium = TextStyle(fontFamily = TekturFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),

    headlineLarge = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),

    titleLarge = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),

    bodyLarge = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = LexendFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)
