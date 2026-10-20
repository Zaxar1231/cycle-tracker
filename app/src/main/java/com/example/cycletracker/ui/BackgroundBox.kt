package com.example.cycletracker.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Готовый фон: цвет сверху, цвет снизу и два мягких пятна. Цвета светлые, для тёмной темы приглушаются. */
data class BackgroundPreset(
    val key: String,
    val label: String,
    val top: Color,
    val bottom: Color,
    val glowA: Color,
    val glowB: Color
)

const val BACKGROUND_THEME = "theme"
const val BACKGROUND_PHOTO = "photo"
const val BACKGROUND_PLAIN = "plain"

val BACKGROUND_PRESETS = listOf(
    BackgroundPreset("dawn", "Рассвет", Color(0xFFFFF1EC), Color(0xFFFFD6E0), Color(0xFFFFB199), Color(0xFFF7A1C4)),
    BackgroundPreset("lavender", "Лаванда", Color(0xFFF5F0FF), Color(0xFFE2D7FB), Color(0xFFC7B4F5), Color(0xFFF2C4E6)),
    BackgroundPreset("sea", "Море", Color(0xFFEFFAFB), Color(0xFFD3ECF7), Color(0xFF9FDCE0), Color(0xFFA9C8F2)),
    BackgroundPreset("mint", "Мята", Color(0xFFF2FBF5), Color(0xFFDDF2E5), Color(0xFFA8E2C1), Color(0xFFD4EFA6)),
    BackgroundPreset("sunset", "Закат", Color(0xFFFFF4E6), Color(0xFFF9D9E9), Color(0xFFFFC27A), Color(0xFFD9A6F0)),
    BackgroundPreset("peony", "Пион", Color(0xFFFFF0F5), Color(0xFFF8D2E4), Color(0xFFF59AC0), Color(0xFFFFC9B5))
)

/** Есть ли под контентом картинка — карточки тогда делаются чуть прозрачнее. */
val LocalHasPhotoBackground = staticCompositionLocalOf { false }

@Composable
fun BackgroundBox(
    style: String,
    photoUri: String?,
    photoDim: Int,
    photoBlur: Boolean,
    content: @Composable () -> Unit
) {
    val usePhoto = style == BACKGROUND_PHOTO && photoUri != null
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            usePhoto -> PhotoBackground(photoUri!!, photoDim, photoBlur)
            style == BACKGROUND_PLAIN -> Unit
            else -> GradientBackground(backgroundPresetFor(style))
        }
        CompositionLocalProvider(LocalHasPhotoBackground provides usePhoto) {
            content()
        }
    }
}

@Composable
fun backgroundPresetFor(style: String): BackgroundPreset {
    BACKGROUND_PRESETS.firstOrNull { it.key == style }?.let { return it }
    val c = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    // Для тёмной темы пресеты всё равно пройдут через затемнение, поэтому берём светлые основы.
    return if (dark) {
        BackgroundPreset(BACKGROUND_THEME, "Тема", c.background, c.background, c.primary, c.tertiary)
    } else {
        BackgroundPreset(
            BACKGROUND_THEME, "Тема",
            c.background, lerp(c.background, c.primaryContainer, 0.7f),
            c.primaryContainer, c.tertiaryContainer
        )
    }
}

@Composable
fun GradientBackground(preset: BackgroundPreset, modifier: Modifier = Modifier.fillMaxSize()) {
    val dark = isSystemInDarkTheme()
    val night = MaterialTheme.colorScheme.background
    fun adapt(color: Color, amount: Float) = if (dark) lerp(color, night, amount) else color
    val top = adapt(preset.top, 0.92f)
    val bottom = adapt(preset.bottom, 0.82f)
    val glowA = adapt(preset.glowA, 0.6f).copy(alpha = if (dark) 0.35f else 0.55f)
    val glowB = adapt(preset.glowB, 0.6f).copy(alpha = if (dark) 0.3f else 0.5f)
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(top, bottom)))
        val r = size.maxDimension * 0.55f
        val a = Offset(size.width * 0.95f, size.height * 0.08f)
        drawCircle(Brush.radialGradient(listOf(glowA, Color.Transparent), center = a, radius = r), r, a)
        val b = Offset(size.width * 0.05f, size.height * 0.78f)
        drawCircle(Brush.radialGradient(listOf(glowB, Color.Transparent), center = b, radius = r), r, b)
    }
}

@Composable
private fun PhotoBackground(uri: String, dimPercent: Int, blur: Boolean) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) { decodeScaled(context, Uri.parse(uri), maxSide = 1600) }
    }
    val image = bitmap ?: return
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .then(if (blur && Build.VERSION.SDK_INT >= 31) Modifier.blur(18.dp) else Modifier),
        contentScale = ContentScale.Crop
    )
    // Светлая вуаль в светлой теме и тёмная в тёмной — текст остаётся читаемым, а фото не превращается в серое пятно.
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = dimPercent.coerceIn(0, 90) / 100f))
    )
}

/** Декодирует фото с уменьшением: полноразмерный снимок с камеры может не поместиться в память. */
fun decodeScaled(context: Context, uri: Uri, maxSide: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    if (longest <= 0) return@runCatching null
    var sample = 1
    while (longest / (sample * 2) >= maxSide) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
    }
}.getOrNull()
