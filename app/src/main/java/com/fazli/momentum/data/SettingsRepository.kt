package com.fazli.momentum.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val START_DATE = stringPreferencesKey("start_date")
        val ACTIVE_THEME = stringPreferencesKey("active_theme")
        val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        val WEEKLY_REVIEW_REMINDER_ENABLED = booleanPreferencesKey("weekly_review_reminder_enabled")
        val PERIOD_LENGTH_DAYS = intPreferencesKey("period_length_days")

        const val DEFAULT_DAILY_REMINDER_TIME = "08:00"
        const val DEFAULT_PERIOD_LENGTH_DAYS = 90
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

    suspend fun setStartDate(date: String) {
        dataStore.edit { prefs -> prefs[START_DATE] = date }
    }

    val activeThemeFlow: Flow<AppTheme> = dataStore.data.map { prefs ->
        prefs[ACTIVE_THEME]?.let { name -> runCatching { AppTheme.valueOf(name) }.getOrNull() } ?: AppTheme.MIDNIGHT
    }

    suspend fun setActiveTheme(theme: AppTheme) {
        dataStore.edit { prefs -> prefs[ACTIVE_THEME] = theme.name }
    }

    val dailyReminderTimeFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_TIME] ?: DEFAULT_DAILY_REMINDER_TIME
    }

    suspend fun setDailyReminderTime(time: String) {
        dataStore.edit { prefs -> prefs[DAILY_REMINDER_TIME] = time }
    }

    val weeklyReviewReminderEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WEEKLY_REVIEW_REMINDER_ENABLED] ?: true
    }

    suspend fun setWeeklyReviewReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[WEEKLY_REVIEW_REMINDER_ENABLED] = enabled }
    }

    val periodLengthDaysFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PERIOD_LENGTH_DAYS] ?: DEFAULT_PERIOD_LENGTH_DAYS
    }

    suspend fun setPeriodLengthDays(days: Int) {
        dataStore.edit { prefs -> prefs[PERIOD_LENGTH_DAYS] = days }
    }
}
