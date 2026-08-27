package com.example.cycletracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.DayPhase
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.logic.phaseOn
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonth(
    month: YearMonth,
    info: CycleInfo,
    logs: Map<LocalDate, DailyLog> = emptyMap(),
    pill: PillSchedule? = null,
    onDateClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        val firstDay = month.atDay(1)
        val offset = firstDay.dayOfWeek.value - 1
        val cells = mutableListOf<LocalDate?>()
        repeat(offset) { cells.add(null) }
        for (d in 1..month.lengthOfMonth()) cells.add(month.atDay(d))
        while (cells.size % 7 != 0) cells.add(null)
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                info = info,
                                log = logs[date],
                                pill = pill,
                                onClick = { onDateClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    info: CycleInfo,
    log: DailyLog?,
    pill: PillSchedule?,
    onClick: () -> Unit
) {
    val phase = phaseOn(date, info)
    val isToday = date == info.today
    val colors = MaterialTheme.colorScheme

    val (background, content) = when (phase) {
        DayPhase.PERIOD -> colors.primary to colors.onPrimary
        DayPhase.OVULATION -> colors.tertiary to colors.onTertiary
        DayPhase.FERTILE -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> Color.Transparent to colors.onSurface
    }

    val marks = buildString {
        if (log?.sex == true) append("❤️")
        if (pill?.isActiveDay(date) == true) append("💊")
        if (log != null && !log.isEmpty) append("📝")
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .then(
                when {
                    phase == DayPhase.PREDICTED_PERIOD ->
                        Modifier.border(2.dp, colors.primary, CircleShape)
                    isToday ->
                        Modifier.border(1.dp, colors.outline, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (phase == DayPhase.PREDICTED_PERIOD) colors.primary else content
            )
            if (marks.isNotEmpty()) {
                Text(
                    text = marks,
                    fontSize = 8.sp,
                    color = content,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LegendItem(fill = colors.primary, border = null, label = "Месячные")
            LegendItem(fill = null, border = colors.primary, label = "Ожидаемые")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LegendItem(fill = colors.tertiary, border = null, label = "Овуляция")
            LegendItem(fill = colors.tertiaryContainer, border = null, label = "Фертильные дни")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LegendItem(fill = null, border = null, label = "❤️ интим")
            LegendItem(fill = null, border = null, label = "💊 таблетка")
            LegendItem(fill = null, border = null, label = "📝 дневник")
        }
    }
}

@Composable
private fun LegendItem(fill: Color?, border: Color?, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .then(if (fill != null) Modifier.background(fill) else Modifier)
                .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
