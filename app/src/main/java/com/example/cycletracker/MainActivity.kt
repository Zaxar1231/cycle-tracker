package com.example.cycletracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cycletracker.data.CyclePreferences
import com.example.cycletracker.notify.Notifications
import com.example.cycletracker.notify.Reminders
import com.example.cycletracker.ui.BackgroundBox
import com.example.cycletracker.ui.MainScreen
import com.example.cycletracker.ui.screens.OnboardingScreen
import com.example.cycletracker.ui.screens.PinScreen
import com.example.cycletracker.ui.theme.CycleTrackerTheme

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notifications.ensureChannels(this)
        Reminders.rescheduleAll(this, CyclePreferences(this))
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val viewModel: CycleViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            CycleTrackerTheme(themeScheme = settings.themeScheme) {
                var locked by rememberSaveable { mutableStateOf(true) }
                when {
                    !settings.onboardingDone -> OnboardingScreen(viewModel)
                    settings.pinEnabled && locked -> PinScreen(
                        checkPin = { viewModel.checkPin(it) },
                        onUnlock = { locked = false }
                    )
                    else -> BackgroundBox(settings.backgroundUri) {
                        MainScreen(viewModel)
                    }
                }
            }
        }
    }
}
