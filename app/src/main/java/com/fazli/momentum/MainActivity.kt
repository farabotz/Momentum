package com.fazli.momentum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fazli.momentum.data.AppTheme
import com.fazli.momentum.ui.theme.colorSchemeFor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MomentumApplication
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
