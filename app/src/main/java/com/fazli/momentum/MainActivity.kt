package com.fazli.momentum

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fazli.momentum.data.AppTheme
import com.fazli.momentum.notification.NotificationHelper
import com.fazli.momentum.notification.ReminderScheduler
import com.fazli.momentum.ui.theme.colorSchemeFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MomentumApplication

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        lifecycleScope.launch {
            NotificationHelper.ensureChannels(this@MainActivity)
            val dailyTime = app.settingsRepository.dailyReminderTimeFlow.first()
            val weeklyEnabled = app.settingsRepository.weeklyReviewReminderEnabledFlow.first()
            ReminderScheduler.scheduleDaily(this@MainActivity, dailyTime)
            ReminderScheduler.scheduleWeekly(this@MainActivity, weeklyEnabled)
        }

        setContent {
            val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = AppTheme.MIDNIGHT)
            MaterialTheme(
                colorScheme = colorSchemeFor(activeTheme)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MomentumApp()
                }
            }
        }
    }
}
