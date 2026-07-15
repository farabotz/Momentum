package com.fazli.momentum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fazli.momentum.data.AppTheme
import com.fazli.momentum.data.JournalEntry
import com.fazli.momentum.data.Milestone
import com.fazli.momentum.data.MomentumRepository
import com.fazli.momentum.data.Pillar
import com.fazli.momentum.data.ProgressCounter
import com.fazli.momentum.data.SettingsRepository
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskCompletion
import com.fazli.momentum.data.TaskRecurrence
import com.fazli.momentum.data.TaskTier
import com.fazli.momentum.data.WeeklyReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class SettingsUiState(
    val isLoading: Boolean = true,
    val theme: AppTheme = AppTheme.MIDNIGHT,
    val pillars: List<Pillar> = emptyList(),
    val tasksByPillar: Map<Pillar, List<Task>> = emptyMap(),
    val dailyReminderTime: String = SettingsRepository.DEFAULT_DAILY_REMINDER_TIME,
    val weeklyReviewReminderEnabled: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val periodLengthDays: Int = SettingsRepository.DEFAULT_PERIOD_LENGTH_DAYS
)

class SettingsViewModel(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getPillars(),
        repository.getTasks(),
        settingsRepository.activeThemeFlow,
        settingsRepository.dailyReminderTimeFlow,
        settingsRepository.weeklyReviewReminderEnabledFlow,
        settingsRepository.startDateFlow,
        settingsRepository.periodLengthDaysFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val pillars = (values[0] as List<Pillar>).sortedBy { it.order }
        @Suppress("UNCHECKED_CAST")
        val tasks = values[1] as List<Task>
        val theme = values[2] as AppTheme
        val dailyReminderTime = values[3] as String
        val weeklyReviewReminderEnabled = values[4] as Boolean
        val startDate = LocalDate.parse(values[5] as String)
        val periodLengthDays = values[6] as Int

        SettingsUiState(
            isLoading = false,
            theme = theme,
            pillars = pillars,
            tasksByPillar = pillars.associateWith { p -> tasks.filter { it.pillarId == p.id }.sortedBy { it.order } },
            dailyReminderTime = dailyReminderTime,
            weeklyReviewReminderEnabled = weeklyReviewReminderEnabled,
            startDate = startDate,
            periodLengthDays = periodLengthDays
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setActiveTheme(theme) }
    }

    fun setDailyReminderTime(time: String) {
        viewModelScope.launch { settingsRepository.setDailyReminderTime(time) }
    }

    fun setWeeklyReviewReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWeeklyReviewReminderEnabled(enabled) }
    }

    fun setStartDate(date: LocalDate) {
        viewModelScope.launch { settingsRepository.setStartDate(date.toString()) }
    }

    fun setPeriodLengthDays(days: Int) {
        viewModelScope.launch { settingsRepository.setPeriodLengthDays(days) }
    }

    fun addTask(task: Task) {
        viewModelScope.launch { repository.insertTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun setTaskActive(task: Task, active: Boolean) {
        viewModelScope.launch { repository.updateTask(task.copy(active = active)) }
    }

    fun addPillar(name: String) {
        viewModelScope.launch {
            val nextOrder = uiState.value.pillars.size
            repository.insertPillar(
                Pillar(
                    id = "p_${System.currentTimeMillis()}",
                    name = name,
                    iconName = "circle",
                    colorKey = "neutral",
                    order = nextOrder
                )
            )
        }
    }

    fun renamePillar(pillar: Pillar, newName: String) {
        viewModelScope.launch { repository.insertPillar(pillar.copy(name = newName)) }
    }

    fun deletePillar(pillar: Pillar) {
        viewModelScope.launch { repository.deletePillar(pillar) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            settingsRepository.setStartDate(LocalDate.now().toString())
        }
    }

    suspend fun exportData(): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)

        val state = uiState.value
        root.put(
            "settings",
            JSONObject().apply {
                put("activeTheme", state.theme.name)
                put("startDate", state.startDate.toString())
                put("periodLengthDays", state.periodLengthDays)
                put("dailyReminderTime", state.dailyReminderTime)
                put("weeklyReviewReminderEnabled", state.weeklyReviewReminderEnabled)
            }
        )

        root.put("pillars", jsonArrayOf(repository.getPillarsList()) { it.toJson() })
        root.put("tasks", jsonArrayOf(repository.getTasksList()) { it.toJson() })
        root.put("completions", jsonArrayOf(repository.getAllCompletionsList()) { it.toJson() })
        root.put("counters", jsonArrayOf(repository.getCountersList()) { it.toJson() })
        root.put("reviews", jsonArrayOf(repository.getReviewsList()) { it.toJson() })
        root.put("milestones", jsonArrayOf(repository.getMilestonesList()) { it.toJson() })
        root.put("journalEntries", jsonArrayOf(repository.getJournalEntriesList()) { it.toJson() })

        return root.toString(2)
    }

    suspend fun importData(json: String) {
        val root = JSONObject(json)

        root.optJSONObject("settings")?.let { settings ->
            settings.optStringOrNull("activeTheme")
                ?.let { name -> runCatching { AppTheme.valueOf(name) }.getOrNull() }
                ?.let { settingsRepository.setActiveTheme(it) }
            settings.optStringOrNull("startDate")?.let { settingsRepository.setStartDate(it) }
            if (settings.has("periodLengthDays")) settingsRepository.setPeriodLengthDays(settings.getInt("periodLengthDays"))
            settings.optStringOrNull("dailyReminderTime")?.let { settingsRepository.setDailyReminderTime(it) }
            if (settings.has("weeklyReviewReminderEnabled")) {
                settingsRepository.setWeeklyReviewReminderEnabled(settings.getBoolean("weeklyReviewReminderEnabled"))
            }
        }

        root.optJSONArray("pillars")?.let { arr -> repository.insertPillars(arr.mapObjects { pillarFromJson(it) }) }
        root.optJSONArray("tasks")?.let { arr -> repository.insertTasks(arr.mapObjects { taskFromJson(it) }) }
        root.optJSONArray("completions")?.let { arr -> repository.insertCompletions(arr.mapObjects { completionFromJson(it) }) }
        root.optJSONArray("counters")?.let { arr -> repository.insertCounters(arr.mapObjects { counterFromJson(it) }) }
        root.optJSONArray("reviews")?.let { arr -> repository.insertReviews(arr.mapObjects { reviewFromJson(it) }) }
        root.optJSONArray("milestones")?.let { arr -> repository.insertMilestones(arr.mapObjects { milestoneFromJson(it) }) }
        root.optJSONArray("journalEntries")?.let { arr -> repository.insertJournalEntries(arr.mapObjects { journalFromJson(it) }) }
    }
}

