package com.example.cycletracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.example.cycletracker.ui.BACKGROUND_PHOTO
import com.example.cycletracker.ui.BACKGROUND_PLAIN
import com.example.cycletracker.ui.BACKGROUND_PRESETS
import com.example.cycletracker.ui.BACKGROUND_THEME
import com.example.cycletracker.ui.GradientBackground
import com.example.cycletracker.ui.backgroundPresetFor
import com.example.cycletracker.ui.decodeScaled
import com.example.cycletracker.ui.components.AppTonalButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cycletracker.CycleViewModel
import com.example.cycletracker.UpdateStatus
import com.example.cycletracker.update.Updater
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.logic.PregnancyInfo
import com.example.cycletracker.ui.theme.THEME_OPTIONS
import com.example.cycletracker.ui.theme.schemePreviewColor
import com.example.cycletracker.util.formatFull
import com.example.cycletracker.util.formatShort
import com.example.cycletracker.util.shareFile
import com.example.cycletracker.util.shareText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt
import com.example.cycletracker.ui.components.AppCard
import com.example.cycletracker.ui.components.IconBadge
import com.example.cycletracker.ui.components.AppButton
import com.example.cycletracker.ui.components.AppOutlinedButton

@Composable
fun SettingsScreen(vm: CycleViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsState()
    val cycle by vm.cycle.collectAsState()
    var page by rememberSaveable { mutableStateOf("main") }
    BackHandler(enabled = page != "main") { page = "main" }
    when (page) {
        "appearance" -> AppearancePage(settings, vm, onBack = { page = "main" })
        "profile" -> ProfilePage(settings, vm, onBack = { page = "main" })
        "pregnancy" -> PregnancyPage(settings, vm, onBack = { page = "main" })
        "pill" -> PillPage(settings, vm, onBack = { page = "main" })
        "reminders" -> RemindersPage(settings, vm, onBack = { page = "main" })
        "security" -> SecurityPage(settings, vm, onBack = { page = "main" })
        "updates" -> UpdatesPage(settings, vm, onBack = { page = "main" })
        else -> MainSettingsPage(settings, cycle, vm, onNavigate = { page = it })
    }
}

