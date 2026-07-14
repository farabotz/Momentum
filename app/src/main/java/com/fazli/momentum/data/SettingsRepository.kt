package com.fazli.momentum.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val START_DATE = stringPreferencesKey("start_date")
    }

    val startDateFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[START_DATE] ?: LocalDate.now().toString()
    }

    suspend fun initStartDateIfMissing() {
        dataStore.edit { prefs ->
            if (prefs[START_DATE] == null) {
                prefs[START_DATE] = LocalDate.now().toString()
            }
        }
    }
}
