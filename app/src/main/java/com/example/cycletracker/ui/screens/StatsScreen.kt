package com.example.cycletracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.isIrregular
import com.example.cycletracker.ui.components.CycleBarChart
import com.example.cycletracker.ui.components.HormoneCurveChart
import com.example.cycletracker.util.formatShort
import java.time.LocalDate
import com.example.cycletracker.ui.components.AppCard

@Composable
fun StatsScreen(
    info: CycleInfo,
    logs: Map<LocalDate, DailyLog>,
    settings: SettingsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (info.periods.isEmpty() && logs.isEmpty()) {
            AppCard {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет данных", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Отметьте начало месячных и ведите дневник — здесь появится статистика.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (info.periods.isNotEmpty()) {
                val usedCycles = minOf(6, info.cycleLengths.size)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Средний цикл",
                        value = "${info.avgCycleLength} дн.",
                        note = if (info.cycleLengths.size < 2) "по настройкам" else "по $usedCycles циклам",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Месячные",
                        value = "${info.settingsPeriodLength} дн.",
                        note = "настройка",
                        modifier = Modifier.weight(1f)
                    )
                }
                StatCard(
                    title = "Фаза после овуляции (лютеиновая)",
                    value = "${info.settingsLutealLength} дн.",
                    note = "используется для расчёта даты овуляции"
                )
            }

            if (isIrregular(info)) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "⚠️ Нерегулярный цикл: разница между последними циклами больше 10 дней. Покажите эту статистику гинекологу.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (info.cycleLengths.isNotEmpty()) {
                ChartCard(title = "Длина циклов (дней)") {
                    CycleBarChart(values = info.cycleLengths)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Номера циклов по порядку",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ChartCard(title = "Уровни гормонов в течение цикла") {
                HormoneCurveChart()
            }

            SymptomStatsCard(logs)

            if (info.periods.isNotEmpty()) {
                HistoryCard(info)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    AppCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SymptomStatsCard(logs: Map<LocalDate, DailyLog>) {
    val counters = mutableMapOf<String, Int>()
    logs.values.forEach { log ->
        if (log.mood.isNotEmpty()) counters[log.mood] = (counters[log.mood] ?: 0) + 1
        log.symptoms.forEach { counters[it] = (counters[it] ?: 0) + 1 }
        if (log.discharge.isNotEmpty()) counters[log.discharge] = (counters[log.discharge] ?: 0) + 1
    }
    val sexDays = logs.values.count { it.sex }
    if (counters.isEmpty() && sexDays == 0) return

    AppCard {
        Column(Modifier.padding(16.dp)) {
            Text("Как часто отмечались симптомы", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val max = (counters.values.maxOrNull() ?: 1).coerceAtLeast(1)
            val nameFor = fun(emoji: String): String {
                return (MOODS + SYMPTOMS + DISCHARGES).firstOrNull { it.first == emoji }?.second
                    ?: emoji
            }
            counters.entries.sortedByDescending { it.value }.forEach { (emoji, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$emoji ${nameFor(emoji)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(150.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(count / max.toFloat())
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
            if (sexDays > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "❤️ Половой акт отмечался $sexDays ${plural(sexDays)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun plural(n: Int): String {
    val n10 = n % 10
    val n100 = n % 100
    return when {
        n100 in 11..14 -> "раз"
        n10 == 1 -> "раз"
        n10 in 2..4 -> "раза"
        else -> "раз"
    }
}

@Composable
private fun StatCard(title: String, value: String, note: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoryCard(info: CycleInfo) {
    AppCard {
        Column(Modifier.padding(16.dp)) {
            Text("История циклов", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            info.periods.indices.reversed().forEach { i ->
                val start = info.periods[i]
                val end = start.plusDays((info.settingsPeriodLength - 1).toLong())
                val length = if (i < info.periods.lastIndex) info.cycleLengths[i] else info.avgCycleLength
                val predicted = i == info.periods.lastIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${start.formatShort()} – ${end.formatShort()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Цикл №${i + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$length дн." + if (predicted) " *" else "",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (i > 0) HorizontalDivider()
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "* последний цикл — прогноз по средней длине",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
