package com.example.mockmate.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mockmate.R

object MocklyColors {
    val Outline = Color(0xFF727784)
    val Background = Color(0xFFF7F9FB)
    val SurfaceContainerHigh = Color(0xFFE6E8EA)
    val InverseSurface = Color(0xFF2D3133)
    val OnSurface = Color(0xFF191C1E)
    val TertiaryFixed = Color(0xFF6FFBBE)
    val InverseOnSurface = Color(0xFFEFF1F3)
    val OnSecondaryContainer = Color(0xFF57657A)
    val PrimaryContainer = Color(0xFF005BC5)
    val SurfaceTint = Color(0xFF005AC3)
    val SecondaryContainer = Color(0xFFD5E3FC)
    val OnPrimaryContainer = Color(0xFFCCDAFF)
    val PrimaryFixed = Color(0xFFD8E2FF)
    val OnErrorContainer = Color(0xFF93000A)
    val TertiaryContainer = Color(0xFF006D4A)
    val OnSecondaryFixed = Color(0xFF0D1C2E)
    val Error = Color(0xFFBA1A1A)
    val SurfaceContainerLow = Color(0xFFF2F4F6)
    val OutlineVariant = Color(0xFFC2C6D5)
    val OnSecondary = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE0E3E5)
    val SurfaceDim = Color(0xFFD8DADC)
    val OnTertiaryContainer = Color(0xFF65F2B5)
    val OnPrimaryFixedVariant = Color(0xFF004395)
    val Primary = Color(0xFF004497)
    val SurfaceBright = Color(0xFFF7F9FB)
    val SecondaryFixed = Color(0xFFD5E3FC)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnSecondaryFixedVariant = Color(0xFF3A485B)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnTertiaryFixed = Color(0xFF002113)
    val OnBackground = Color(0xFF191C1E)
    val Tertiary = Color(0xFF005237)
    val OnTertiaryFixedVariant = Color(0xFF005236)
    val PrimaryFixedDim = Color(0xFFAEC6FF)
    val Secondary = Color(0xFF515F74)
    val ErrorContainer = Color(0xFFFFDAD6)
    val SurfaceContainerHighest = Color(0xFFE0E3E5)
    val SurfaceContainer = Color(0xFFECEEF0)
    val OnPrimaryFixed = Color(0xFF001A42)
    val SecondaryFixedDim = Color(0xFFB9C7DF)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val OnError = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF7F9FB)
    val OnSurfaceVariant = Color(0xFF424753)
    val TertiaryFixedDim = Color(0xFF4EDEA3)
    val InversePrimary = Color(0xFFAEC6FF)
}

private val MocklyColorScheme: ColorScheme = lightColorScheme(
    primary = MocklyColors.Primary,
    onPrimary = MocklyColors.OnPrimary,
    primaryContainer = MocklyColors.PrimaryContainer,
    onPrimaryContainer = MocklyColors.OnPrimaryContainer,
    inversePrimary = MocklyColors.InversePrimary,
    secondary = MocklyColors.Secondary,
    onSecondary = MocklyColors.OnSecondary,
    secondaryContainer = MocklyColors.SecondaryContainer,
    onSecondaryContainer = MocklyColors.OnSecondaryContainer,
    tertiary = MocklyColors.Tertiary,
    onTertiary = MocklyColors.OnTertiary,
    tertiaryContainer = MocklyColors.TertiaryContainer,
    onTertiaryContainer = MocklyColors.OnTertiaryContainer,
    background = MocklyColors.Background,
    onBackground = MocklyColors.OnBackground,
    surface = MocklyColors.Surface,
    onSurface = MocklyColors.OnSurface,
    surfaceVariant = MocklyColors.SurfaceVariant,
    onSurfaceVariant = MocklyColors.OnSurfaceVariant,
    surfaceTint = MocklyColors.SurfaceTint,
    inverseSurface = MocklyColors.InverseSurface,
    inverseOnSurface = MocklyColors.InverseOnSurface,
    error = MocklyColors.Error,
    onError = MocklyColors.OnError,
    errorContainer = MocklyColors.ErrorContainer,
    onErrorContainer = MocklyColors.OnErrorContainer,
    outline = MocklyColors.Outline,
    outlineVariant = MocklyColors.OutlineVariant,
    surfaceBright = MocklyColors.SurfaceBright,
    surfaceDim = MocklyColors.SurfaceDim,
    surfaceContainer = MocklyColors.SurfaceContainer,
    surfaceContainerHigh = MocklyColors.SurfaceContainerHigh,
    surfaceContainerHighest = MocklyColors.SurfaceContainerHighest,
    surfaceContainerLow = MocklyColors.SurfaceContainerLow,
    surfaceContainerLowest = MocklyColors.SurfaceContainerLowest
)

