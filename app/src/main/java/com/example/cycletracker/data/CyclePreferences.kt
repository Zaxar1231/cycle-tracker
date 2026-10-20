package com.example.cycletracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.LocalDate

class CyclePreferences(context: Context) {

    private val prefs = context.getSharedPreferences("cycle_data", Context.MODE_PRIVATE)

    fun loadPeriodStarts(): List<LocalDate> {
        val raw = prefs.getString(KEY_PERIODS, "") ?: ""
        return raw.split(",")
            .mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }
            .sorted()
    }

    fun savePeriodStarts(dates: List<LocalDate>) {
        prefs.edit()
            .putString(KEY_PERIODS, dates.map { it.toString() }.joinToString(","))
            .apply()
    }

    fun loadCycleLength(): Int = prefs.getInt(KEY_CYCLE, 28)

    fun saveCycleLength(value: Int) {
        prefs.edit().putInt(KEY_CYCLE, value).apply()
    }

    fun loadPeriodLength(): Int = prefs.getInt(KEY_PERIOD_LEN, 5)

    fun savePeriodLength(value: Int) {
        prefs.edit().putInt(KEY_PERIOD_LEN, value).apply()
    }

    fun loadLutealLength(): Int = prefs.getInt(KEY_LUTEAL, 14)

    fun saveLutealLength(value: Int) {
        prefs.edit().putInt(KEY_LUTEAL, value).apply()
    }

    fun loadFertileDays(): Int = prefs.getInt(KEY_FERTILE, 5)

    fun saveFertileDays(value: Int) {
        prefs.edit().putInt(KEY_FERTILE, value).apply()
    }

    fun loadThemeScheme(): String = prefs.getString(KEY_THEME, "rose") ?: "rose"

    fun saveThemeScheme(value: String) {
        prefs.edit().putString(KEY_THEME, value).apply()
    }

    fun loadIconVariant(): String = prefs.getString(KEY_ICON, "rose") ?: "rose"

    fun saveIconVariant(value: String) {
        prefs.edit().putString(KEY_ICON, value).apply()
    }

    fun loadBackgroundUri(): String? = prefs.getString(KEY_BG, null)

    fun saveBackgroundUri(uri: String?) {
        prefs.edit().putString(KEY_BG, uri).apply()
    }

    /** Раньше был только выбор фото: если фото уже выбрано, оставляем его. */
    fun loadBackgroundStyle(): String =
        prefs.getString(KEY_BG_STYLE, null) ?: if (loadBackgroundUri() != null) "photo" else "theme"

    fun saveBackgroundStyle(value: String) {
        prefs.edit().putString(KEY_BG_STYLE, value).apply()
    }

    fun loadBackgroundDim(): Int = prefs.getInt(KEY_BG_DIM, 35)

    fun saveBackgroundDim(value: Int) {
        prefs.edit().putInt(KEY_BG_DIM, value).apply()
    }

    fun loadBackgroundBlur(): Boolean = prefs.getBoolean(KEY_BG_BLUR, false)

    fun saveBackgroundBlur(value: Boolean) {
        prefs.edit().putBoolean(KEY_BG_BLUR, value).apply()
    }

    fun isPinEnabled(): Boolean = prefs.getBoolean(KEY_PIN_ON, false)

    fun savePinEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ON, value).apply()
    }

    fun loadPinHash(): String = prefs.getString(KEY_PIN, "") ?: ""

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, sha256(pin)).apply()
    }

    fun checkPin(pin: String): Boolean {
        val hash = prefs.getString(KEY_PIN, "") ?: ""
        return hash.isNotEmpty() && hash == sha256(pin)
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARD, false)

    fun saveOnboardingDone(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARD, value).apply()
    }

    fun loadProfileName(): String = prefs.getString(KEY_NAME, "") ?: ""

    fun saveProfileName(value: String) {
        prefs.edit().putString(KEY_NAME, value).apply()
    }

    fun loadBirthDate(): LocalDate? = prefs.getString(KEY_BIRTH, null)?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

    fun saveBirthDate(value: LocalDate?) {
        prefs.edit().putString(KEY_BIRTH, value?.toString()).apply()
    }

    fun loadWeight(): Double? = if (prefs.contains(KEY_WEIGHT)) prefs.getFloat(KEY_WEIGHT, 0f).toDouble() else null

    fun saveWeight(value: Double?) {
        prefs.edit().putFloat(KEY_WEIGHT, value?.toFloat() ?: 0f).apply()
    }

    fun loadHeight(): Int? = if (prefs.contains(KEY_HEIGHT)) prefs.getInt(KEY_HEIGHT, 0) else null

    fun saveHeight(value: Int?) {
        prefs.edit().putInt(KEY_HEIGHT, value ?: 0).apply()
    }

    fun isPregnant(): Boolean = prefs.getBoolean(KEY_PREGNANT, false)

    fun savePregnant(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREGNANT, value).apply()
    }

    fun loadLmpDate(): LocalDate? = prefs.getString(KEY_LMP, null)?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

    fun saveLmpDate(value: LocalDate?) {
        prefs.edit().putString(KEY_LMP, value?.toString()).apply()
    }

    fun loadPillEnabled(): Boolean = prefs.getBoolean(KEY_PILL_ON, false)

    fun savePillEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_PILL_ON, value).apply()
    }

    fun loadPillStartDate(): LocalDate? = prefs.getString(KEY_PILL_START, null)?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

    fun savePillStartDate(value: LocalDate?) {
        prefs.edit().putString(KEY_PILL_START, value?.toString()).apply()
    }

    fun loadPillActiveDays(): Int = prefs.getInt(KEY_PILL_ACTIVE, 21)

    fun savePillActiveDays(value: Int) {
        prefs.edit().putInt(KEY_PILL_ACTIVE, value).apply()
    }

    fun loadPillBreakDays(): Int = prefs.getInt(KEY_PILL_BREAK, 7)

    fun savePillBreakDays(value: Int) {
        prefs.edit().putInt(KEY_PILL_BREAK, value).apply()
    }

    fun loadPillHour(): Int = prefs.getInt(KEY_PILL_HOUR, 20)

    fun savePillHour(value: Int) {
        prefs.edit().putInt(KEY_PILL_HOUR, value).apply()
    }

    fun loadPillMinute(): Int = prefs.getInt(KEY_PILL_MINUTE, 0)

    fun savePillMinute(value: Int) {
        prefs.edit().putInt(KEY_PILL_MINUTE, value).apply()
    }

    fun loadPillMealRule(): String = prefs.getString(KEY_PILL_MEAL, "независимо от еды") ?: "независимо от еды"

    fun savePillMealRule(value: String) {
        prefs.edit().putString(KEY_PILL_MEAL, value).apply()
    }

    fun loadWaterEnabled(): Boolean = prefs.getBoolean(KEY_WATER_ON, false)

    fun saveWaterEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_WATER_ON, value).apply()
    }

    fun loadWaterStartHour(): Int = prefs.getInt(KEY_WATER_START, 8)

    fun saveWaterStartHour(value: Int) {
        prefs.edit().putInt(KEY_WATER_START, value).apply()
    }

    fun loadWaterEndHour(): Int = prefs.getInt(KEY_WATER_END, 22)

    fun saveWaterEndHour(value: Int) {
        prefs.edit().putInt(KEY_WATER_END, value).apply()
    }

    fun loadWaterIntervalHours(): Int = prefs.getInt(KEY_WATER_INTERVAL, 2)

    fun saveWaterIntervalHours(value: Int) {
        prefs.edit().putInt(KEY_WATER_INTERVAL, value).apply()
    }

    fun loadHealthReminderEnabled(): Boolean = prefs.getBoolean(KEY_HEALTH_ON, false)

    fun saveHealthReminderEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_HEALTH_ON, value).apply()
    }

    fun loadHealthHour(): Int = prefs.getInt(KEY_HEALTH_HOUR, 9)

    fun saveHealthHour(value: Int) {
        prefs.edit().putInt(KEY_HEALTH_HOUR, value).apply()
    }

    fun loadHealthMinute(): Int = prefs.getInt(KEY_HEALTH_MINUTE, 0)

    fun saveHealthMinute(value: Int) {
        prefs.edit().putInt(KEY_HEALTH_MINUTE, value).apply()
    }

    fun loadFertilityNotify(): Boolean = prefs.getBoolean(KEY_FERT_NOTIFY, true)

    fun saveFertilityNotify(value: Boolean) {
        prefs.edit().putBoolean(KEY_FERT_NOTIFY, value).apply()
    }

    fun loadPet(): String = prefs.getString(KEY_PET, "cat") ?: "cat"

    fun savePet(value: String) {
        prefs.edit().putString(KEY_PET, value).apply()
    }

    fun loadLastIrregularMonth(): String = prefs.getString(KEY_IRREGULAR_MONTH, "") ?: ""

    fun saveLastIrregularMonth(value: String) {
        prefs.edit().putString(KEY_IRREGULAR_MONTH, value).apply()
    }

    fun loadUpdateAutoCheck(): Boolean = prefs.getBoolean(KEY_UPDATE_AUTO, true)

    fun saveUpdateAutoCheck(value: Boolean) {
        prefs.edit().putBoolean(KEY_UPDATE_AUTO, value).apply()
    }

    fun loadUpdateSkippedVersion(): String = prefs.getString(KEY_UPDATE_SKIPPED, "") ?: ""

    fun saveUpdateSkippedVersion(value: String) {
        prefs.edit().putString(KEY_UPDATE_SKIPPED, value).apply()
    }

    fun loadUpdateLastCheck(): Long = prefs.getLong(KEY_UPDATE_LAST_CHECK, 0L)

    fun saveUpdateLastCheck(value: Long) {
        prefs.edit().putLong(KEY_UPDATE_LAST_CHECK, value).apply()
    }

    fun saveLog(date: LocalDate, log: DailyLog) {
        val all = loadAllLogs().toMutableMap()
        if (log.isEmpty) all.remove(date) else all[date] = log
        val jo = JSONObject()
        all.forEach { (d, l) -> jo.put(d.toString(), l.toJson()) }
        prefs.edit().putString(KEY_LOGS, jo.toString()).apply()
    }

    fun loadLog(date: LocalDate): DailyLog = loadAllLogs()[date] ?: DailyLog()

    fun loadAllLogs(): Map<LocalDate, DailyLog> {
        val raw = prefs.getString(KEY_LOGS, null) ?: return emptyMap()
        return runCatching {
            val jo = JSONObject(raw)
            val result = mutableMapOf<LocalDate, DailyLog>()
            jo.keys().forEach { key ->
                val date = LocalDate.parse(key)
                result[date] = DailyLog.fromJson(jo.getJSONObject(key))
            }
            result
        }.getOrDefault(emptyMap())
    }

    fun loadSettings(): SettingsState = SettingsState(
        themeScheme = loadThemeScheme(),
        iconVariant = loadIconVariant(),
        backgroundUri = loadBackgroundUri(),
        backgroundStyle = loadBackgroundStyle(),
        backgroundDim = loadBackgroundDim(),
        backgroundBlur = loadBackgroundBlur(),
        pinEnabled = isPinEnabled(),
        fertileDays = loadFertileDays(),
        profileName = loadProfileName(),
        birthDate = loadBirthDate(),
        weightKg = loadWeight(),
        heightCm = loadHeight(),
        isPregnant = isPregnant(),
        lmpDate = loadLmpDate(),
        pillEnabled = loadPillEnabled(),
        pillStartDate = loadPillStartDate(),
        pillActiveDays = loadPillActiveDays(),
        pillBreakDays = loadPillBreakDays(),
        pillHour = loadPillHour(),
        pillMinute = loadPillMinute(),
        pillMealRule = loadPillMealRule(),
        waterEnabled = loadWaterEnabled(),
        waterStartHour = loadWaterStartHour(),
        waterEndHour = loadWaterEndHour(),
        waterIntervalHours = loadWaterIntervalHours(),
        healthReminderEnabled = loadHealthReminderEnabled(),
        healthHour = loadHealthHour(),
        healthMinute = loadHealthMinute(),
        fertilityNotifyEnabled = loadFertilityNotify(),
        onboardingDone = isOnboardingDone(),
        pet = loadPet(),
        updateAutoCheck = loadUpdateAutoCheck()
    )

    fun exportJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        val profile = JSONObject().apply {
            put("name", loadProfileName())
            loadBirthDate()?.let { put("birthDate", it.toString()) }
            loadWeight()?.let { put("weightKg", it) }
            loadHeight()?.let { put("heightCm", it) }
        }
        root.put("profile", profile)
        val settings = JSONObject().apply {
            put("cycleLength", loadCycleLength())
            put("periodLength", loadPeriodLength())
            put("lutealLength", loadLutealLength())
            put("fertileDays", loadFertileDays())
            put("themeScheme", loadThemeScheme())
            put("isPregnant", isPregnant())
            loadLmpDate()?.let { put("lmpDate", it.toString()) }
            put("pillEnabled", loadPillEnabled())
            loadPillStartDate()?.let { put("pillStartDate", it.toString()) }
            put("pillActiveDays", loadPillActiveDays())
            put("pillBreakDays", loadPillBreakDays())
            put("pillHour", loadPillHour())
            put("pillMinute", loadPillMinute())
            put("pillMealRule", loadPillMealRule())
            put("pet", loadPet())
        }
        root.put("settings", settings)
        root.put("periods", JSONArray(loadPeriodStarts().map { it.toString() }))
        val logs = JSONObject()
        loadAllLogs().forEach { (d, l) -> logs.put(d.toString(), l.toJson()) }
        root.put("logs", logs)
        return root.toString(2)
    }

    fun importJson(text: String): Boolean {
        return runCatching {
            val root = JSONObject(text)
            root.optJSONObject("profile")?.let { p ->
                p.optString("name").takeIf { it.isNotEmpty() }?.let { saveProfileName(it) }
                p.optString("birthDate").takeIf { it.isNotEmpty() }?.let { saveBirthDate(LocalDate.parse(it)) }
                if (p.has("weightKg")) saveWeight(p.getDouble("weightKg"))
                if (p.has("heightCm")) saveHeight(p.getInt("heightCm"))
            }
            root.optJSONObject("settings")?.let { s ->
                if (s.has("cycleLength")) saveCycleLength(s.getInt("cycleLength"))
                if (s.has("periodLength")) savePeriodLength(s.getInt("periodLength"))
                if (s.has("lutealLength")) saveLutealLength(s.getInt("lutealLength"))
                if (s.has("fertileDays")) saveFertileDays(s.getInt("fertileDays"))
                if (s.has("themeScheme")) saveThemeScheme(s.getString("themeScheme"))
                if (s.has("isPregnant")) savePregnant(s.getBoolean("isPregnant"))
                s.optString("lmpDate").takeIf { it.isNotEmpty() }?.let { saveLmpDate(LocalDate.parse(it)) }
                if (s.has("pillEnabled")) savePillEnabled(s.getBoolean("pillEnabled"))
                s.optString("pillStartDate").takeIf { it.isNotEmpty() }?.let { savePillStartDate(LocalDate.parse(it)) }
                if (s.has("pillActiveDays")) savePillActiveDays(s.getInt("pillActiveDays"))
                if (s.has("pillBreakDays")) savePillBreakDays(s.getInt("pillBreakDays"))
                if (s.has("pillHour")) savePillHour(s.getInt("pillHour"))
                if (s.has("pillMinute")) savePillMinute(s.getInt("pillMinute"))
                if (s.has("pillMealRule")) savePillMealRule(s.getString("pillMealRule"))
                if (s.has("pet")) savePet(s.getString("pet"))
            }
            root.optJSONArray("periods")?.let { arr ->
                val dates = (0 until arr.length()).mapNotNull { runCatching { LocalDate.parse(arr.getString(it)) }.getOrNull() }
                savePeriodStarts(dates)
            }
            root.optJSONObject("logs")?.let { logs ->
                val parsed = mutableMapOf<LocalDate, DailyLog>()
                logs.keys().forEach { key ->
                    val date = LocalDate.parse(key)
                    parsed[date] = DailyLog.fromJson(logs.getJSONObject(key))
                }
                val jo = JSONObject()
                parsed.forEach { (d, l) -> jo.put(d.toString(), l.toJson()) }
                prefs.edit().putString(KEY_LOGS, jo.toString()).apply()
            }
            true
        }.getOrDefault(false)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_PERIODS = "period_start_dates"
        private const val KEY_CYCLE = "default_cycle_length"
        private const val KEY_PERIOD_LEN = "default_period_length"
        private const val KEY_LUTEAL = "luteal_length"
        private const val KEY_FERTILE = "fertile_days"
        private const val KEY_THEME = "theme_scheme"
        private const val KEY_ICON = "icon_variant"
        private const val KEY_BG = "background_uri"
        private const val KEY_BG_STYLE = "background_style"
        private const val KEY_BG_DIM = "background_dim"
        private const val KEY_BG_BLUR = "background_blur"
        private const val KEY_PIN_ON = "pin_enabled"
        private const val KEY_PIN = "pin_hash"
        private const val KEY_ONBOARD = "onboarding_done"
        private const val KEY_NAME = "profile_name"
        private const val KEY_BIRTH = "profile_birth"
        private const val KEY_WEIGHT = "profile_weight"
        private const val KEY_HEIGHT = "profile_height"
        private const val KEY_PREGNANT = "pregnant_mode"
        private const val KEY_LMP = "pregnancy_lmp"
        private const val KEY_PILL_ON = "pill_enabled"
        private const val KEY_PILL_START = "pill_start"
        private const val KEY_PILL_ACTIVE = "pill_active_days"
        private const val KEY_PILL_BREAK = "pill_break_days"
        private const val KEY_PILL_HOUR = "pill_hour"
        private const val KEY_PILL_MINUTE = "pill_minute"
        private const val KEY_PILL_MEAL = "pill_meal_rule"
        private const val KEY_WATER_ON = "water_enabled"
        private const val KEY_WATER_START = "water_start_hour"
        private const val KEY_WATER_END = "water_end_hour"
        private const val KEY_WATER_INTERVAL = "water_interval_hours"
        private const val KEY_HEALTH_ON = "health_enabled"
        private const val KEY_HEALTH_HOUR = "health_hour"
        private const val KEY_HEALTH_MINUTE = "health_minute"
        private const val KEY_FERT_NOTIFY = "fertility_notify"
        private const val KEY_PET = "assistant_pet"
        private const val KEY_IRREGULAR_MONTH = "irregular_notified_month"
        private const val KEY_LOGS = "daily_logs_json"
        private const val KEY_UPDATE_AUTO = "update_auto_check"
        private const val KEY_UPDATE_SKIPPED = "update_skipped_version"
        private const val KEY_UPDATE_LAST_CHECK = "update_last_check"

        fun sha256(input: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}
