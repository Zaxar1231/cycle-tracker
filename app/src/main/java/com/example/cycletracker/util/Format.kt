package com.example.cycletracker.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val RU = Locale("ru")

fun LocalDate.formatShort(): String = format(DateTimeFormatter.ofPattern("d MMMM", RU))

fun LocalDate.formatFull(): String = format(DateTimeFormatter.ofPattern("d MMMM yyyy", RU))

fun YearMonth.monthTitle(): String =
    format(DateTimeFormatter.ofPattern("LLLL yyyy", RU)).replaceFirstChar { it.uppercase() }

fun pluralDays(n: Long): String {
    val n100 = n % 100
    val n10 = n % 10
    return when {
        n100 in 11..14 -> "дней"
        n10 == 1L -> "день"
        n10 in 2..4 -> "дня"
        else -> "дней"
    }
}

fun daysUntilText(today: LocalDate, target: LocalDate): String {
    val diff = ChronoUnit.DAYS.between(today, target)
    return when {
        diff == 0L -> "сегодня"
        diff == 1L -> "завтра"
        diff > 0 -> "через $diff ${pluralDays(diff)}"
        else -> "уже прошло"
    }
}
