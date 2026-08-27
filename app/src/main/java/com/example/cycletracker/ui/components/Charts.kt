package com.example.cycletracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.cycletracker.logic.hormoneLevels

@Composable
fun CycleBarChart(values: List<Int>, modifier: Modifier = Modifier) {
    if (values.isEmpty()) return
    val max = (values.maxOrNull() ?: 1).toFloat()
    val barColor = MaterialTheme.colorScheme.primary
    Row(modifier = modifier.height(150.dp)) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((value / max).coerceIn(0.04f, 1f))
                            .clip(MaterialTheme.shapes.small)
                            .background(barColor)
                    )
                }
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HormoneCurveChart(modifier: Modifier = Modifier) {
    val estrogenColor = MaterialTheme.colorScheme.primary
    val progesteroneColor = MaterialTheme.colorScheme.tertiary
    val lhColor = MaterialTheme.colorScheme.error
    val fshColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val pad = 14.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        fun point(x: Int, y: Int): Offset =
            Offset(pad + (x + 14) / 28f * w, pad + (100 - y) / 100f * h)

        listOf(0, 50, 100).forEach { level ->
            drawLine(
                color = gridColor,
                start = Offset(pad, point(0, level).y),
                end = Offset(pad + w, point(0, level).y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val series = listOf(
            "Эстроген" to estrogenColor,
            "Прогестерон" to progesteroneColor,
            "ЛГ (лютеинизирующий гормон)" to lhColor,
            "ФСГ (фолликулостимулирующий)" to fshColor
        )
        series.forEach { (name, color) ->
            val path = Path()
            for (t in -14..14) {
                val value = hormoneLevels(t)[name] ?: 0
                val p = point(t, value)
                if (t == -14) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, color, style = Stroke(width = 2.5.dp.toPx()))
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            "Эстроген" to estrogenColor,
            "Прогестерон" to progesteroneColor,
            "ЛГ" to lhColor,
            "ФСГ" to fshColor
        ).forEach { (name, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(4.dp))
                Text(name, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = "День 0 — день овуляции, от -14 до +14",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