private val MocklyDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8DB8FF),
    onPrimary = Color(0xFF002E68),
    primaryContainer = Color(0xFF005BC5),
    onPrimaryContainer = Color(0xFFD8E2FF),
    inversePrimary = MocklyColors.Primary,
    secondary = Color(0xFFBAC7DD),
    onSecondary = Color(0xFF243144),
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFD5E3FC),
    tertiary = Color(0xFF6FFBBE),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF005237),
    onTertiaryContainer = Color(0xFF9CFFD3),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFEFF1F3),
    surface = Color(0xFF0B1020),
    onSurface = Color(0xFFEFF1F3),
    surfaceVariant = Color(0xFF20283A),
    onSurfaceVariant = Color(0xFFC8CEDA),
    surfaceTint = Color(0xFF8DB8FF),
    inverseSurface = Color(0xFFEFF1F3),
    inverseOnSurface = Color(0xFF191C1E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9AA3B5),
    outlineVariant = Color(0xFF465064),
    surfaceBright = Color(0xFF1A2235),
    surfaceDim = Color(0xFF070B16),
    surfaceContainer = Color(0xFF111827),
    surfaceContainerHigh = Color(0xFF172033),
    surfaceContainerHighest = Color(0xFF202A40),
    surfaceContainerLow = Color(0xFF111827),
    surfaceContainerLowest = Color(0xFF161E2F)
)

private val HeadlineFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold),
    Font(R.font.manrope, FontWeight.ExtraBold)
)
private val BodyFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

private val MocklyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HeadlineFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

private val MocklyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Animates every color in a [ColorScheme] using spring-based animation
 * for a fluid dark/light mode crossfade.
 */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    val spec = spring<Color>(stiffness = Spring.StiffnessLow)
    return copy(
        primary = animateColorAsState(primary, spec, label = "primary").value,
        onPrimary = animateColorAsState(onPrimary, spec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, spec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, spec, label = "onPrimaryContainer").value,
        inversePrimary = animateColorAsState(inversePrimary, spec, label = "inversePrimary").value,
        secondary = animateColorAsState(secondary, spec, label = "secondary").value,
        onSecondary = animateColorAsState(onSecondary, spec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, spec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, spec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(tertiary, spec, label = "tertiary").value,
        onTertiary = animateColorAsState(onTertiary, spec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, spec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, spec, label = "onTertiaryContainer").value,
        background = animateColorAsState(background, spec, label = "background").value,
        onBackground = animateColorAsState(onBackground, spec, label = "onBackground").value,
        surface = animateColorAsState(surface, spec, label = "surface").value,
        onSurface = animateColorAsState(onSurface, spec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, spec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, spec, label = "onSurfaceVariant").value,
        surfaceTint = animateColorAsState(surfaceTint, spec, label = "surfaceTint").value,
        inverseSurface = animateColorAsState(inverseSurface, spec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, spec, label = "inverseOnSurface").value,
        error = animateColorAsState(error, spec, label = "error").value,
        onError = animateColorAsState(onError, spec, label = "onError").value,
        errorContainer = animateColorAsState(errorContainer, spec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(onErrorContainer, spec, label = "onErrorContainer").value,
        outline = animateColorAsState(outline, spec, label = "outline").value,
        outlineVariant = animateColorAsState(outlineVariant, spec, label = "outlineVariant").value,
        surfaceBright = animateColorAsState(surfaceBright, spec, label = "surfaceBright").value,
        surfaceDim = animateColorAsState(surfaceDim, spec, label = "surfaceDim").value,
        surfaceContainer = animateColorAsState(surfaceContainer, spec, label = "surfaceContainer").value,
        surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, spec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, spec, label = "surfaceContainerHighest").value,
        surfaceContainerLow = animateColorAsState(surfaceContainerLow, spec, label = "surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, spec, label = "surfaceContainerLowest").value,
        scrim = animateColorAsState(scrim, spec, label = "scrim").value
    )
}

@Composable
fun MocklyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetScheme = if (darkTheme) MocklyDarkColorScheme else MocklyColorScheme
    val animatedScheme = targetScheme.animated()

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = MocklyTypography,
        shapes = MocklyShapes,
        content = content
    )
}
