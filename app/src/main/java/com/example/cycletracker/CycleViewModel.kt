package com.example.cycletracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleCalculator
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.isIrregular
import com.example.cycletracker.notify.Notifications
import com.example.cycletracker.notify.Reminders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

class CycleViewModel(application: Application) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()
    private val prefs = CyclePreferences(context)
    private val calculator = CycleCalculator()

    private val _cycle = MutableStateFlow(loadCycle())
    val cycle: StateFlow<CycleInfo> = _cycle.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _logs = MutableStateFlow(prefs.loadAllLogs())
    val logs: StateFlow<Map<LocalDate, DailyLog>> = _logs.asStateFlow()

    private fun loadCycle(): CycleInfo = calculator.compute(
        periods = prefs.loadPeriodStarts(),
        defaultCycle = prefs.loadCycleLength(),
        periodLength = prefs.loadPeriodLength(),
        lutealLength = prefs.loadLutealLength(),
        fertileDays = prefs.loadFertileDays(),
        today = LocalDate.now()
    )

    private fun loadSettings(): SettingsState = prefs.loadSettings()

    private fun refreshCycle() {
        _cycle.value = loadCycle()
        maybeNotifyIrregularity()
        Reminders.updateWidgets(context)
    }

    private fun refreshSettings() {
        _settings.value = loadSettings()
        Reminders.rescheduleAll(context, prefs)
    }

    private fun refreshLogs() {
        _logs.value = prefs.loadAllLogs()
        Reminders.updateWidgets(context)
    }

    private fun maybeNotifyIrregularity() {
        val info = _cycle.value
        if (!isIrregular(info)) return
        val month = YearMonth.from(info.today).toString()
        if (prefs.loadLastIrregularMonth() == month) return
        prefs.saveLastIrregularMonth(month)
        Notifications.postIrregular(context, info)
    }

    fun addPeriod(date: LocalDate) {
        prefs.savePeriodStarts((prefs.loadPeriodStarts() + date).distinct().sorted())
        refreshCycle()
    }

    fun removePeriod(date: LocalDate) {
        prefs.savePeriodStarts(prefs.loadPeriodStarts().filterNot { it == date })
        refreshCycle()
    }

    fun setCycleLength(value: Int) {
        prefs.saveCycleLength(value)
        refreshCycle()
    }

    fun setPeriodLength(value: Int) {
        prefs.savePeriodLength(value)
        refreshCycle()
    }

    fun setLutealLength(value: Int) {
        prefs.saveLutealLength(value)
        refreshCycle()
    }

    fun setFertileDays(value: Int) {
        prefs.saveFertileDays(value)
        refreshCycle()
    }

    fun saveLog(date: LocalDate, log: DailyLog) {
        prefs.saveLog(date, log)
        refreshLogs()
    }

    fun toggleSex(date: LocalDate) {
        val current = prefs.loadLog(date)
        prefs.saveLog(date, current.copy(sex = !current.sex))
        refreshLogs()
    }

    fun setThemeScheme(value: String) {
        prefs.saveThemeScheme(value)
        refreshSettings()
    }

    fun setBackgroundUri(uri: String?) {
        prefs.saveBackgroundUri(uri)
        refreshSettings()
    }

    fun setIconVariant(variant: String) {
        val pm = context.packageManager
        val aliases = mapOf(
            "rose" to "com.example.cycletracker.IconRose",
            "lavender" to "com.example.cycletracker.IconLavender",
            "teal" to "com.example.cycletracker.IconTeal",
            "peach" to "com.example.cycletracker.IconPeach"
        )
        aliases.forEach { (name, className) ->
            val state = if (name == variant) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                ComponentName(context, className),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
        prefs.saveIconVariant(variant)
    }

    fun setPin(pin: String) {
        prefs.savePin(pin)
        prefs.savePinEnabled(true)
        refreshSettings()
    }

    fun disablePin() {
        prefs.savePinEnabled(false)
        refreshSettings()
    }

    fun checkPin(pin: String): Boolean = prefs.checkPin(pin)

    fun completeOnboarding(
        name: String,
        birthDate: LocalDate?,
        weightKg: Double?,
        heightCm: Int?,
        cycleLength: Int,
        periodLength: Int,
        lutealLength: Int,
        fertileDays: Int
    ) {
        prefs.saveProfileName(name)
        prefs.saveBirthDate(birthDate)
        prefs.saveWeight(weightKg)
        prefs.saveHeight(heightCm)
        prefs.saveCycleLength(cycleLength)
        prefs.savePeriodLength(periodLength)
        prefs.saveLutealLength(lutealLength)
        prefs.saveFertileDays(fertileDays)
        prefs.saveOnboardingDone(true)
        refreshSettings()
        refreshCycle()
    }

    fun finishOnboarding() {
        prefs.saveOnboardingDone(true)
        refreshSettings()
        refreshCycle()
    }

    fun updateProfile(name: String, birthDate: LocalDate?, weightKg: Double?, heightCm: Int?) {
        prefs.saveProfileName(name)
        prefs.saveBirthDate(birthDate)
        prefs.saveWeight(weightKg)
        prefs.saveHeight(heightCm)
        refreshSettings()
    }

    fun setPregnancy(enabled: Boolean, lmp: LocalDate?) {
        prefs.savePregnant(enabled)
        prefs.saveLmpDate(lmp)
        refreshSettings()
        refreshCycle()
    }

    fun setPill(
        enabled: Boolean,
        startDate: LocalDate?,
        activeDays: Int,
        breakDays: Int,
        hour: Int,
        minute: Int,
        mealRule: String
    ) {
        prefs.savePillEnabled(enabled)
        prefs.savePillStartDate(startDate)
        prefs.savePillActiveDays(activeDays)
        prefs.savePillBreakDays(breakDays)
        prefs.savePillHour(hour)
        prefs.savePillMinute(minute)
        prefs.savePillMealRule(mealRule)
        refreshSettings()
    }

    fun setWaterReminder(enabled: Boolean, startHour: Int, endHour: Int, intervalHours: Int) {
        prefs.saveWaterEnabled(enabled)
        prefs.saveWaterStartHour(startHour)
        prefs.saveWaterEndHour(endHour)
        prefs.saveWaterIntervalHours(intervalHours)
        refreshSettings()
    }

    fun setHealthReminder(enabled: Boolean, hour: Int, minute: Int) {
        prefs.saveHealthReminderEnabled(enabled)
        prefs.saveHealthHour(hour)
        prefs.saveHealthMinute(minute)
        refreshSettings()
    }

    fun setFertilityNotify(enabled: Boolean) {
        prefs.saveFertilityNotify(enabled)
        refreshSettings()
    }

    fun setPet(value: String) {
        prefs.savePet(value)
        refreshSettings()
    }

    fun exportJson(): String = prefs.exportJson()

    fun importJson(text: String): Boolean {
        val ok = prefs.importJson(text)
        if (ok) {
            refreshSettings()
            refreshCycle()
            refreshLogs()
        }
        return ok
    }

    fun buildReport(): String {
        val info = _cycle.value
        val logs = _logs.value
        val settings = _settings.value
        val sb = StringBuilder()
        sb.appendLine("ОТЧЁТ О ЗДОРОВЬЕ — Мой цикл")
        sb.appendLine("Сформирован: ${LocalDate.now()}")
        sb.appendLine()
        sb.appendLine("=== ПРОФИЛЬ ===")
        if (settings.profileName.isNotEmpty()) sb.appendLine("Имя: ${settings.profileName}")
        settings.birthDate?.let {
            sb.appendLine("Дата рождения: $it (${LocalDate.now().year - it.year} лет)")
        }
        settings.weightKg?.let { sb.appendLine("Вес: $it кг") }
        settings.heightCm?.let { sb.appendLine("Рост: $it см") }
        sb.appendLine()
        sb.appendLine("=== ЦИКЛ ===")
        sb.appendLine("Средняя длина цикла: ${info.avgCycleLength} дней")
        sb.appendLine("Длительность месячных: ${info.settingsPeriodLength} дней")
        sb.appendLine("Лютеиновая фаза: ${info.settingsLutealLength} дней")
        sb.appendLine("Фертильное окно: ${info.settingsFertileDays} дней до овуляции + 1")
        sb.appendLine("Отмечено циклов: ${info.periods.size}")
        if (info.cycleLengths.isNotEmpty()) {
            sb.appendLine("Длины циклов: ${info.cycleLengths.joinToString(", ")}")
        }
        info.nextPeriodStart?.let { sb.appendLine("Прогноз следующих месячных: $it") }
        info.ovulationDate?.let { sb.appendLine("Прогноз овуляции: $it") }
        if (isIrregular(info)) sb.appendLine("Внимание: цикл нерегулярный (разница больше 10 дней).")
        sb.appendLine()
        if (settings.isPregnant) {
            settings.lmpDate?.let {
                val due = it.plusDays(280)
                sb.appendLine("=== БЕРЕМЕННОСТЬ ===")
                sb.appendLine("Первый день последних месячных: $it")
                sb.appendLine("Предполагаемая дата родов: $due")
            }
            sb.appendLine()
        }
        sb.appendLine("=== ДНЕВНИК ===")
        if (logs.isEmpty()) {
            sb.appendLine("Записей нет.")
        } else {
            logs.toSortedMap().forEach { (date, log) ->
                val parts = mutableListOf<String>()
                if (log.mood.isNotEmpty()) parts.add("настроение: ${log.mood}")
                if (log.symptoms.isNotEmpty()) parts.add("симптомы: ${log.symptoms.joinToString(", ")}")
                if (log.discharge.isNotEmpty()) parts.add("выделения: ${log.discharge}")
                log.temperature?.let { parts.add("температура: $it°C") }
                if (log.customSymptoms.isNotEmpty()) parts.add("своё: ${log.customSymptoms}")
                if (log.sex) parts.add("половой акт: да")
                if (log.note.isNotEmpty()) parts.add("заметка: ${log.note}")
                if (parts.isNotEmpty()) sb.appendLine("$date — ${parts.joinToString("; ")}")
            }
        }
        sb.appendLine()
        sb.appendLine("Отчёт сформирован приложением «Мой цикл». Не является медицинским документом.")
        return sb.toString()
    }

    fun writeReportFile(): File {
        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "health_report_${LocalDate.now()}.txt")
        file.writeText(buildReport())
        return file
    }

    fun resetAll() {
        prefs.clearAll()
        Reminders.cancelAll(context)
        refreshSettings()
        refreshCycle()
        refreshLogs()
    }
}
