package com.example.cycletracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext

private data class Scheme(val light: ColorScheme, val dark: ColorScheme)

private val RoseScheme = Scheme(
    light = lightColorScheme(
        primary = Color(0xFF9C2550),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3E0020),
        secondary = Color(0xFF74565F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E2),
        onSecondaryContainer = Color(0xFF2B151C),
        tertiary = Color(0xFF7B5630),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDDBD),
        onTertiaryContainer = Color(0xFF2C1600),
        background = Color(0xFFFFF8F8),
        onBackground = Color(0xFF22191B),
        surface = Color(0xFFFFF8F8),
        onSurface = Color(0xFF22191B),
        surfaceVariant = Color(0xFFF2DDE2),
        onSurfaceVariant = Color(0xFF514347),
        outline = Color(0xFF837377)
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB1C6),
        onPrimary = Color(0xFF5F1136),
        primaryContainer = Color(0xFF7C204B),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary = Color(0xFFE3BDC6),
        onSecondary = Color(0xFF422931),
        secondaryContainer = Color(0xFF5A3F48),
        onSecondaryContainer = Color(0xFFFFD9E2),
        tertiary = Color(0xFFEEC0A0),
        onTertiary = Color(0xFF462907),
        tertiaryContainer = Color(0xFF5F3F1B),
        onTertiaryContainer = Color(0xFFFFDDBD),
        background = Color(0xFF1A1113),
        onBackground = Color(0xFFF0DEE1),
        surface = Color(0xFF1A1113),
        onSurface = Color(0xFFF0DEE1),
        surfaceVariant = Color(0xFF514347),
        onSurfaceVariant = Color(0xFFD5C2C6),
        outline = Color(0xFF9F8C90)
    )
)

private val LavenderScheme = Scheme(
    light = lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E)
    ),
    dark = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99)
    )
)

private val TealScheme = Scheme(
    light = lightColorScheme(
        primary = Color(0xFF006A60),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF72F7E0),
        onPrimaryContainer = Color(0xFF00201C),
        secondary = Color(0xFF4A635E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8E2),
        onSecondaryContainer = Color(0xFF05201C),
        tertiary = Color(0xFF456179),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFCDE5FF),
        onTertiaryContainer = Color(0xFF001E31),
        background = Color(0xFFFAFDFB),
        onBackground = Color(0xFF191C1B),
        surface = Color(0xFFFAFDFB),
        onSurface = Color(0xFF191C1B),
        surfaceVariant = Color(0xFFDAE5E1),
        onSurfaceVariant = Color(0xFF3F4947),
        outline = Color(0xFF6F7976)
    ),
    dark = darkColorScheme(
        primary = Color(0xFF55DBCC),
        onPrimary = Color(0xFF003731),
        primaryContainer = Color(0xFF005048),
        onPrimaryContainer = Color(0xFF72F7E0),
        secondary = Color(0xFFB1CCC5),
        onSecondary = Color(0xFF1C3530),
        secondaryContainer = Color(0xFF334B46),
        onSecondaryContainer = Color(0xFFCCE8E2),
        tertiary = Color(0xFFB1CBE9),
        onTertiary = Color(0xFF1A3448),
        tertiaryContainer = Color(0xFF2D4961),
        onTertiaryContainer = Color(0xFFCDE5FF),
        background = Color(0xFF191C1B),
        onBackground = Color(0xFFE0E3E1),
        surface = Color(0xFF191C1B),
        onSurface = Color(0xFFE0E3E1),
        surfaceVariant = Color(0xFF3F4947),
        onSurfaceVariant = Color(0xFFBEC9C6),
        outline = Color(0xFF889390)
    )
)

private val PeachScheme = Scheme(
    light = lightColorScheme(
        primary = Color(0xFFA34A36),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD1),
        onPrimaryContainer = Color(0xFF3A0B02),
        secondary = Color(0xFF77574F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDAD1),
        onSecondaryContainer = Color(0xFF2C150F),
        tertiary = Color(0xFF6C5D2F),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF5E1A7),
        onTertiaryContainer = Color(0xFF231B00),
        background = Color(0xFFFFF8F6),
        onBackground = Color(0xFF221918),
        surface = Color(0xFFFFF8F6),
        onSurface = Color(0xFF221918),
        surfaceVariant = Color(0xFFF5DED8),
        onSurfaceVariant = Color(0xFF53433F),
        outline = Color(0xFF85736E)
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB4A1),
        onPrimary = Color(0xFF5C1A0A),
        primaryContainer = Color(0xFF7C2E1C),
        onPrimaryContainer = Color(0xFFFFDAD1),
        secondary = Color(0xFFE7BEB3),
        onSecondary = Color(0xFF442A22),
        secondaryContainer = Color(0xFF5D3F38),
        onSecondaryContainer = Color(0xFFFFDAD1),
        tertiary = Color(0xFFD8C68E),
        onTertiary = Color(0xFF3A2F04),
        tertiaryContainer = Color(0xFF524419),
        onTertiaryContainer = Color(0xFFF5E1A7),
        background = Color(0xFF201A18),
        onBackground = Color(0xFFEDE0DC),
        surface = Color(0xFF201A18),
        onSurface = Color(0xFFEDE0DC),
        surfaceVariant = Color(0xFF53433F),
        onSurfaceVariant = Color(0xFFD8C2BC),
        outline = Color(0xFFA08C86)
    )
)

