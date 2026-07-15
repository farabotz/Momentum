package com.fazli.momentum.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fazli.momentum.MomentumApplication
import kotlinx.coroutines.flow.first

class WeeklyReviewWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MomentumApplication
        NotificationHelper.ensureChannels(applicationContext)

        NotificationHelper.notify(
            applicationContext,
            NotificationHelper.CHANNEL_WEEKLY,
            NotificationHelper.NOTIFICATION_ID_WEEKLY,
            "Waktunya review minggu ini",
            "Isi menang, macet, dan penyesuaian di layar Progres."
        )

        val enabled = app.settingsRepository.weeklyReviewReminderEnabledFlow.first()
        ReminderScheduler.scheduleWeekly(applicationContext, enabled)

        return Result.success()
    }
}
