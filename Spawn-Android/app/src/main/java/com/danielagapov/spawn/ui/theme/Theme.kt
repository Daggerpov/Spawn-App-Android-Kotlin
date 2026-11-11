package com.danielagapov.spawn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.danielagapov.spawn.R

/** Palette from the iOS app's Constants.swift (which mirrors the Figma color styles). */
object Palette {
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    val Gray50 = Color(0xFFF2EEEE)
    val Gray100 = Color(0xFFE0DADA)
    val Gray200 = Color(0xFFD1CBCB)
    val Gray300 = Color(0xFFA9A0A0)
    val Gray400 = Color(0xFF857C7C)
    val Gray500 = Color(0xFF625A5A)
    val Gray600 = Color(0xFF3E3B3B)
    val Gray700 = Color(0xFF262424)
    val Gray800 = Color(0xFF1F1E1E)
    val Gray900 = Color(0xFF1B1B1B)
    val GrayInput = Color(0xFFF5F5F5)

    val Indigo100 = Color(0xFFEBEEFD)
    val Indigo200 = Color(0xFFDADFFF)
    val Indigo300 = Color(0xFFB6C1FF)
    val Indigo400 = Color(0xFF91A2FF)
    val Indigo500 = Color(0xFF6B81FB)
    val Indigo600 = Color(0xFF536AEE)
    val Indigo700 = Color(0xFF344DDE)

    val Red500 = Color(0xFFFF7270)
    val Red600 = Color(0xFFFD4E4C)
    val Green400 = Color(0xFF46DEA1)
    val Green500 = Color(0xFF30D895)
    val Green600 = Color(0xFF1AB979)
    val Blue600 = Color(0xFF1D85E7)
    val Blue700 = Color(0xFF0B6ACD)
    val Pink500 = Color(0xFFED64A6)

    val FigmaNavy = Color(0xFF1E265A)
    val ChevronCircle = Color(0xFFDBE0FF)
    val TypeButton = Color(0xFFEDEFF6)
    val TabActive = Color(0xFF536AEE)
    val TabInactive = Color(0xFF4B527F)
    val TabBackground = Color(0xFFDFE2F5)
    val HappeningNow = Color(0xFF46DEA1)
}

/** The four activity colors and their Figma gradients (iOS ActivityGradient.swift). */
object ActivityPalette {
    val base = listOf(Palette.Red600, Palette.Pink500, Palette.Green600, Palette.Blue600)

    private val stops: List<List<Pair<Float, Color>>> = listOf(
        listOf(0f to Color(0xFFFB6C6A), 0.5f to Color(0xFFCA4161), 0.75f to Color(0xFFB12B5C), 1f to Color(0xFF981657)),
        listOf(0f to Color(0xFFFB6AC6), 0.25f to Color(0xFFCE55BA), 0.5f to Color(0xFFA140AF), 0.75f to Color(0xFF742BA4), 1f to Color(0xFF481698)),
        listOf(0f to Color(0xFF7DF883), 0.25f to Color(0xFF63DA75), 0.5f to Color(0xFF49BC66), 0.75f to Color(0xFF2E9E57), 1f to Color(0xFF148049)),
        listOf(0f to Color(0xFF6AD2FB), 0.25f to Color(0xFF5DA3E2), 0.5f to Color(0xFF4F74CA), 0.75f to Color(0xFF4245B1), 0.875f to Color(0xFF3B2DA4), 1f to Color(0xFF341698)),
    )

    fun stops(index: Int): Array<Pair<Float, Color>> = stops[index.mod(4)].toTypedArray()
    fun color(index: Int): Color = base[index.mod(4)]
    fun deep(index: Int): Color = stops[index.mod(4)].firstOrNull { it.first == 0.75f }?.second ?: base[index.mod(4)]
    fun light(index: Int): Color = stops[index.mod(4)].first().second

    /** Cards: radial gradient centered bottom-trailing. */
    fun card(index: Int, width: Float, height: Float): Brush = Brush.radialGradient(
        colorStops = stops(index),
        center = androidx.compose.ui.geometry.Offset(width, height),
        radius = kotlin.math.hypot(width, height).coerceAtLeast(1f),
    )

    /** Pills/buttons: linear deep -> light. */
    fun pill(index: Int): Brush = Brush.horizontalGradient(listOf(deep(index), light(index)))
}

@Immutable
data class SpawnColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val heading: Color,
    val text: Color,
    val textSecondary: Color,
    val placeholder: Color,
    val divider: Color,
    val accent: Color,
    val chevronCircle: Color,
    val typeButton: Color,
    val tabBar: Color,
    val tabActive: Color,
    val tabInactive: Color,
    val input: Color,
    val isDark: Boolean,
)

