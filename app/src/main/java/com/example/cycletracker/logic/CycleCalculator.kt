package com.example.cycletracker.logic

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

enum class DayPhase {
    PERIOD,
    PREDICTED_PERIOD,
    OVULATION,
    FERTILE,
    NORMAL
}

data class CycleInfo(
    val periods: List<LocalDate>,
    val cycleLengths: List<Int>,
    val settingsCycleLength: Int,
    val settingsPeriodLength: Int,
    val settingsLutealLength: Int,
    val settingsFertileDays: Int,
    val today: LocalDate,
    val nextPeriodStart: LocalDate?,
    val nextPeriodEnd: LocalDate?,
    val ovulationDate: LocalDate?,
    val fertileStart: LocalDate?,
    val fertileEnd: LocalDate?,
    val currentCycleStart: LocalDate?,
    val currentCycleDay: Int?
) {
    val avgCycleLength: Int
        get() = if (cycleLengths.isEmpty()) settingsCycleLength
        else cycleLengths.average().roundToInt().coerceIn(15, 45)
}

class CycleCalculator {

    fun compute(
        periods: List<LocalDate>,
        defaultCycle: Int,
        periodLength: Int,
        lutealLength: Int,
        fertileDays: Int = 5,
        today: LocalDate
    ): CycleInfo {
        val sorted = periods.filterNot { it.isAfter(today) }.distinct().sorted()
        val diffs = sorted.zipWithNext().map { (a, b) -> ChronoUnit.DAYS.between(a, b).toInt() }
        val avg = if (diffs.isEmpty()) defaultCycle
        else diffs.takeLast(6).average().roundToInt().coerceIn(15, 45)

        val nextStart = sorted.lastOrNull()?.plusDays(avg.toLong())
        val nextEnd = nextStart?.plusDays((periodLength - 1).toLong())
        val ovulation = nextStart?.minusDays(lutealLength.toLong())
        val fertileStart = ovulation?.minusDays(fertileDays.toLong())
        val fertileEnd = ovulation?.plusDays(1)
        val currentStart = sorted.lastOrNull()
        val currentDay = currentStart?.let { ChronoUnit.DAYS.between(it, today).toInt() + 1 }

        return CycleInfo(
            periods = sorted,
            cycleLengths = diffs,
            settingsCycleLength = defaultCycle,
            settingsPeriodLength = periodLength,
            settingsLutealLength = lutealLength,
            settingsFertileDays = fertileDays,
            today = today,
            nextPeriodStart = nextStart,
            nextPeriodEnd = nextEnd,
            ovulationDate = ovulation,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            currentCycleStart = currentStart,
            currentCycleDay = currentDay
        )
    }
}

fun phaseOn(date: LocalDate, info: CycleInfo): DayPhase {
    for (start in info.periods) {
        val end = start.plusDays((info.settingsPeriodLength - 1).toLong())
        if (!date.isBefore(start) && !date.isAfter(end)) return DayPhase.PERIOD
    }
    info.ovulationDate?.let { if (date == it) return DayPhase.OVULATION }
    if (info.fertileStart != null && info.fertileEnd != null &&
        !date.isBefore(info.fertileStart) && !date.isAfter(info.fertileEnd)
    ) {
        return DayPhase.FERTILE
    }
    if (info.nextPeriodStart != null && info.nextPeriodEnd != null &&
        !date.isBefore(info.nextPeriodStart) && !date.isAfter(info.nextPeriodEnd)
    ) {
        return DayPhase.PREDICTED_PERIOD
    }
    return DayPhase.NORMAL
}