private val BlueScheme = Scheme(
    light = lightColorScheme(
        primary = Color(0xFF435E91),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E2FF),
        onPrimaryContainer = Color(0xFF001A41),
        secondary = Color(0xFF575E71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDBE2F9),
        onSecondaryContainer = Color(0xFF141B2C),
        tertiary = Color(0xFF715573),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFCD7FF),
        onTertiaryContainer = Color(0xFF29132D),
        background = Color(0xFFF9F9FF),
        onBackground = Color(0xFF1A1B20),
        surface = Color(0xFFF9F9FF),
        onSurface = Color(0xFF1A1B20),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF45464F),
        outline = Color(0xFF757680)
    ),
    dark = darkColorScheme(
        primary = Color(0xFFAEC6FF),
        onPrimary = Color(0xFF0D2F60),
        primaryContainer = Color(0xFF2A477B),
        onPrimaryContainer = Color(0xFFD8E2FF),
        secondary = Color(0xFFBFC6DC),
        onSecondary = Color(0xFF293041),
        secondaryContainer = Color(0xFF404659),
        onSecondaryContainer = Color(0xFFDBE2F9),
        tertiary = Color(0xFFDFC0E8),
        onTertiary = Color(0xFF402844),
        tertiaryContainer = Color(0xFF583D5A),
        onTertiaryContainer = Color(0xFFFCD7FF),
        background = Color(0xFF121318),
        onBackground = Color(0xFFE3E2E9),
        surface = Color(0xFF121318),
        onSurface = Color(0xFFE3E2E9),
        surfaceVariant = Color(0xFF45464F),
        onSurfaceVariant = Color(0xFFC5C6D0),
        outline = Color(0xFF8F9099)
    )
)

private val Schemes = mapOf(
    "rose" to RoseScheme,
    "lavender" to LavenderScheme,
    "teal" to TealScheme,
    "peach" to PeachScheme,
    "blue" to BlueScheme
)

val THEME_OPTIONS = listOf(
    "rose" to "Розовая",
    "lavender" to "Лавандовая",
    "teal" to "Бирюзовая",
    "peach" to "Персиковая",
    "blue" to "Синяя",
    "dynamic" to "Системная"
)

fun schemePreviewColor(key: String): Color = when (key) {
    "rose" -> Color(0xFF9C2550)
    "lavender" -> Color(0xFF6750A4)
    "teal" -> Color(0xFF006A60)
    "peach" -> Color(0xFFA34A36)
    "blue" -> Color(0xFF435E91)
    else -> Color(0xFF6750A4)
}

/**
 * Свои схемы задают только основные цвета, а карточки, навигация и диалоги в Material 3
 * используют surfaceContainer*. Без них берутся стандартные фиолетовые цвета, которые
 * не подходят к теме. Выводим их из surface с лёгким оттенком primary.
 */
private fun ColorScheme.withSurfaceContainers(dark: Boolean): ColorScheme {
    fun tint(amount: Float) = lerp(surface, primary, amount)
    return if (dark) {
        copy(
            surfaceContainerLowest = lerp(surface, Color.Black, 0.35f),
            surfaceContainerLow = tint(0.05f),
            surfaceContainer = tint(0.08f),
            surfaceContainerHigh = tint(0.11f),
            surfaceContainerHighest = tint(0.14f),
            surfaceBright = tint(0.18f),
            surfaceDim = surface,
            surfaceTint = primary,
            inversePrimary = lerp(primary, Color.Black, 0.45f),
            outlineVariant = lerp(outline, surface, 0.55f)
        )
    } else {
        copy(
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = tint(0.025f),
            surfaceContainer = tint(0.045f),
            surfaceContainerHigh = tint(0.065f),
            surfaceContainerHighest = tint(0.085f),
            surfaceBright = surface,
            surfaceDim = tint(0.12f),
            surfaceTint = primary,
            inversePrimary = lerp(primary, Color.White, 0.45f),
            outlineVariant = lerp(outline, surface, 0.6f)
        )
    }
}

@Composable
fun CycleTrackerTheme(
    themeScheme: String = "rose",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeScheme == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val scheme = Schemes[themeScheme] ?: RoseScheme
            (if (darkTheme) scheme.dark else scheme.light).withSurfaceContainers(darkTheme)
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
