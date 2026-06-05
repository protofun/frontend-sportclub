package org.example.project.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1565C0)
val PrimaryDark = Color(0xFF0D47A1)
val PrimaryLight = Color(0xFF1976D2)
val PrimaryContainer = Color(0xFFD3E2FF)
val OnPrimary = Color.White

val Secondary = Color(0xFFFF6D00)
val SecondaryContainer = Color(0xFFFFE0B2)
val OnSecondary = Color.White

val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF5F5F5)
val Background = Color(0xFFF8F9FA)
val OnBackground = Color(0xFF1A1A2E)
val OnSurface = Color(0xFF212121)
val OnSurfaceVariant = Color(0xFF757575)

val Error = Color(0xFFD32F2F)
val Success = Color(0xFF388E3C)
val Warning = Color(0xFFF57C00)

val SportClubColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    background = Background,
    onBackground = OnBackground,
    error = Error
)

@Composable
fun SportClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SportClubColorScheme,
        typography = Typography(),
        content = content
    )
}
