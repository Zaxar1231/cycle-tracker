package com.example.cycletracker.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class DailyLog(
    val mood: String = "",
    val symptoms: List<String> = emptyList(),
    val discharge: String = "",
    val temperature: Double? = null,
    val customSymptoms: String = "",
    val note: String = "",
    val sex: Boolean = false
) {
    val isEmpty: Boolean
        get() = mood.isEmpty() && symptoms.isEmpty() && discharge.isEmpty() &&
            temperature == null && customSymptoms.isEmpty() && note.isEmpty() && !sex

    fun toJson(): JSONObject = JSONObject().apply {
        put("mood", mood)
        put("symptoms", JSONArray(symptoms))
        put("discharge", discharge)
        temperature?.let { put("temperature", it) }
        put("customSymptoms", customSymptoms)
        put("note", note)
        put("sex", sex)
    }

    companion object {
        fun fromJson(jo: JSONObject): DailyLog = DailyLog(
            mood = jo.optString("mood"),
            symptoms = jo.optJSONArray("symptoms")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList(),
            discharge = jo.optString("discharge"),
            temperature = if (jo.has("temperature")) jo.getDouble("temperature") else null,
            customSymptoms = jo.optString("customSymptoms"),
            note = jo.optString("note"),
            sex = jo.optBoolean("sex")
        )
    }
}

data class SettingsState(
    val themeScheme: String = "rose",
    val iconVariant: String = "rose",
    val backgroundUri: String? = null,
    val pinEnabled: Boolean = false,
    val fertileDays: Int = 5,
    val profileName: String = "",
    val birthDate: LocalDate? = null,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val isPregnant: Boolean = false,
    val lmpDate: LocalDate? = null,
    val pillEnabled: Boolean = false,
    val pillStartDate: LocalDate? = null,
    val pillActiveDays: Int = 21,
    val pillBreakDays: Int = 7,
    val pillHour: Int = 20,
    val pillMinute: Int = 0,
    val pillMealRule: String = "независимо от еды",
    val waterEnabled: Boolean = false,
    val waterStartHour: Int = 8,
    val waterEndHour: Int = 22,
    val waterIntervalHours: Int = 2,
    val healthReminderEnabled: Boolean = false,
    val healthHour: Int = 9,
    val healthMinute: Int = 0,
    val fertilityNotifyEnabled: Boolean = true,
    val onboardingDone: Boolean = false,
    val pet: String = "cat",
    val updateAutoCheck: Boolean = true
)