private val LightSpawnColors = SpawnColors(
    background = Palette.White,
    surface = Palette.White,
    surfaceMuted = Palette.Gray50,
    heading = Palette.FigmaNavy,
    text = Palette.Gray900,
    textSecondary = Palette.Gray500,
    placeholder = Palette.Gray400,
    divider = Palette.Gray100,
    accent = Palette.Indigo500,
    chevronCircle = Palette.ChevronCircle,
    typeButton = Palette.TypeButton,
    tabBar = Color(0xF2EEF0FB),
    tabActive = Palette.TabActive,
    tabInactive = Palette.TabInactive,
    input = Palette.GrayInput,
    isDark = false,
)

private val DarkSpawnColors = SpawnColors(
    background = Palette.Gray900,
    surface = Palette.Gray800,
    surfaceMuted = Palette.Gray700,
    heading = Palette.White,
    text = Palette.White,
    textSecondary = Palette.Gray300,
    placeholder = Palette.Gray500,
    divider = Palette.Gray600,
    accent = Palette.Indigo400,
    chevronCircle = Color(0xFF2D335C),
    typeButton = Palette.Gray700,
    tabBar = Color(0xF2262424),
    tabActive = Palette.Indigo400,
    tabInactive = Palette.Gray300,
    input = Palette.Gray700,
    isDark = true,
)

val LocalSpawnColors = staticCompositionLocalOf { LightSpawnColors }

val Onest = FontFamily(
    Font(R.font.onest_regular, FontWeight.Normal),
    Font(R.font.onest_medium, FontWeight.Medium),
    Font(R.font.onest_semibold, FontWeight.SemiBold),
    Font(R.font.onest_bold, FontWeight.Bold),
    Font(R.font.onest_extra_bold, FontWeight.ExtraBold),
    Font(R.font.onest_light, FontWeight.Light),
)

/** iOS uses .onestRegular/.onestMedium/.onestSemiBold/.onestBold(size:) everywhere — same helper here. */
fun onest(size: TextUnit, weight: FontWeight = FontWeight.Normal, lineHeight: TextUnit = TextUnit.Unspecified) =
    TextStyle(fontFamily = Onest, fontWeight = weight, fontSize = size, lineHeight = lineHeight)

private val SpawnTypography = Typography(
    displayLarge = onest(32.sp, FontWeight.Bold),
    headlineLarge = onest(28.sp, FontWeight.Bold),
    headlineMedium = onest(24.sp, FontWeight.SemiBold),
    titleLarge = onest(20.sp, FontWeight.SemiBold),
    titleMedium = onest(17.sp, FontWeight.SemiBold),
    titleSmall = onest(15.sp, FontWeight.SemiBold),
    bodyLarge = onest(16.sp),
    bodyMedium = onest(14.sp),
    bodySmall = onest(13.sp),
    labelLarge = onest(15.sp, FontWeight.SemiBold),
    labelMedium = onest(13.sp, FontWeight.Medium),
    labelSmall = onest(11.sp, FontWeight.Medium),
)

object SpawnTheme {
    val colors: SpawnColors @Composable get() = LocalSpawnColors.current
}

@Composable
fun SpawnTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val spawn = if (darkTheme) DarkSpawnColors else LightSpawnColors
    val material = if (darkTheme) {
        darkColorScheme(
            primary = Palette.Indigo400, onPrimary = Palette.White,
            background = spawn.background, surface = spawn.surface, onBackground = spawn.text, onSurface = spawn.text,
            surfaceVariant = spawn.surfaceMuted, secondary = Palette.Indigo300, error = Palette.Red500,
            surfaceContainerLow = spawn.surface, surfaceContainer = spawn.surface, surfaceContainerHigh = spawn.surfaceMuted,
        )
    } else {
        lightColorScheme(
            primary = Palette.Indigo500, onPrimary = Palette.White,
            background = spawn.background, surface = spawn.surface, onBackground = spawn.text, onSurface = spawn.text,
            surfaceVariant = spawn.surfaceMuted, secondary = Palette.Indigo600, error = Palette.Red600,
            surfaceContainerLow = Palette.White, surfaceContainer = Palette.White, surfaceContainerHigh = Palette.Indigo100,
        )
    }
    CompositionLocalProvider(LocalSpawnColors provides spawn) {
        MaterialTheme(colorScheme = material, typography = SpawnTypography, content = content)
    }
}
