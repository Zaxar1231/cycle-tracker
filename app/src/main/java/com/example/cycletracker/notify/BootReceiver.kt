package com.example.cycletracker.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cycletracker.data.CyclePreferences

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Notifications.ensureChannels(context)
        Reminders.rescheduleAll(context, CyclePreferences(context))
        Reminders.updateWidgets(context)
    }
}
