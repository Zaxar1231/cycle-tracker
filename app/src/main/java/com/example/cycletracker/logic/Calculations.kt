package com.example.cycletracker.logic

import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.util.formatShort
import com.example.cycletracker.util.pluralDays
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PillSchedule(private val settings: SettingsState) {
    val packLength: Int
        get() = settings.pillActiveDays + settings.pillBreakDays

    fun dayNumberOn(date: LocalDate): Int? {
        val start = settings.pillStartDate ?: return null
        if (date.isBefore(start)) return null
        val d = ChronoUnit.DAYS.between(start, date).toInt()
        return d % packLength
    }

    fun isActiveDay(date: LocalDate): Boolean {
        val n = dayNumberOn(date) ?: return false
        return n < settings.pillActiveDays
    }

    fun isBreakDay(date: LocalDate): Boolean {
        val n = dayNumberOn(date) ?: return false
        return n >= settings.pillActiveDays
    }

    fun statusOn(date: LocalDate): String {
        if (settings.pillStartDate == null) return "не настроено"
        val n = dayNumberOn(date) ?: return "ещё не начат"
        return if (n < settings.pillActiveDays) {
            "активный день: таблетка №${n + 1} из ${settings.pillActiveDays}"
        } else {
            "перерыв: день ${n - settings.pillActiveDays + 1} из ${settings.pillBreakDays}"
        }
    }

    fun timeText(): String = String.format("%02d:%02d", settings.pillHour, settings.pillMinute)
}

data class PregnancyInfo(val lmp: LocalDate, val today: LocalDate) {
    val dueDate: LocalDate
        get() = lmp.plusDays(280)

    val totalDays: Int
        get() = ChronoUnit.DAYS.between(lmp, today).toInt().coerceIn(0, 290)

    val weeks: Int
        get() = totalDays / 7

    val daysIntoWeek: Int
        get() = totalDays % 7

    val daysLeft: Int
        get() = ChronoUnit.DAYS.between(today, dueDate).toInt().coerceAtLeast(0)

    val trimester: Int
        get() = when {
            weeks < 13 -> 1
            weeks < 28 -> 2
            else -> 3
        }
}

fun pregnancyProbability(date: LocalDate, info: CycleInfo): Int {
    if (info.periods.isEmpty() || info.ovulationDate == null) return 0
    if (phaseOn(date, info) == DayPhase.PERIOD) return 1
    val offset = ChronoUnit.DAYS.between(info.ovulationDate, date).toInt()
    return when (offset) {
        -5 -> 8
        -4 -> 12
        -3 -> 18
        -2 -> 27
        -1 -> 31
        0 -> 33
        1 -> 8
        2 -> 3
        in -10..-6 -> 2
        else -> 1
    }
}

fun hormoneLevels(offsetFromOvulation: Int): Map<String, Int> {
    val t = offsetFromOvulation.coerceIn(-14, 14)
    val estrogen = when {
        t < 0 -> (15 + (t + 14) * 6.5).toInt().coerceIn(15, 100)
        t == 0 -> 60
        t in 1..5 -> (60 - t * 5).coerceAtLeast(35)
        t in 6..10 -> 38
        else -> 30
    }
    val lh = when {
        t < -2 -> 15
        t == -2 -> 30
        t == -1 -> 100
        t == 0 -> 80
        t == 1 -> 25
        else -> 15
    }
    val fsh = when {
        t < -13 -> 45
        t in -13..-10 -> 40
        t in -9..-3 -> 20
        t == -2 -> 25
        t in -1..0 -> 55
        else -> 15
    }
    val progesterone = when {
        t <= -2 -> 8
        t == -1 -> 12
        t == 0 -> 15
        t == 1 -> 20
        t == 2 -> 30
        t == 3 -> 45
        t == 4 -> 60
        t == 5 -> 75
        t == 6 -> 90
        t == 7 -> 100
        t == 8 -> 95
        t == 9 -> 85
        t == 10 -> 70
        t == 11 -> 50
        t == 12 -> 30
        else -> 12
    }
    return mapOf(
        "Эстроген" to estrogen,
        "Прогестерон" to progesterone,
        "ЛГ (лютеинизирующий гормон)" to lh,
        "ФСГ (фолликулостимулирующий)" to fsh
    )
}

fun isIrregular(info: CycleInfo): Boolean {
    val lengths = info.cycleLengths.takeLast(6)
    if (lengths.size < 3) return false
    return (lengths.maxOrNull() ?: 0) - (lengths.minOrNull() ?: 0) > 10
}

fun currentPhaseLabel(info: CycleInfo): Pair<String, String>? {
    val day = info.currentCycleDay ?: return null
    val today = info.today
    val ovulation = info.ovulationDate
    return when {
        day <= info.settingsPeriodLength ->
            "Менструация" to "Дни месячных. Обычно длятся ${info.settingsPeriodLength} ${pluralDays(info.settingsPeriodLength.toLong())}."
        ovulation == today ->
            "Овуляция" to "Выход яйцеклетки из яичника. Самая высокая вероятность беременности."
        info.fertileStart != null && info.fertileEnd != null &&
            !today.isBefore(info.fertileStart) && !today.isAfter(info.fertileEnd) ->
            "Фертильное окно" to "Повышенная вероятность беременности в эти дни."
        ovulation != null && today.isBefore(ovulation) ->
            "Фолликулярная фаза" to "Организм готовится к овуляции."
        else ->
            "Лютеиновая фаза" to "Организм готовится к следующим месячным."
    }
}

fun delayDays(info: CycleInfo): Int? {
    val start = info.nextPeriodStart ?: return null
    val diff = ChronoUnit.DAYS.between(start, info.today)
    return if (diff > 0) diff.toInt() else null
}

fun pregnancyStatusText(p: PregnancyInfo): String =
    "Неделя ${p.weeks + 1}, день ${p.daysIntoWeek + 1} • ${p.trimester} триместр"
