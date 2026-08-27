package com.example.cycletracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.logic.pregnancyProbability
import com.example.cycletracker.util.formatFull
import java.time.LocalDate

val MOODS = listOf(
    "😊" to "Радостное",
    "😐" to "Обычное",
    "😢" to "Грустное",
    "😠" to "Раздражённое",
    "😴" to "Уставшее",
    "😍" to "Влюблённое"
)

val SYMPTOMS = listOf(
    "🤕" to "Головная боль",
    "😣" to "Спазмы",
    "🤢" to "Тошнота",
    "😴" to "Усталость",
    "🍫" to "Тяга к сладкому",
    "😤" to "Раздражительность",
    "🥶" to "Озноб",
    "🤧" to "Насморк"
)

val DISCHARGES = listOf(
    "🫥" to "Нет",
    "💧" to "Водянистые",
    "🧴" to "Кремообразные",
    "🥚" to "Как яичный белок",
    "⚪" to "Густые"
)

@Composable
fun EmojiChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surfaceVariant)
            .border(1.dp, if (selected) colors.primary else colors.outline.copy(alpha = 0.4f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji)
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaryEditorDialog(
    date: LocalDate,
    log: DailyLog,
    onSave: (DailyLog) -> Unit,
    onDismiss: () -> Unit
) {
    var mood by remember(date, log) { mutableStateOf(log.mood) }
    var symptoms by remember(date, log) { mutableStateOf(log.symptoms) }
    var discharge by remember(date, log) { mutableStateOf(log.discharge) }
    var temperature by remember(date, log) { mutableStateOf(log.temperature?.toString() ?: "") }
    var customSymptoms by remember(date, log) { mutableStateOf(log.customSymptoms) }
    var note by remember(date, log) { mutableStateOf(log.note) }
    var sex by remember(date, log) { mutableStateOf(log.sex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Дневник — ${date.formatFull()}") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Настроение",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MOODS.forEach { (emoji, label) ->
                        EmojiChip(emoji, label, mood == emoji) {
                            mood = if (mood == emoji) "" else emoji
                        }
                    }
                }

                Text(
                    "Симптомы",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SYMPTOMS.forEach { (emoji, label) ->
                        EmojiChip(emoji, label, emoji in symptoms) {
                            symptoms = if (emoji in symptoms) symptoms - emoji else symptoms + emoji
                        }
                    }
                }

                Text(
                    "Выделения",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DISCHARGES.forEach { (emoji, label) ->
                        EmojiChip(emoji, label, discharge == emoji) {
                            discharge = if (discharge == emoji) "" else emoji
                        }
                    }
                }

                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Температура, °C") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customSymptoms,
                    onValueChange = { customSymptoms = it },
                    label = { Text("Свои симптомы") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { sex = !sex }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (sex) "❤️" else "🤍", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Половой акт в этот день", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        DailyLog(
                            mood = mood,
                            symptoms = symptoms,
                            discharge = discharge,
                            temperature = runCatching { temperature.replace(',', '.').toDouble() }.getOrNull(),
                            customSymptoms = customSymptoms,
                            note = note,
                            sex = sex
                        )
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun DayDetailDialog(
    date: LocalDate,
    info: CycleInfo,
    log: DailyLog,
    pill: PillSchedule?,
    onMarkPeriod: (LocalDate) -> Unit,
    onRemovePeriod: (LocalDate) -> Unit,
    onToggleSex: (LocalDate) -> Unit,
    onOpenDiary: () -> Unit,
    onDismiss: () -> Unit
) {
    val isMarked = info.periods.contains(date)
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.formatFull()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (log.sex) Text("❤️ Половой акт", style = MaterialTheme.typography.bodyMedium)
                if (!log.isEmpty) Text("📝 Есть запись в дневнике", style = MaterialTheme.typography.bodyMedium)
                if (pill?.isActiveDay(date) == true) Text("💊 День приёма таблетки", style = MaterialTheme.typography.bodyMedium)
                if (pill?.isBreakDay(date) == true) Text("⏸ Перерыв в приёме таблеток", style = MaterialTheme.typography.bodyMedium)
                if (isMarked) {
                    Text(
                        "🩸 Начало месячных",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary
                    )
                } else if (info.periods.isNotEmpty()) {
                    val p = pregnancyProbability(date, info)
                    if (p > 0) {
                        Text(
                            "Вероятность зачатия в этот день: около $p%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (p >= 10) colors.error else colors.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(onClick = onOpenDiary, modifier = Modifier.fillMaxWidth()) {
                    Text("Открыть дневник")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isMarked) onRemovePeriod(date) else onMarkPeriod(date)
                    onDismiss()
                }
            ) {
                Text(if (isMarked) "Убрать отметку" else "Начало месячных")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onToggleSex(date)
                    onDismiss()
                }
            ) {
                Text(if (log.sex) "Убрать ❤️" else "Добавить ❤️")
            }
        }
    )
}
