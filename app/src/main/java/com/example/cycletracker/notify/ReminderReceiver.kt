package com.example.cycletracker.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.logic.CycleCalculator
import com.example.cycletracker.logic.PillSchedule
import com.example.cycletracker.logic.pregnancyProbability
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Reminders.ACTION_REMINDER) return
        Notifications.ensureChannels(context)
        val prefs = CyclePreferences(context)
        val type = intent.getStringExtra(Reminders.EXTRA_TYPE) ?: return
        val today = LocalDate.now()

        when (type) {
            Reminders.TYPE_PILL -> {
                if (!prefs.loadPillEnabled()) return
                val settings = prefs.loadSettings()
                val schedule = PillSchedule(settings)
                if (!schedule.isActiveDay(today)) return
                val n = (schedule.dayNumberOn(today) ?: 0) + 1
                val time = String.format("%02d:%02d", settings.pillHour, settings.pillMinute)
                Notifications.postPill(
                    context,
                    "Таблетка №$n из ${settings.pillActiveDays}. Время приёма: $time. Приём: ${settings.pillMealRule}."
                )
            }

            Reminders.TYPE_WATER -> {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                if (hour in prefs.loadWaterStartHour() until prefs.loadWaterEndHour()) {
                    Notifications.postWater(context)
                }
            }

            Reminders.TYPE_HEALTH -> {
                if (prefs.loadHealthReminderEnabled()) {
                    Notifications.postHealth(context)
                }
            }

            Reminders.TYPE_FERTILITY -> {
                if (!prefs.loadFertilityNotify()) return
                if (prefs.isPregnant()) return
                val info = CycleCalculator().compute(
                    periods = prefs.loadPeriodStarts(),
                    defaultCycle = prefs.loadCycleLength(),
                    periodLength = prefs.loadPeriodLength(),
                    lutealLength = prefs.loadLutealLength(),
                    fertileDays = prefs.loadFertileDays(),
                    today = today
                )
                val probability = pregnancyProbability(today, info)
                if (probability >= 10) {
                    Notifications.postFertility(context, probability)
                }
            }
        }
    }
}
