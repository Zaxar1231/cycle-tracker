package com.example.cycletracker

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.data.DailyLog
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.CycleCalculator
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.logic.isIrregular
import com.example.cycletracker.notify.Notifications
import com.example.cycletracker.notify.Reminders
import com.example.cycletracker.update.ApkCheck
import com.example.cycletracker.update.ReleaseInfo
import com.example.cycletracker.update.Updater
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class Available(val release: ReleaseInfo) : UpdateStatus
    data class Downloading(val release: ReleaseInfo, val progress: Float) : UpdateStatus
    data class ReadyToInstall(val release: ReleaseInfo, val file: File) : UpdateStatus
    data class Failed(val message: String, val release: ReleaseInfo? = null) : UpdateStatus
}

data class UpdateUiState(
    val status: UpdateStatus = UpdateStatus.Idle,
    val showDialog: Boolean = false
)

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

    private val _update = MutableStateFlow(UpdateUiState())
    val update: StateFlow<UpdateUiState> = _update.asStateFlow()
    private var updateJob: Job? = null

    init {
        autoCheckForUpdates()
    }

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

    private fun autoCheckForUpdates() {
        if (!prefs.loadUpdateAutoCheck()) return
        val now = System.currentTimeMillis()
        if (now - prefs.loadUpdateLastCheck() < AUTO_CHECK_INTERVAL_MS) return
        checkForUpdates(manual = false)
    }

    /** manual = true — пользователь нажал «Проверить»: показываем результат, даже если версия пропущена. */
    fun checkForUpdates(manual: Boolean = true) {
        if (updateJob?.isActive == true) return
        updateJob = viewModelScope.launch {
            if (manual) _update.value = UpdateUiState(UpdateStatus.Checking)
            val result = runCatching { Updater.fetchLatest() }
            prefs.saveUpdateLastCheck(System.currentTimeMillis())
            val release = result.getOrNull()
            _update.value = when {
                result.isFailure -> if (manual) {
                    UpdateUiState(UpdateStatus.Failed(errorText(result.exceptionOrNull())))
                } else {
                    UpdateUiState()
                }
                release != null && Updater.isNewer(release.version, Updater.currentVersion) -> {
                    val skipped = !manual && prefs.loadUpdateSkippedVersion() == release.version
                    UpdateUiState(UpdateStatus.Available(release), showDialog = !skipped)
                }
                manual -> UpdateUiState(UpdateStatus.UpToDate)
                else -> UpdateUiState()
            }
        }
    }

    fun downloadUpdate(release: ReleaseInfo) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            _update.update { it.copy(status = UpdateStatus.Downloading(release, 0f), showDialog = true) }
            try {
                val file = Updater.download(context, release) { progress ->
                    _update.update { state ->
                        if (state.status is UpdateStatus.Downloading) {
                            state.copy(status = UpdateStatus.Downloading(release, progress))
                        } else {
                            state
                        }
                    }
                }
                val check = Updater.verify(context, file)
                if (check != ApkCheck.OK) file.delete()
                val status = when (check) {
                    ApkCheck.OK -> UpdateStatus.ReadyToInstall(release, file)
                    ApkCheck.NOT_NEWER -> UpdateStatus.Failed("Скачанная версия не новее установленной.", release)
                    ApkCheck.SIGNATURE_MISMATCH -> UpdateStatus.Failed(
                        "Файл подписан другим ключом, поэтому Android не установит его поверх текущей версии. " +
                            "Установите вручную со страницы релиза (потребуется удалить приложение — сначала сделайте экспорт данных).",
                        release
                    )
                    ApkCheck.INVALID -> UpdateStatus.Failed("Скачанный файл повреждён или не является обновлением приложения.", release)
                }
                _update.update { it.copy(status = status) }
                if (status is UpdateStatus.ReadyToInstall) installUpdate()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _update.update { it.copy(status = UpdateStatus.Failed(errorText(e), release)) }
            }
        }
    }

    fun cancelDownload() {
        val status = _update.value.status
        updateJob?.cancel()
        _update.value = if (status is UpdateStatus.Downloading) {
            UpdateUiState(UpdateStatus.Available(status.release))
        } else {
            UpdateUiState()
        }
    }

    /** Открывает системный установщик; если нет разрешения на установку — экран этого разрешения. */
    fun installUpdate() {
        val status = _update.value.status as? UpdateStatus.ReadyToInstall ?: return
        if (!status.file.exists()) {
            _update.update { it.copy(status = UpdateStatus.Available(status.release)) }
            return
        }
        runCatching {
            if (Updater.canInstall(context)) {
                Updater.install(context, status.file)
            } else {
                Updater.openInstallPermissionSettings(context)
            }
        }.onFailure { e ->
            _update.update { it.copy(status = UpdateStatus.Failed(errorText(e), status.release)) }
        }
    }

    fun canInstallUpdates(): Boolean = Updater.canInstall(context)

    fun openReleasePage(release: ReleaseInfo) = Updater.openReleasePage(context, release)

    fun showUpdateDialog() {
        _update.update { it.copy(showDialog = true) }
    }

    fun dismissUpdateDialog() {
        _update.update { it.copy(showDialog = false) }
    }

    fun skipUpdate(release: ReleaseInfo) {
        prefs.saveUpdateSkippedVersion(release.version)
        _update.update { it.copy(showDialog = false) }
    }

    fun setUpdateAutoCheck(enabled: Boolean) {
        prefs.saveUpdateAutoCheck(enabled)
        refreshSettings()
    }

    private fun errorText(e: Throwable?): String = when (e) {
        is java.net.UnknownHostException, is java.net.ConnectException ->
            "Нет подключения к интернету."
        is java.net.SocketTimeoutException -> "Сервер не отвечает, попробуйте позже."
        else -> e?.message?.takeIf { it.isNotBlank() } ?: "Не удалось проверить обновления."
    }

    fun resetAll() {
        prefs.clearAll()
        Reminders.cancelAll(context)
        refreshSettings()
        refreshCycle()
        refreshLogs()
    }

    companion object {
        private const val AUTO_CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L
    }
}
