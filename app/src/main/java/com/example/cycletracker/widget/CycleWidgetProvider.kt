package com.example.cycletracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.cycletracker.MainActivity
import com.example.cycletracker.R
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.logic.CycleCalculator
import com.example.cycletracker.logic.currentPhaseLabel
import com.example.cycletracker.util.formatShort
import java.time.LocalDate

class CycleWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_DATA_CHANGED = "com.example.cycletracker.WIDGET_UPDATE"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CycleWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = CyclePreferences(context)
            val info = CycleCalculator().compute(
                periods = prefs.loadPeriodStarts(),
                defaultCycle = prefs.loadCycleLength(),
                periodLength = prefs.loadPeriodLength(),
                lutealLength = prefs.loadLutealLength(),
                fertileDays = prefs.loadFertileDays(),
                today = LocalDate.now()
            )
            val phase = currentPhaseLabel(info)?.first ?: "Нет данных"
            val day = info.currentCycleDay?.let { "День цикла $it" } ?: "День цикла —"

            val views = RemoteViews(context.packageName, R.layout.widget_cycle)
            views.setTextViewText(R.id.widget_day, day)
            views.setTextViewText(R.id.widget_phase, phase)
            val nextText = info.nextPeriodStart?.let {
                "Следующие: ${it.formatShort()}"
            } ?: if (prefs.isPregnant()) "Режим беременности" else "Отметьте месячные"
            views.setTextViewText(R.id.widget_next, nextText)

            val openIntent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            manager.updateAppWidget(id, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_DATA_CHANGED) {
            updateAll(context)
        }
    }
}
