package com.example.cycletracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.ui.components.CalendarLegend
import com.example.cycletracker.ui.components.CalendarMonth
import com.example.cycletracker.util.monthTitle
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    info: CycleInfo,
    logs: Map<LocalDate, DailyLog>,
    settings: SettingsState,
    onLogPeriod: (LocalDate) -> Unit,
    onRemovePeriod: (LocalDate) -> Unit,
    onToggleSex: (LocalDate) -> Unit,
    onSaveLog: (LocalDate, DailyLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var shownMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var diaryDate by remember { mutableStateOf<LocalDate?>(null) }
    val pill = if (settings.pillEnabled) PillSchedule(settings) else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { shownMonth = shownMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Предыдущий месяц")
            }
            Text(shownMonth.monthTitle(), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { shownMonth = shownMonth.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Следующий месяц")
            }
        }
        Card {
            Column(Modifier.padding(16.dp)) {
                CalendarMonth(
                    month = shownMonth,
                    info = info,
                    logs = logs,
                    pill = pill,
                    onDateClick = { selectedDate = it }
                )
                Spacer(Modifier.height(12.dp))
                CalendarLegend()
            }
        }
        Text(
            text = "Нажмите на дату, чтобы отметить месячные, добавить ❤️ или открыть дневник.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    selectedDate?.let { date ->
        DayDetailDialog(
            date = date,
            info = info,
            log = logs[date] ?: DailyLog(),
            pill = pill,
            onMarkPeriod = onLogPeriod,
            onRemovePeriod = onRemovePeriod,
            onToggleSex = onToggleSex,
            onOpenDiary = { diaryDate = date },
            onDismiss = { selectedDate = null }
        )
    }

    diaryDate?.let { date ->
        DiaryEditorDialog(
            date = date,
            log = logs[date] ?: DailyLog(),
            onSave = { log ->
                onSaveLog(date, log)
                diaryDate = null
            },
            onDismiss = { diaryDate = null }
        )
    }
}
