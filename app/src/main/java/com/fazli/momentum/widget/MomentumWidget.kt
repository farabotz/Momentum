package com.fazli.momentum.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fazli.momentum.MainActivity
import com.fazli.momentum.MomentumApplication
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskTier
import com.fazli.momentum.domain.calculateStreakAndWarning
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val WidgetBackground = ColorProvider(day = Color(0xFFFBF6EC), night = Color(0xFF1B1F26))
private val WidgetOnBackground = ColorProvider(day = Color(0xFF4A3F2C), night = Color(0xFFEDE8DC))
private val WidgetAccent = ColorProvider(day = Color(0xFFB5651D), night = Color(0xFFD9A441))

val TaskIdKey = ActionParameters.Key<String>("taskId")

data class WidgetData(
    val dayNumber: Long,
    val periodLengthDays: Int,
    val streak: Int,
    val wajibTasks: List<Pair<Task, Boolean>>
)

private suspend fun loadWidgetData(app: MomentumApplication): WidgetData {
    val repository = app.repository
    val settingsRepository = app.settingsRepository
    val today = LocalDate.now()
    val todayStr = today.toString()

    val startDate = LocalDate.parse(settingsRepository.startDateFlow.first())
    val periodLengthDays = settingsRepository.periodLengthDaysFlow.first()
    val dayNumber = ChronoUnit.DAYS.between(startDate, today) + 1

    val activeTasks = repository.getActiveTasksList()
    val wajibTasks = activeTasks.filter { it.tier == TaskTier.WAJIB }

    val todayCompletions = repository.getCompletionsForDateList(todayStr)
    val completedIds = todayCompletions.filter { it.completed }.map { it.taskId }.toSet()

    val allCompletions = repository.getAllCompletionsList()
    val (streak, _) = calculateStreakAndWarning(wajibTasks.map { it.id }, allCompletions, today)

    return WidgetData(
        dayNumber = dayNumber,
        periodLengthDays = periodLengthDays,
        streak = streak,
        wajibTasks = wajibTasks.map { it to completedIds.contains(it.id) }
    )
}

class MomentumWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MomentumApplication
        val data = loadWidgetData(app)

        provideContent {
            val size = LocalSize.current
            if (size.height < 120.dp) {
                SmallWidgetContent(data)
            } else {
                MediumWidgetContent(data)
            }
        }
    }
}

@Composable
private fun SmallWidgetContent(data: WidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "HARI KE-${data.dayNumber.coerceAtLeast(0)}/${data.periodLengthDays}",
            style = TextStyle(color = WidgetAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )
        Text(
            text = "Streak ${data.streak} hari",
            style = TextStyle(color = WidgetOnBackground, fontSize = 12.sp)
        )
        val doneCount = data.wajibTasks.count { it.second }
        Text(
            text = "WAJIB $doneCount/${data.wajibTasks.size}",
            style = TextStyle(color = WidgetOnBackground, fontSize = 12.sp)
        )
    }
}

@Composable
private fun MediumWidgetContent(data: WidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "HARI KE-${data.dayNumber.coerceAtLeast(0)}/${data.periodLengthDays}  ·  Streak ${data.streak}",
                style = TextStyle(color = WidgetAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            )
        }
        if (data.wajibTasks.isEmpty()) {
            Text(
                text = "Belum ada task WAJIB.",
                style = TextStyle(color = WidgetOnBackground, fontSize = 11.sp)
            )
        } else {
            data.wajibTasks.forEach { (task, completed) ->
                CheckBox(
                    checked = completed,
                    onCheckedChange = actionRunCallback<ToggleTaskAction>(actionParametersOf(TaskIdKey to task.id)),
                    text = task.title,
                    style = TextStyle(color = WidgetOnBackground, fontSize = 11.sp),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
        }
    }
}
