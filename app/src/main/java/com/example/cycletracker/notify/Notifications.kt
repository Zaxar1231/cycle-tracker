package com.example.cycletracker.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cycletracker.MainActivity
import com.example.cycletracker.R
import com.example.cycletracker.logic.CycleInfo
import com.example.cycletracker.util.formatShort

object Notifications {

    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_CYCLE = "cycle"

    const val ID_PILL = 1001
    const val ID_WATER = 1002
    const val ID_HEALTH = 1003
    const val ID_FERTILITY = 1004
    const val ID_IRREGULAR = 1005

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Напоминания", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Таблетки, вода, здоровье"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CYCLE, "Цикл", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Вероятность зачатия и регулярность цикла"
            }
        )
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun post(context: Context, channel: String, id: Int, title: String, text: String) {
        ensureChannels(context)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    fun postPill(context: Context, text: String) {
        post(context, CHANNEL_REMINDERS, ID_PILL, "Пора принять таблетку 💊", text)
    }

    fun postWater(context: Context) {
        post(context, CHANNEL_REMINDERS, ID_WATER, "Время попить воды 💧", "Выпейте стакан воды — это полезно в любой фазе цикла.")
    }

    fun postHealth(context: Context) {
        post(
            context, CHANNEL_REMINDERS, ID_HEALTH, "Как вы себя чувствуете? 🌸",
            "Запишите настроение, симптомы и температуру в дневник приложения."
        )
    }

    fun postFertility(context: Context, probability: Int) {
        post(
            context, CHANNEL_CYCLE, ID_FERTILITY, "Высокая вероятность зачатия ⚠️",
            "Сегодня вероятность зачатия около $probability%. Если беременность не планируется — используйте контрацепцию."
        )
    }

    fun postIrregular(context: Context, info: CycleInfo) {
        post(
            context, CHANNEL_CYCLE, ID_IRREGULAR, "Нерегулярный цикл ⚠️",
            "Разница между последними циклами больше 10 дней. Если это повторяется — проконсультируйтесь с гинекологом."
        )
    }
}
