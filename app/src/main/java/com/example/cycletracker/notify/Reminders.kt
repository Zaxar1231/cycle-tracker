package com.example.cycletracker.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.widget.CycleWidgetProvider
import java.util.Calendar

object Reminders {

    const val ACTION_REMINDER = "com.example.cycletracker.REMINDER"
    const val EXTRA_TYPE = "type"

    const val TYPE_PILL = "pill"
    const val TYPE_WATER = "water"
    const val TYPE_HEALTH = "health"
    const val TYPE_FERTILITY = "fertility"

    private const val CODE_PILL = 1101
    private const val CODE_WATER = 1102
    private const val CODE_HEALTH = 1103
    private const val CODE_FERTILITY = 1104

    private fun pendingIntent(context: Context, code: Int, type: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis
    }

    fun scheduleDaily(context: Context, code: Int, type: String, hour: Int, minute: Int) {
        alarmManager(context).setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTrigger(hour, minute),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context, code, type)
        )
    }

    fun scheduleEveryHours(context: Context, code: Int, type: String, startHour: Int, intervalHours: Int) {
        alarmManager(context).setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTrigger(startHour, 0),
            intervalHours.toLong() * 60 * 60 * 1000,
            pendingIntent(context, code, type)
        )
    }

    fun cancel(context: Context, code: Int, type: String) {
        alarmManager(context).cancel(pendingIntent(context, code, type))
    }

    fun cancelAll(context: Context) {
        cancel(context, CODE_PILL, TYPE_PILL)
        cancel(context, CODE_WATER, TYPE_WATER)
        cancel(context, CODE_HEALTH, TYPE_HEALTH)
        cancel(context, CODE_FERTILITY, TYPE_FERTILITY)
    }

    fun rescheduleAll(context: Context, prefs: CyclePreferences) {
        cancelAll(context)
        if (prefs.loadPillEnabled() && prefs.loadPillStartDate() != null) {
            scheduleDaily(context, CODE_PILL, TYPE_PILL, prefs.loadPillHour(), prefs.loadPillMinute())
        }
        if (prefs.loadWaterEnabled()) {
            scheduleEveryHours(
                context, CODE_WATER, TYPE_WATER,
                prefs.loadWaterStartHour(), prefs.loadWaterIntervalHours()
            )
        }
        if (prefs.loadHealthReminderEnabled()) {
            scheduleDaily(context, CODE_HEALTH, TYPE_HEALTH, prefs.loadHealthHour(), prefs.loadHealthMinute())
        }
        if (prefs.loadFertilityNotify()) {
            scheduleDaily(context, CODE_FERTILITY, TYPE_FERTILITY, 9, 0)
        }
    }

    fun updateWidgets(context: Context) {
        context.sendBroadcast(
            Intent(context, CycleWidgetProvider::class.java)
                .setAction(CycleWidgetProvider.ACTION_DATA_CHANGED)
        )
    }
}
