package com.example.cycletracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cycletracker.ui.LocalHasPhotoBackground

private val ButtonHeight = 52.dp
private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/** Карточка в стиле приложения: мягкий цвет поверхности, тонкая обводка, полупрозрачность поверх фото. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val alpha = if (LocalHasPhotoBackground.current) 0.86f else 0.94f
    val color = (containerColor ?: scheme.surfaceContainerLow).let { it.copy(alpha = it.alpha * alpha) }
    val colors = CardDefaults.cardColors(containerColor = color, contentColor = scheme.onSurface)
    val border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
    val shape = MaterialTheme.shapes.large
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    ) {
        ButtonIcon(icon)
        content()
    }
}

@Composable
fun AppTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding
    ) {
        ButtonIcon(icon)
        content()
    }
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    danger: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (danger) scheme.error else scheme.primary
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = ButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonPadding,
        border = BorderStroke(1.dp, accent.copy(alpha = if (enabled) 0.6f else 0.2f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = scheme.surface.copy(alpha = 0.6f),
            contentColor = accent
        )
    ) {
        ButtonIcon(icon)
        content()
    }
}

@Composable
private fun ButtonIcon(icon: ImageVector?) {
    if (icon == null) return
    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(8.dp))
}

/** Иконка в цветном кружке — для строк настроек и заголовков карточек. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}