class SettingsViewModelFactory(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun <T> jsonArrayOf(items: List<T>, toJson: (T) -> JSONObject): JSONArray {
    val arr = JSONArray()
    items.forEach { arr.put(toJson(it)) }
    return arr
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> = (0 until length()).map { transform(getJSONObject(it)) }

private fun JSONObject.optIntOrNull(key: String): Int? = if (has(key) && !isNull(key)) getInt(key) else null
private fun JSONObject.optStringOrNull(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null

private fun Pillar.toJson() = JSONObject().apply {
    put("id", id); put("name", name); put("iconName", iconName); put("colorKey", colorKey); put("order", order)
}

private fun pillarFromJson(o: JSONObject) = Pillar(
    id = o.getString("id"),
    name = o.getString("name"),
    iconName = o.getString("iconName"),
    colorKey = o.getString("colorKey"),
    order = o.getInt("order")
)

private fun Task.toJson() = JSONObject().apply {
    put("id", id); put("title", title); put("description", description); put("pillarId", pillarId)
    put("tier", tier.name); put("recurrence", recurrence.name)
    targetMinutes?.let { put("targetMinutes", it) }
    daysOfWeek?.let { put("daysOfWeek", it) }
    put("active", active); put("order", order); put("createdAt", createdAt)
}

private fun taskFromJson(o: JSONObject) = Task(
    id = o.getString("id"),
    title = o.getString("title"),
    description = o.getString("description"),
    pillarId = o.getString("pillarId"),
    tier = TaskTier.valueOf(o.getString("tier")),
    recurrence = TaskRecurrence.valueOf(o.getString("recurrence")),
    targetMinutes = o.optIntOrNull("targetMinutes"),
    daysOfWeek = o.optStringOrNull("daysOfWeek"),
    active = o.getBoolean("active"),
    order = o.getInt("order"),
    createdAt = o.getLong("createdAt")
)

private fun TaskCompletion.toJson() = JSONObject().apply {
    put("id", id); put("taskId", taskId); put("date", date); put("completed", completed)
    note?.let { put("note", it) }
}

private fun completionFromJson(o: JSONObject) = TaskCompletion(
    id = o.getString("id"),
    taskId = o.getString("taskId"),
    date = o.getString("date"),
    completed = o.getBoolean("completed"),
    note = o.optStringOrNull("note")
)

private fun ProgressCounter.toJson() = JSONObject().apply {
    put("id", id); put("label", label); put("currentValue", currentValue)
    targetValue?.let { put("targetValue", it) }
    put("order", order)
}

private fun counterFromJson(o: JSONObject) = ProgressCounter(
    id = o.getString("id"),
    label = o.getString("label"),
    currentValue = o.getInt("currentValue"),
    targetValue = o.optIntOrNull("targetValue"),
    order = o.getInt("order")
)

private fun WeeklyReview.toJson() = JSONObject().apply {
    put("id", id); put("weekStartDate", weekStartDate); put("win", win)
    put("struggle", struggle); put("adjust", adjust); put("createdAt", createdAt)
}

private fun reviewFromJson(o: JSONObject) = WeeklyReview(
    id = o.getString("id"),
    weekStartDate = o.getString("weekStartDate"),
    win = o.getString("win"),
    struggle = o.getString("struggle"),
    adjust = o.getString("adjust"),
    createdAt = o.getLong("createdAt")
)

private fun Milestone.toJson() = JSONObject().apply {
    put("id", id); put("month", month); put("title", title); put("description", description); put("done", done)
    targetDate?.let { put("targetDate", it) }
}

private fun milestoneFromJson(o: JSONObject) = Milestone(
    id = o.getString("id"),
    month = o.getInt("month"),
    title = o.getString("title"),
    description = o.getString("description"),
    done = o.getBoolean("done"),
    targetDate = o.optStringOrNull("targetDate")
)

private fun JournalEntry.toJson() = JSONObject().apply {
    put("id", id); put("date", date); put("text", text)
}

private fun journalFromJson(o: JSONObject) = JournalEntry(
    id = o.getString("id"),
    date = o.getString("date"),
    text = o.getString("text")
)
