package com.example.cycletracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cycletracker.CycleViewModel
import com.example.cycletracker.ui.components.UpdateDialog
import com.example.cycletracker.ui.screens.AssistantScreen
import com.example.cycletracker.ui.screens.CalendarScreen
import com.example.cycletracker.ui.screens.HomeScreen
import com.example.cycletracker.ui.screens.SettingsScreen
import com.example.cycletracker.ui.screens.StatsScreen
import com.example.cycletracker.util.formatShort
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CycleViewModel) {
    val info by viewModel.cycle.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val update by viewModel.update.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("Главная", Icons.Filled.Home),
        TabItem("Календарь", Icons.Filled.CalendarMonth),
        TabItem("Статистика", Icons.Filled.Insights),
        TabItem("Помощник", Icons.Filled.Pets),
        TabItem("Настройки", Icons.Filled.Settings)
    )

    fun logPeriod(date: LocalDate, showUndo: Boolean) {
        viewModel.addPeriod(date)
        if (showUndo) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Начало месячных: ${date.formatShort()}",
                    actionLabel = "Отменить",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.removePeriod(date)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tabs[selectedTab].title, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 0 && !info.periods.contains(info.today) && !settings.isPregnant) {
                ExtendedFloatingActionButton(
                    onClick = { logPeriod(info.today, showUndo = true) },
                    icon = { Icon(Icons.Filled.WaterDrop, contentDescription = null) },
                    text = { Text("Месячные начались") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    info = info,
                    logs = logs,
                    settings = settings,
                    onLogToday = { logPeriod(info.today, showUndo = true) },
                    onSaveLog = { viewModel.saveLog(info.today, it) },
                    onToggleSex = { viewModel.toggleSex(info.today) }
                )
                1 -> CalendarScreen(
                    info = info,
                    logs = logs,
                    settings = settings,
                    onLogPeriod = { logPeriod(it, showUndo = true) },
                    onRemovePeriod = viewModel::removePeriod,
                    onToggleSex = viewModel::toggleSex,
                    onSaveLog = viewModel::saveLog
                )
                2 -> StatsScreen(
                    info = info,
                    logs = logs,
                    settings = settings
                )
                3 -> AssistantScreen(
                    settings = settings,
                    onSetPet = viewModel::setPet
                )
                4 -> SettingsScreen(vm = viewModel)
            }
        }
    }

    if (update.showDialog) {
        UpdateDialog(
            status = update.status,
            onDownload = viewModel::downloadUpdate,
            onInstall = viewModel::installUpdate,
            onCancelDownload = viewModel::cancelDownload,
            onSkip = viewModel::skipUpdate,
            onOpenPage = viewModel::openReleasePage,
            onDismiss = viewModel::dismissUpdateDialog
        )
    }
}
