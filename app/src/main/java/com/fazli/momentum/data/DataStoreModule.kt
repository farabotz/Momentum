package com.fazli.momentum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * DataStore (Preferences) setup per SPEC §4.8. Active theme + reminder settings live here.
 * Fase 0 only scaffolds the instance; keys/schemas are wired in Fase 4 (Tema & Pengaturan).
 */
private const val SETTINGS_NAME = "momentum_settings"

fun createSettingsDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    ) {
        context.preferencesDataStoreFile(SETTINGS_NAME)
    }
}
