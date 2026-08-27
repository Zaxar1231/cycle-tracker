package com.example.cycletracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.logic.PregnancyInfo
import com.example.cycletracker.logic.currentPhaseLabel
import com.example.cycletracker.logic.delayDays
import com.example.cycletracker.logic.hormoneLevels
import com.example.cycletracker.logic.isIrregular
import com.example.cycletracker.logic.pregnancyProbability
import com.example.cycletracker.logic.tipFor
import com.example.cycletracker.ui.components.CalendarMonth
import com.example.cycletracker.util.daysUntilText
import com.example.cycletracker.util.formatFull
import com.example.cycletracker.util.formatShort
import com.example.cycletracker.util.monthTitle
import com.example.cycletracker.util.pluralDays
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    info: CycleInfo,
    logs: Map<LocalDate, DailyLog>,
    settings: SettingsState,
    onLogToday: () -> Unit,
    onSaveLog: (DailyLog) -> Unit,
    onToggleSex: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLog = logs[info.today] ?: DailyLog()
    val pill = if (settings.pillEnabled) PillSchedule(settings) else null
    val pregnancy = if (settings.isPregnant) settings.lmpDate?.let { PregnancyInfo(it, info.today) } else null
    var showDiary by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        if (pregnancy != null) {
            PregnancyCard(pregnancy)
        } else if (info.periods.isEmpty()) {
            EmptyStateCard(onLogToday = onLogToday)
        } else {
            CurrentCycleCard(info)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(
                    title = "Следующие месячные",
                    value = info.nextPeriodStart?.formatShort() ?: "—",
                    subtitle = info.nextPeriodStart?.let { daysUntilText(info.today, it) } ?: "",
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    title = "Овуляция",
                    value = info.ovulationDate?.formatShort() ?: "—",
                    subtitle = info.ovulationDate?.let { daysUntilText(info.today, it) } ?: "",
                    modifier = Modifier.weight(1f)
                )
            }
            if (info.fertileStart != null && info.fertileEnd != null) {
                InfoCard(
                    title = "Фертильное окно",
                    value = "${info.fertileStart.formatShort()} – ${info.fertileEnd.formatShort()}",
                    subtitle = "Повышенная вероятность беременности"
                )
            }
        }

        if (pregnancy == null && info.periods.isNotEmpty()) {
            ProbabilityCard(info)
        }

        DiaryCard(
            todayLog = todayLog,
            date = info.today,
            onOpenDiary = { showDiary = true },
            onToggleSex = onToggleSex
        )

        if (pill != null) {
            PillCard(pill, settings)
        }

        info.ovulationDate?.let { ovu ->
            HormonesCard(ChronoUnit.DAYS.between(ovu, info.today).toInt())
        }

        TipCard(info)

        if (isIrregular(info)) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "⚠️ Ваш цикл нерегулярный: разница между последними циклами больше 10 дней. Если это повторяется — проконсультируйтесь с гинекологом.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = YearMonth.of(info.today.year, info.today.month).monthTitle(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                CalendarMonth(
                    month = YearMonth.of(info.today.year, info.today.month),
                    info = info,
                    logs = logs,
                    pill = pill
                )
            }
        }
        Spacer(Modifier.height(72.dp))
    }

    if (showDiary) {
        DiaryEditorDialog(
            date = info.today,
            log = todayLog,
            onSave = {
                onSaveLog(it)
                showDiary = false
            },
            onDismiss = { showDiary = false }
        )
    }
}

@Composable
private fun PregnancyCard(p: PregnancyInfo) {
    Card {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "Беременность 🤰",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Неделя ${p.weeks + 1}, день ${p.daysIntoWeek + 1}",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text("${p.trimester} триместр", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Предполагаемая дата родов: ${p.dueDate.formatFull()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Осталось ${p.daysLeft} ${pluralDays(p.daysLeft.toLong())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProbabilityCard(info: CycleInfo) {
    val p = pregnancyProbability(info.today, info)
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Вероятность зачатия сегодня",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "≈ $p%",
                style = MaterialTheme.typography.headlineSmall,
                color = if (p >= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = when {
                    p >= 10 -> "Высокая вероятность — используйте контрацепцию, если не планируете беременность."
                    p >= 5 -> "Средняя вероятность зачатия."
                    else -> "Низкая вероятность зачатия."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiaryCard(
    todayLog: DailyLog,
    date: LocalDate,
    onOpenDiary: () -> Unit,
    onToggleSex: () -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дневник • ${date.formatShort()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (todayLog.sex) "❤️" else "🤍",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable(onClick = onToggleSex)
                            .padding(8.dp)
                    )
                    TextButton(onClick = onOpenDiary) { Text("Заполнить") }
                }
            }
            val summary = logSummary(todayLog)
            if (summary.isNotEmpty()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Отметьте настроение, симптомы, температуру и выделения — так прогноз станет точнее.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun logSummary(log: DailyLog): String {
    val parts = mutableListOf<String>()
    if (log.mood.isNotEmpty()) parts.add("Настроение: ${log.mood}")
    if (log.symptoms.isNotEmpty()) parts.add(log.symptoms.joinToString(" "))
    if (log.discharge.isNotEmpty()) parts.add("Выделения: ${log.discharge}")
    log.temperature?.let { parts.add("Температура: ${it}°C") }
    if (log.customSymptoms.isNotEmpty()) parts.add(log.customSymptoms)
    if (log.note.isNotEmpty()) parts.add(log.note)
    return parts.joinToString(" • ")
}

@Composable
private fun PillCard(pill: PillSchedule, settings: SettingsState) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("💊 Контрацептивы", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(pill.statusOn(LocalDate.now()), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Время приёма: ${pill.timeText()} • ${settings.pillMealRule}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HormonesCard(offsetFromOvulation: Int) {
    val levels = hormoneLevels(offsetFromOvulation)
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("🧪 Гормоны сегодня", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            listOf("Эстроген", "Прогестерон", "ЛГ (лютеинизирующий гормон)", "ФСГ (фолликулостимулирующий)").forEach { name ->
                val value = levels[name] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
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
                                .fillMaxHeight()
                                .fillMaxWidth(value / 100f)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$value%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun TipCard(info: CycleInfo) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "💡 Совет дня",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = tipFor(info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CurrentCycleCard(info: CycleInfo) {
    val phase = currentPhaseLabel(info)
    val delay = delayDays(info)
    Card {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "День цикла",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = info.currentCycleDay?.toString() ?: "—",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (phase != null) {
                Spacer(Modifier.height(8.dp))
                Text(phase.first, style = MaterialTheme.typography.titleMedium)
                Text(
                    phase.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (delay != null && delay > 0 && info.nextPeriodStart != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Месячные ожидались ${info.nextPeriodStart.formatShort()} — задержка $delay ${pluralDays(delay.toLong())}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onLogToday: () -> Unit) {
    Card {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Добро пожаловать!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Отметьте первый день месячных, и приложение рассчитает ваш цикл: следующие месячные, овуляцию и фертильное окно.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogToday) {
                Text("Отметить сегодня")
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
