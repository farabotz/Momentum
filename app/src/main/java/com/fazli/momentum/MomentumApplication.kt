package com.fazli.momentum

import android.app.Application
import com.fazli.momentum.data.AppDatabase
import com.fazli.momentum.data.MomentumRepository
import com.fazli.momentum.data.SettingsRepository
import com.fazli.momentum.data.createSettingsDataStore
import com.fazli.momentum.widget.WidgetRefresher

class MomentumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WidgetRefresher.init(this)
    }

    private val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy {
        MomentumRepository(
            pillarDao = database.pillarDao(),
            taskDao = database.taskDao(),
            completionDao = database.completionDao(),
            counterDao = database.counterDao(),
            reviewDao = database.reviewDao(),
            milestoneDao = database.milestoneDao(),
            journalDao = database.journalDao()
        )
    }

    val settingsRepository by lazy {
        SettingsRepository(createSettingsDataStore(this))
    }
}
