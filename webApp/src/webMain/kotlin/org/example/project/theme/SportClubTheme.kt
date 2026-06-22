package org.example.project.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color pallate

val Primary          = Color(0xFF1565C0) // dark blue
val PrimaryDark      = Color(0xFF0D47A1) // darker blue
val PrimaryLight     = Color(0xFF1976D2) // lighter blue
val PrimaryContainer = Color(0xFFD3E2FF) // very light blue
val OnPrimary        = Color.White

val Secondary          = Color(0xFFFF6D00) // orange — accent
val SecondaryContainer = Color(0xFFFFE0B2)
val OnSecondary        = Color.White

val Surface         = Color(0xFFFFFFFF) // white
val SurfaceVariant  = Color(0xFFF5F5F5) // light grey
val Background      = Color(0xFFF8F9FA) // off-white
val OnBackground    = Color(0xFF1A1A2E) // near-black
val OnSurface       = Color(0xFF212121) // dark grey
val OnSurfaceVariant = Color(0xFF757575) // medium grey

val Error   = Color(0xFFD32F2F) // red
val Success = Color(0xFF388E3C) // green
val Warning = Color(0xFFF57C00) // amber

val SportClubColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = PrimaryDark,
    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,
    background           = Background,
    onBackground         = OnBackground,
    error                = Error
)

// Wrap your root composable with SportClubTheme { } to apply colors and typography globally.
@Composable
fun SportClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SportClubColorScheme, typography = Typography(), content = content)
}
