package com.fazli.momentum.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.fazli.momentum.MomentumApplication
import java.time.LocalDate

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        val app = context.applicationContext as MomentumApplication
        val todayStr = LocalDate.now().toString()

        val completions = app.repository.getCompletionsForDateList(todayStr)
        val currentlyCompleted = completions.any { it.taskId == taskId && it.completed }
        app.repository.toggleCompletion(taskId, todayStr, !currentlyCompleted)

        MomentumWidget().update(context, glanceId)
    }
}
