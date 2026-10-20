package com.example.cycletracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cycletracker.CycleViewModel
import com.example.cycletracker.util.formatFull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt
import com.example.cycletracker.ui.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(vm: CycleViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var cycle by remember { mutableFloatStateOf(28f) }
    var period by remember { mutableFloatStateOf(5f) }
    var luteal by remember { mutableFloatStateOf(14f) }
    var fertile by remember { mutableFloatStateOf(5f) }
    var showDatePicker by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            }.getOrDefault("")
            importMessage = if (vm.importJson(text)) "Данные импортированы!" else "Не удалось прочитать файл"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🌸", fontSize = 48.sp)
        Text(
            text = "Добро пожаловать в «Мой цикл»!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Заполните данные один раз — приложение рассчитает цикл, овуляцию и фертильные дни.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

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

        Text(
            text = "Параметры цикла",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        OnboardingSlider("Длина цикла", cycle, 20f..45f, 24, "дн.") { cycle = it }
        OnboardingSlider("Длительность месячных", period, 2f..10f, 7, "дн.") { period = it }
        OnboardingSlider("Лютеиновая фаза", luteal, 10f..16f, 5, "дн.") { luteal = it }
        OnboardingSlider("Фертильные дни (до овуляции)", fertile, 3f..8f, 4, "дн.") { fertile = it }

        TextButton(
            onClick = { importLauncher.launch(arrayOf("text/*", "application/json")) }
        ) {
            Text("Импортировать данные из файла")
        }
        importMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.startsWith("Данные")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            if (message.startsWith("Данные")) {
                TextButton(onClick = { vm.finishOnboarding() }) {
                    Text("Продолжить с импортированными данными")
                }
            }
        }

        AppButton(
            onClick = {
                vm.completeOnboarding(
                    name = name.trim(),
                    birthDate = birthDate,
                    weightKg = runCatching { weightText.replace(',', '.').toDouble() }.getOrNull(),
                    heightCm = runCatching { heightText.toInt() }.getOrNull(),
                    cycleLength = cycle.roundToInt(),
                    periodLength = period.roundToInt(),
                    lutealLength = luteal.roundToInt(),
                    fertileDays = fertile.roundToInt()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать")
        }
        Spacer(Modifier.height(24.dp))
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

@Composable
private fun OnboardingSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    suffix: String,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "$title: ${value.roundToInt()} $suffix",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}