@Composable
private fun MainSettingsPage(
    settings: SettingsState,
    cycle: CycleInfo,
    vm: CycleViewModel,
    onNavigate: (String) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var cycleLen by remember(settings) { mutableFloatStateOf(cycle.avgCycleLength.coerceIn(15, 45).toFloat()) }
    var periodLen by remember(settings) { mutableFloatStateOf(cycle.settingsPeriodLength.toFloat()) }
    var luteal by remember(settings) { mutableFloatStateOf(cycle.settingsLutealLength.toFloat()) }
    var fertile by remember(settings) { mutableFloatStateOf(cycle.settingsFertileDays.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Параметры цикла", style = MaterialTheme.typography.titleMedium)
                SettingSlider(
                    title = "Длина цикла",
                    description = "Сколько дней проходит от первых суток одних месячных до первых суток следующих. Обычно 21–35 дней.",
                    value = cycleLen,
                    valueRange = 20f..45f,
                    steps = 24,
                    suffix = "дн.",
                    onValueChange = { cycleLen = it },
                    onCommit = vm::setCycleLength
                )
                SettingSlider(
                    title = "Длительность месячных",
                    description = "Сколько дней длятся месячные. Обычно 3–7 дней.",
                    value = periodLen,
                    valueRange = 2f..10f,
                    steps = 7,
                    suffix = "дн.",
                    onValueChange = { periodLen = it },
                    onCommit = vm::setPeriodLength
                )
                SettingSlider(
                    title = "Фаза после овуляции",
                    description = "Дни от овуляции до следующих месячных. Обычно 14 дней.",
                    value = luteal,
                    valueRange = 10f..16f,
                    steps = 5,
                    suffix = "дн.",
                    onValueChange = { luteal = it },
                    onCommit = vm::setLutealLength
                )
                SettingSlider(
                    title = "Фертильные дни",
                    description = "Сколько дней до овуляции считается фертильным окном.",
                    value = fertile,
                    valueRange = 3f..8f,
                    steps = 4,
                    suffix = "дн.",
                    onValueChange = { fertile = it },
                    onCommit = vm::setFertileDays
                )
            }
        }

        AppCard {
            Column(Modifier.padding(vertical = 4.dp, horizontal = 12.dp)) {
                SettingsRow(Icons.Filled.Palette, "Внешний вид", "Тема, фон, иконка приложения") { onNavigate("appearance") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.Person, "Профиль и данные", "Имя, вес, рост, экспорт и отчёт") { onNavigate("profile") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.ChildFriendly, "Режим беременности", "Расчёт срока и даты родов") { onNavigate("pregnancy") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.Medication, "Контрацептивы (таблетки)", "График приёма и напоминания") { onNavigate("pill") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.Notifications, "Напоминания", "Вода, здоровье, фертильность") { onNavigate("reminders") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.Lock, "Безопасность", "PIN-код для входа") { onNavigate("security") }
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.SystemUpdate, "Обновления", "Версия ${Updater.currentVersion}, проверка новых версий") { onNavigate("updates") }
            }
        }

        Text("Данные", style = MaterialTheme.typography.titleMedium)
        AppOutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.DeleteForever,
            danger = true
        ) {
            Text("Удалить все данные")
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Удалить все данные?") },
            text = { Text("Все отмеченные месячные, дневник и настройки будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetAll()
                        showResetDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PageHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Назад")
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearancePage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            vm.setBackgroundPhoto(uri.toString())
        }
    }
    val launchPicker = {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    var dim by remember(settings.backgroundDim) { mutableFloatStateOf(settings.backgroundDim.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader("Внешний вид", onBack)

        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Цветовая тема", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 3
                ) {
                    THEME_OPTIONS.forEach { (key, label) ->
                        ThemeSwatch(
                            color = schemePreviewColor(key),
                            label = label,
                            selected = settings.themeScheme == key,
                            onClick = { vm.setThemeScheme(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Фон приложения", style = MaterialTheme.typography.titleMedium)
                val tiles = buildList {
                    add(BACKGROUND_THEME to "Под тему")
                    BACKGROUND_PRESETS.forEach { add(it.key to it.label) }
                    add(BACKGROUND_PLAIN to "Без фона")
                    add(BACKGROUND_PHOTO to "Своё фото")
                }
                tiles.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (key, label) ->
                            BackgroundTile(
                                key = key,
                                label = label,
                                photoUri = settings.backgroundUri,
                                selected = settings.backgroundStyle == key,
                                onClick = {
                                    if (key == BACKGROUND_PHOTO && settings.backgroundUri == null) launchPicker()
                                    else vm.setBackgroundStyle(key)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                if (settings.backgroundStyle == BACKGROUND_PHOTO && settings.backgroundUri != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSlider(
                        title = "Осветление фото",
                        description = "Чем больше, тем спокойнее фон и легче читать текст.",
                        value = dim,
                        valueRange = 0f..80f,
                        steps = 15,
                        suffix = "%",
                        onValueChange = { dim = it },
                        onCommit = vm::setBackgroundDim
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Размыть фото", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (Build.VERSION.SDK_INT >= 31) "Мягкий фон без отвлекающих деталей"
                                else "Доступно на Android 12 и новее",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.backgroundBlur,
                            onCheckedChange = vm::setBackgroundBlur,
                            enabled = Build.VERSION.SDK_INT >= 31
                        )
                    }
                    AppTonalButton(
                        onClick = launchPicker,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Image
                    ) {
                        Text("Выбрать другое фото")
                    }
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Иконка приложения", style = MaterialTheme.typography.titleMedium)
                val iconVariants = listOf(
                    "rose" to "Розовая",
                    "lavender" to "Лавандовая",
                    "teal" to "Бирюзовая",
                    "peach" to "Персиковая"
                )
                val iconColors = mapOf(
                    "rose" to Color(0xFFF48FB1),
                    "lavender" to Color(0xFFB39DDB),
                    "teal" to Color(0xFF80CBC4),
                    "peach" to Color(0xFFFFAB91)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconVariants.forEach { (key, label) ->
                        val selected = settings.iconVariant == key
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { vm.setIconVariant(key) }
                                .padding(6.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(iconColors[key] ?: Color.Gray)
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💧", fontSize = 24.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "Android не разрешает ставить своё фото как иконку. Ярлык на рабочем столе обновится в течение минуты.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ThemeSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .border(
                    width = 3.dp,
                    color = if (selected) color else Color.Transparent,
                    shape = CircleShape
                )
                .padding(5.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(lerp(color, Color.White, 0.35f), color))),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackgroundTile(
    key: String,
    label: String,
    photoUri: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(18.dp)
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(shape)
                .background(scheme.background)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) scheme.primary else scheme.outlineVariant,
                    shape = shape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when (key) {
                BACKGROUND_PLAIN -> Unit
                BACKGROUND_PHOTO -> {
                    val thumb by produceState<ImageBitmap?>(null, photoUri) {
                        value = photoUri?.let { uri ->
                            withContext(Dispatchers.IO) { decodeScaled(context, Uri.parse(uri), maxSide = 360) }
                        }
                    }
                    val image = thumb
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.AddPhotoAlternate,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                else -> GradientBackground(backgroundPresetFor(key), Modifier.matchParentSize())
            }
            // Мини-«карточки», чтобы было видно, как фон смотрится под содержимым.
            if (key != BACKGROUND_PHOTO || photoUri != null) {
                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(scheme.surfaceContainerLow.copy(alpha = 0.9f))
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(scheme.surfaceContainerLow.copy(alpha = 0.9f))
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var name by remember(settings.profileName) { mutableStateOf(settings.profileName) }
    var weightText by remember(settings.weightKg) { mutableStateOf(settings.weightKg?.toString() ?: "") }
    var heightText by remember(settings.heightCm) { mutableStateOf(settings.heightCm?.toString() ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var birthDate by remember(settings.birthDate) { mutableStateOf(settings.birthDate) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            }.getOrDefault("")
            vm.importJson(text)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageHeader("Профиль и данные", onBack)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        AppButton(onClick = { showDatePicker = true }) {
            Text("Дата рождения: ${birthDate?.formatFull() ?: "не указана"}")
        }
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Вес, кг") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("Рост, см") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        AppButton(
            onClick = {
                vm.updateProfile(
                    name = name.trim(),
                    birthDate = birthDate,
                    weightKg = runCatching { weightText.replace(',', '.').toDouble() }.getOrNull(),
                    heightCm = runCatching { heightText.toInt() }.getOrNull()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить профиль")
        }

        HorizontalDivider()
        Text("Данные и отчёты", style = MaterialTheme.typography.titleMedium)
        AppButton(
            onClick = { shareText(context, vm.exportJson()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Экспорт данных (файл JSON)")
        }
        AppOutlinedButton(
            onClick = { importLauncher.launch(arrayOf("text/*", "application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импорт из файла")
        }
        AppButton(
            onClick = { shareFile(context, vm.writeReportFile()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Скачать отчёт о здоровье")
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate?.let {
                it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            birthDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PregnancyPage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var lmp by remember(settings.lmpDate) { mutableStateOf(settings.lmpDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageHeader("Режим беременности", onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Режим беременности", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Расчёт срока от первого дня последних месячных",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = settings.isPregnant,
                onCheckedChange = { on ->
                    if (on) {
                        vm.setPregnancy(true, lmp ?: settings.lmpDate ?: LocalDate.now())
                    } else {
                        vm.setPregnancy(false, null)
                    }
                }
            )
        }

        if (settings.isPregnant) {
            AppButton(onClick = { showDatePicker = true }) {
                Text("Первый день последних месячных: ${lmp?.formatFull() ?: "не указан"}")
            }
            lmp?.let {
                val p = PregnancyInfo(it, LocalDate.now())
                AppCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Неделя ${p.weeks + 1}, день ${p.daysIntoWeek + 1}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("${p.trimester} триместр", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Предполагаемая дата родов: ${p.dueDate.formatFull()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Осталось ${p.daysLeft} дней",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lmp?.let {
                it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            lmp = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            vm.setPregnancy(true, lmp)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PillPage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    var enabled by remember(settings.pillEnabled) { mutableStateOf(settings.pillEnabled) }
    var startDate by remember(settings.pillStartDate) { mutableStateOf(settings.pillStartDate) }
    var activeDays by remember(settings.pillActiveDays) { mutableFloatStateOf(settings.pillActiveDays.toFloat()) }
    var breakDays by remember(settings.pillBreakDays) { mutableFloatStateOf(settings.pillBreakDays.toFloat()) }
    var hour by remember(settings.pillHour) { mutableFloatStateOf(settings.pillHour.toFloat()) }
    var minute by remember(settings.pillMinute) { mutableFloatStateOf(settings.pillMinute.toFloat()) }
    var mealRule by remember(settings.pillMealRule) { mutableStateOf(settings.pillMealRule) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader("Контрацептивы (таблетки)", onBack)

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text("💊 Правила приёма", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Укажите дату первой таблетки упаковки и время приёма. Приложение посчитает активные дни и перерыв, отметит таблетки в календаре и будет напоминать о приёме.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Напоминать о приёме", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = { on ->
                    enabled = on
                    vm.setPill(on, startDate, activeDays.roundToInt(), breakDays.roundToInt(), hour.roundToInt(), minute.roundToInt(), mealRule)
                }
            )
        }

        AppButton(onClick = { showDatePicker = true }) {
            Text("День первой таблетки: ${startDate?.formatShort() ?: "не указан"}")
        }

        SettingSlider(
            title = "Активных таблеток в упаковке",
            description = "Обычно 21 или 28.",
            value = activeDays,
            valueRange = 14f..28f,
            steps = 13,
            suffix = "шт.",
            onValueChange = { activeDays = it },
            onCommit = {}
        )
        SettingSlider(
            title = "Перерыв между упаковками",
            description = "Дней без таблеток. Обычно 7.",
            value = breakDays,
            valueRange = 0f..10f,
            steps = 9,
            suffix = "дн.",
            onValueChange = { breakDays = it },
            onCommit = {}
        )
        SettingSlider(
            title = "Время приёма: час",
            description = "Время суток, когда вы принимаете таблетку.",
            value = hour,
            valueRange = 0f..23f,
            steps = 22,
            suffix = "ч",
            onValueChange = { hour = it },
            onCommit = {}
        )
        SettingSlider(
            title = "Время приёма: минуты",
            description = "",
            value = minute,
            valueRange = 0f..59f,
            steps = 58,
            suffix = "мин",
            onValueChange = { minute = it },
            onCommit = {}
        )

        Text("Приём таблетки", style = MaterialTheme.typography.titleMedium)
        FlowRowChips(
            options = listOf("независимо от еды", "до еды", "после еды"),
            selected = mealRule,
            onSelect = { mealRule = it }
        )

        if (enabled && startDate != null) {
            val schedule = PillSchedule(
                SettingsState(
                    pillEnabled = enabled,
                    pillStartDate = startDate,
                    pillActiveDays = activeDays.roundToInt(),
                    pillBreakDays = breakDays.roundToInt(),
                    pillHour = hour.roundToInt(),
                    pillMinute = minute.roundToInt(),
                    pillMealRule = mealRule
                )
            )
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Сегодня", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(schedule.statusOn(LocalDate.now()), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        AppButton(
            onClick = {
                vm.setPill(enabled, startDate, activeDays.roundToInt(), breakDays.roundToInt(), hour.roundToInt(), minute.roundToInt(), mealRule)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.let {
                it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(option, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun RemindersPage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    var waterEnabled by remember(settings.waterEnabled) { mutableStateOf(settings.waterEnabled) }
    var waterStart by remember(settings.waterStartHour) { mutableFloatStateOf(settings.waterStartHour.toFloat()) }
    var waterEnd by remember(settings.waterEndHour) { mutableFloatStateOf(settings.waterEndHour.toFloat()) }
    var waterInterval by remember(settings.waterIntervalHours) { mutableFloatStateOf(settings.waterIntervalHours.toFloat()) }

    var healthEnabled by remember(settings.healthReminderEnabled) { mutableStateOf(settings.healthReminderEnabled) }
    var healthHour by remember(settings.healthHour) { mutableFloatStateOf(settings.healthHour.toFloat()) }
    var healthMinute by remember(settings.healthMinute) { mutableFloatStateOf(settings.healthMinute.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader("Напоминания", onBack)

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💧 Напоминания о воде", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Switch(checked = waterEnabled, onCheckedChange = { waterEnabled = it })
                }
                SettingSlider("Начало", "", waterStart, 6f..12f, 5, "ч", { waterStart = it }, {})
                SettingSlider("Конец", "", waterEnd, 13f..23f, 9, "ч", { waterEnd = it }, {})
                SettingSlider("Каждые N часов", "", waterInterval, 1f..4f, 2, "ч", { waterInterval = it }, {})
                AppButton(
                    onClick = {
                        vm.setWaterReminder(waterEnabled, waterStart.roundToInt(), waterEnd.roundToInt(), waterInterval.roundToInt())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить напоминание о воде")
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌸 Проверка здоровья", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Switch(checked = healthEnabled, onCheckedChange = { healthEnabled = it })
                }
                Text(
                    text = "Ежедневное напоминание записать самочувствие в дневник.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SettingSlider("Час", "", healthHour, 0f..23f, 22, "ч", { healthHour = it }, {})
                SettingSlider("Минуты", "", healthMinute, 0f..59f, 58, "мин", { healthMinute = it }, {})
                AppButton(
                    onClick = { vm.setHealthReminder(healthEnabled, healthHour.roundToInt(), healthMinute.roundToInt()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить напоминание о здоровье")
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("⚠️ Высокая вероятность зачатия", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Ежедневная проверка в 9:00 — уведомление о контрацепции в дни высокого шанса.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.fertilityNotifyEnabled,
                        onCheckedChange = { vm.setFertilityNotify(it) }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SecurityPage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    var showPinSetup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageHeader("Безопасность", onBack)

        Text(
            text = if (settings.pinEnabled) "PIN-код включён. Приложение будет спрашивать его при каждом запуске."
            else "PIN-код выключен.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AppButton(
            onClick = { showPinSetup = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (settings.pinEnabled) "Изменить PIN" else "Установить PIN")
        }
        if (settings.pinEnabled) {
            AppOutlinedButton(
                onClick = { vm.disablePin() },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.LockOpen,
                danger = true
            ) {
                Text("Отключить PIN")
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showPinSetup) {
        PinSetupDialog(
            onDone = {
                vm.setPin(it)
                showPinSetup = false
            },
            onDismiss = { showPinSetup = false }
        )
    }
}

@Composable
private fun UpdatesPage(settings: SettingsState, vm: CycleViewModel, onBack: () -> Unit) {
    val update by vm.update.collectAsState()
    val status = update.status

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageHeader("Обновления", onBack)

        AppCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Установлена версия ${Updater.currentVersion}", style = MaterialTheme.typography.titleMedium)
                val statusText = when (status) {
                    UpdateStatus.Idle -> "Новые версии берутся из релизов на GitHub."
                    UpdateStatus.Checking -> "Проверяем…"
                    UpdateStatus.UpToDate -> "У вас последняя версия ✅"
                    is UpdateStatus.Available -> "Доступна версия ${status.release.version} 🎉"
                    is UpdateStatus.Downloading -> "Загрузка версии ${status.release.version}: ${(status.progress * 100).toInt()}%"
                    is UpdateStatus.ReadyToInstall -> "Версия ${status.release.version} скачана и готова к установке"
                    is UpdateStatus.Failed -> status.message
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status is UpdateStatus.Failed) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status is UpdateStatus.Downloading) {
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                when (status) {
                    is UpdateStatus.Available -> AppButton(
                        onClick = { vm.downloadUpdate(status.release) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Скачать и установить") }

                    is UpdateStatus.Downloading -> AppOutlinedButton(
                        onClick = vm::cancelDownload,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Отменить загрузку") }

                    is UpdateStatus.ReadyToInstall -> AppButton(
                        onClick = vm::installUpdate,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Установить") }

                    else -> AppButton(
                        onClick = { vm.checkForUpdates(manual = true) },
                        enabled = status != UpdateStatus.Checking,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Проверить обновления") }
                }
                val release = when (status) {
                    is UpdateStatus.Available -> status.release
                    is UpdateStatus.Failed -> status.release
                    else -> null
                }
                if (release != null) {
                    if (status is UpdateStatus.Available) {
                        AppOutlinedButton(
                            onClick = vm::showUpdateDialog,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Что нового") }
                    }
                    TextButton(
                        onClick = { vm.openReleasePage(release) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Открыть страницу релиза") }
                }
            }
        }

        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Проверять автоматически", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "При запуске приложения, не чаще раза в 6 часов.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.updateAutoCheck,
                    onCheckedChange = vm::setUpdateAutoCheck
                )
            }
        }

        Text(
            text = "Обновление скачивается с GitHub и устанавливается системным установщиком Android. " +
                "При первой установке Android попросит разрешить «Установку неизвестных приложений» для «Мой цикл». " +
                "Данные приложения при обновлении сохраняются.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PinSetupDialog(onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 0) "Придумайте PIN (4 цифры)" else "Повторите PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = if (step == 0) first else second,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(4)
                        if (step == 0) {
                            first = digits
                        } else {
                            second = digits
                        }
                        error = false
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Spacer(Modifier.height(4.dp))
                    Text("PIN не совпадают", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        step == 0 && first.length == 4 -> step = 1
                        step == 1 && second == first -> onDone(first)
                        step == 1 -> error = true
                    }
                }
            ) {
                Text(if (step == 0) "Далее" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun SettingSlider(
    title: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    suffix: String,
    onValueChange: (Float) -> Unit,
    onCommit: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                text = "${value.roundToInt()} $suffix",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = { onCommit(value.roundToInt()) }
        )
    }
}
