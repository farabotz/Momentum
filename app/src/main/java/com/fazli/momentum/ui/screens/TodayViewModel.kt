package com.fazli.momentum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fazli.momentum.data.JournalEntry
import com.fazli.momentum.data.MomentumRepository
import com.fazli.momentum.data.SettingsRepository
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskCompletion
import com.fazli.momentum.data.TaskTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TodayUiState(
    val isLoading: Boolean = true,
    val currentDate: LocalDate = LocalDate.now(),
    val dayNumber: Long = 1,
    val periodLengthDays: Int = 90,
    val streak: Int = 0,
    val completionRate: Float = 0f,
    val wajibTasks: List<TaskWithCompletion> = emptyList(),
    val bonusTasks: List<TaskWithCompletion> = emptyList(),
    val journalEntry: String = "",
    val showWarning: Boolean = false
)

data class TaskWithCompletion(
    val task: Task,
    val completed: Boolean
)

class TodayViewModel(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val todayStr: String = today.toString()
    private val journalInput = MutableStateFlow("")

    val uiState: StateFlow<TodayUiState> = combine(
        repository.getActiveTasks(),
        repository.getCompletionsForDate(todayStr),
        repository.getAllCompletions(),
        repository.getJournalEntryForDate(todayStr),
        settingsRepository.startDateFlow,
        settingsRepository.periodLengthDaysFlow,
        journalInput
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val tasks = values[0] as List<Task>
        @Suppress("UNCHECKED_CAST")
        val todayCompletions = values[1] as List<TaskCompletion>
        @Suppress("UNCHECKED_CAST")
        val allCompletions = values[2] as List<TaskCompletion>
        val journal = values[3] as JournalEntry?
        val startDateStr = values[4] as String
        val periodLengthDays = values[5] as Int
        val jInput = values[6] as String

        val currentDate = today
        val startDate = LocalDate.parse(startDateStr)
        val dayNumber = ChronoUnit.DAYS.between(startDate, currentDate) + 1
        
        val completedTaskIds = todayCompletions.filter { it.completed }.map { it.taskId }.toSet()
        
        val wajib = tasks.filter { it.tier == TaskTier.WAJIB }.map { 
            TaskWithCompletion(it, completedTaskIds.contains(it.id))
        }
        val bonus = tasks.filter { it.tier == TaskTier.BONUS }.map {
            TaskWithCompletion(it, completedTaskIds.contains(it.id))
        }

        // Calculate streak and rate
        val wajibTaskIds = tasks.filter { it.tier == TaskTier.WAJIB }.map { it.id }
        val (streak, showWarning) = calculateStreakAndWarning(wajibTaskIds, allCompletions, currentDate)
        val rate = calculateRate(wajibTaskIds, allCompletions, startDate, currentDate)

        // Initialize journal input if not done
        if (jInput.isEmpty() && journal?.text?.isNotEmpty() == true) {
            journalInput.value = journal.text
        }

        TodayUiState(
            isLoading = false,
            currentDate = currentDate,
            dayNumber = dayNumber,
            periodLengthDays = periodLengthDays,
            streak = streak,
            completionRate = rate,
            wajibTasks = wajib,
            bonusTasks = bonus,
            journalEntry = if (jInput.isEmpty() && journal != null) journal.text else jInput,
            showWarning = showWarning
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    init {
        viewModelScope.launch {
            settingsRepository.initStartDateIfMissing()
        }
    }

    private fun calculateStreakAndWarning(wajibTaskIds: List<String>, completions: List<TaskCompletion>, today: LocalDate): Pair<Int, Boolean> {
        if (wajibTaskIds.isEmpty()) return Pair(0, false)
        
        val completionsByDate = completions.filter { it.completed }
            .groupBy { it.date }
            .mapValues { it.value.map { c -> c.taskId }.toSet() }

        var streak = 0
        var current = today
        var warning = false

        // Check if yesterday and day before were missed
        val yesterday = today.minusDays(1).toString()
        val dayBefore = today.minusDays(2).toString()
        
        val yesterdaySuccess = wajibTaskIds.all { completionsByDate[yesterday]?.contains(it) == true }
        val dayBeforeSuccess = wajibTaskIds.all { completionsByDate[dayBefore]?.contains(it) == true }
        
        if (!yesterdaySuccess && !dayBeforeSuccess) {
            warning = true
        }

        while (true) {
            val dateStr = current.toString()
            val completedToday = completionsByDate[dateStr] ?: emptySet()
            val isSuccess = wajibTaskIds.all { completedToday.contains(it) }

            if (isSuccess) {
                streak++
                current = current.minusDays(1)
            } else {
                if (current == today) {
                    current = current.minusDays(1)
                } else {
                    break
                }
            }
        }
        return Pair(streak, warning)
    }

    private fun calculateRate(wajibTaskIds: List<String>, completions: List<TaskCompletion>, start: LocalDate, today: LocalDate): Float {
        if (wajibTaskIds.isEmpty()) return 0f
        val totalDays = ChronoUnit.DAYS.between(start, today) + 1
        if (totalDays <= 0) return 0f

        val completionsByDate = completions.filter { it.completed }
            .groupBy { it.date }
            .mapValues { it.value.map { c -> c.taskId }.toSet() }

        var successDays = 0
        for (i in 0 until totalDays) {
            val d = start.plusDays(i).toString()
            val completed = completionsByDate[d] ?: emptySet()
            if (wajibTaskIds.all { completed.contains(it) }) {
                successDays++
            }
        }

        return (successDays.toFloat() / totalDays.toFloat()) * 100f
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(taskId, todayStr, completed)
        }
    }

    fun updateJournal(text: String) {
        journalInput.value = text
    }

    fun saveJournal() {
        viewModelScope.launch {
            repository.insertJournalEntry(
                JournalEntry(
                    id = "j_${todayStr}",
                    date = todayStr,
                    text = journalInput.value
                )
            )
        }
    }
}

class TodayViewModelFactory(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodayViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
