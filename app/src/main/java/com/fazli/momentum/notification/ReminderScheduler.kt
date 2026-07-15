package com.fazli.momentum.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_DAILY = "daily_reminder_work"
    private const val WORK_WEEKLY = "weekly_review_work"

    fun scheduleDaily(context: Context, time: String) {
        val (hour, minute) = parseTime(time)
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, minute)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val delayMs = Duration.between(now, target).toMillis()

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_DAILY, ExistingWorkPolicy.REPLACE, request)
    }

    fun scheduleWeekly(context: Context, enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_WEEKLY)
            return
        }
        val now = LocalDateTime.now()
        var target = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(19, 0)
        if (!target.isAfter(now)) target = target.plusWeeks(1)
        val delayMs = Duration.between(now, target).toMillis()

        val request = OneTimeWorkRequestBuilder<WeeklyReviewWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_WEEKLY, ExistingWorkPolicy.REPLACE, request)
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        return Pair(parts.getOrElse(0) { 8 }, parts.getOrElse(1) { 0 })
    }
}
