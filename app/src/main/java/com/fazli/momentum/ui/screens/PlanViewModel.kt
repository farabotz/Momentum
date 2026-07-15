package com.fazli.momentum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fazli.momentum.data.MomentumRepository
import com.fazli.momentum.data.Pillar
import com.fazli.momentum.data.SettingsRepository
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskCompletion
import com.fazli.momentum.data.TaskRecurrence
import com.fazli.momentum.data.TaskTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit

enum class PlanView { HARIAN, MINGGUAN, BULANAN }

data class PlanUiState(
    val isLoading: Boolean = true,
    val view: PlanView = PlanView.HARIAN,
    val today: LocalDate = LocalDate.now(),
    val pillars: List<Pillar> = emptyList(),
    val todayTasksByPillar: Map<Pillar, List<TaskWithCompletion>> = emptyMap(),
    val weekDays: List<LocalDate> = emptyList(),
    val weekTasksByDay: Map<LocalDate, List<Task>> = emptyMap(),
    val weekWajibDone: Int = 0,
    val weekWajibTotal: Int = 0,
    val monthDays: List<LocalDate> = emptyList(),
    val successDays: Set<LocalDate> = emptySet(),
    val monthMilestones: List<com.fazli.momentum.data.Milestone> = emptyList()
)

/** Pure, testable: which tasks are scheduled to appear on [date] by recurrence rule. */
fun tasksForDate(tasks: List<Task>, date: LocalDate): List<Task> {
    return tasks.filter { task ->
        when (task.recurrence) {
            TaskRecurrence.DAILY -> true
            TaskRecurrence.WEEKLY -> {
                val days = task.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                days.contains(date.dayOfWeek.value)
            }
            TaskRecurrence.MONTHLY -> true
            TaskRecurrence.ONCE -> true
        }
    }
}

class PlanViewModel(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val viewState = MutableStateFlow(PlanView.HARIAN)

    val uiState: StateFlow<PlanUiState> = combine(
        repository.getPillars(),
        repository.getActiveTasks(),
        repository.getAllCompletions(),
        repository.getMilestones(),
        settingsRepository.startDateFlow,
        viewState
    ) { values ->
        @Suppress("UNCHECKED_CAST") val pillars = values[0] as List<Pillar>
        @Suppress("UNCHECKED_CAST") val tasks = values[1] as List<Task>
        @Suppress("UNCHECKED_CAST") val completions = values[2] as List<TaskCompletion>
        @Suppress("UNCHECKED_CAST") val milestones = values[3] as List<com.fazli.momentum.data.Milestone>
        val startDateStr = values[4] as String
        val view = values[5] as PlanView

        val today = LocalDate.now()
        val startDate = LocalDate.parse(startDateStr)
        val dayNumber = ChronoUnit.DAYS.between(startDate, today) + 1
        val completedByDate = completions.filter { it.completed }.groupBy { it.date }.mapValues { it.value.map { c -> c.taskId }.toSet() }
        val onceCompletedIds = completions.filter { it.completed }.map { it.taskId }.toSet()

        fun visibleTasks(date: LocalDate) = tasksForDate(tasks, date).filter { t ->
            t.recurrence != TaskRecurrence.ONCE || !onceCompletedIds.contains(t.id)
        }

        // Harian
        val todayVisible = visibleTasks(today)
        val todayCompletedIds = completedByDate[today.toString()] ?: emptySet()
        val todayByPillar = pillars.associateWith { pillar ->
            todayVisible.filter { it.pillarId == pillar.id }
                .sortedBy { it.order }
                .map { TaskWithCompletion(it, todayCompletedIds.contains(it.id)) }
        }

        // Mingguan
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
        val weekTasksByDay = weekDays.associateWith { d -> visibleTasks(d).filter { it.tier == TaskTier.WAJIB } }
        val weekWajibTotal = weekTasksByDay.values.sumOf { it.size }
        val weekWajibDone = weekDays.sumOf { d ->
            val done = completedByDate[d.toString()] ?: emptySet()
            weekTasksByDay[d]?.count { done.contains(it.id) } ?: 0
        }

        // Bulanan
        val monthStart = today.withDayOfMonth(1)
        val monthDays = (0 until monthStart.lengthOfMonth()).map { monthStart.plusDays(it.toLong()) }
        val successDays = monthDays.filter { d ->
            val wajibIds = visibleTasks(d).filter { it.tier == TaskTier.WAJIB }.map { it.id }
            val done = completedByDate[d.toString()] ?: emptySet()
            wajibIds.isNotEmpty() && wajibIds.all { done.contains(it) }
        }.toSet()
        val programMonth = (((dayNumber - 1) / 30) + 1).toInt().coerceIn(1, 3)
        val monthMilestones = milestones.filter { it.month == programMonth }

        PlanUiState(
            isLoading = false,
            view = view,
            today = today,
            pillars = pillars,
            todayTasksByPillar = todayByPillar,
            weekDays = weekDays,
            weekTasksByDay = weekTasksByDay,
            weekWajibDone = weekWajibDone,
            weekWajibTotal = weekWajibTotal,
            monthDays = monthDays,
            successDays = successDays,
            monthMilestones = monthMilestones
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlanUiState())

    fun setView(view: PlanView) {
        viewState.value = view
    }

    fun toggleTask(taskId: String, date: LocalDate, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(taskId, date.toString(), completed)
            com.fazli.momentum.widget.WidgetRefresher.refresh()
        }
    }

    fun toggleMilestone(id: String, done: Boolean) {
        viewModelScope.launch {
            repository.updateMilestoneDone(id, done)
        }
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

    fun moveTask(current: Task, other: Task) {
        viewModelScope.launch {
            repository.updateTask(current.copy(order = other.order))
            repository.updateTask(other.copy(order = current.order))
        }
    }
}

class PlanViewModelFactory(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlanViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
