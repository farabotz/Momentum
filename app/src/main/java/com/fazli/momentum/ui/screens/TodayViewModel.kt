package com.fazli.momentum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fazli.momentum.domain.calculateRate
import com.fazli.momentum.domain.calculateStreakAndWarning
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

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(taskId, todayStr, completed)
            com.fazli.momentum.widget.WidgetRefresher.refresh()
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
