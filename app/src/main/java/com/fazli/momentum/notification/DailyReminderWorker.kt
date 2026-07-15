package com.fazli.momentum.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fazli.momentum.MomentumApplication
import com.fazli.momentum.data.TaskTier
import com.fazli.momentum.domain.calculateStreakAndWarning
import com.fazli.momentum.widget.WidgetRefresher
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MomentumApplication
        NotificationHelper.ensureChannels(applicationContext)

        val today = LocalDate.now()
        val todayStr = today.toString()
        val wajibTasks = app.repository.getActiveTasksList().filter { it.tier == TaskTier.WAJIB }
        val todayCompletions = app.repository.getCompletionsForDateList(todayStr)
        val completedIds = todayCompletions.filter { it.completed }.map { it.taskId }.toSet()
        val remaining = wajibTasks.count { !completedIds.contains(it.id) }

        val text = if (remaining == 0) {
            "Semua WAJIB hari ini sudah kecentang. Mantap!"
        } else {
            "$remaining WAJIB tersisa hari ini."
        }
        NotificationHelper.notify(applicationContext, NotificationHelper.CHANNEL_DAILY, NotificationHelper.NOTIFICATION_ID_DAILY, "Cek rencana hari ini", text)

        val allCompletions = app.repository.getAllCompletionsList()
        val (_, warning) = calculateStreakAndWarning(wajibTasks.map { it.id }, allCompletions, today)
        if (warning) {
            NotificationHelper.notify(
                applicationContext,
                NotificationHelper.CHANNEL_WARNING,
                NotificationHelper.NOTIFICATION_ID_WARNING,
                "2 hari WAJIB bolong",
                "Yuk mulai lagi hari ini biar streak gak putus lebih jauh."
            )
        }

        WidgetRefresher.refresh()

        val time = app.settingsRepository.dailyReminderTimeFlow.first()
        ReminderScheduler.scheduleDaily(applicationContext, time)

        return Result.success()
    }
}
