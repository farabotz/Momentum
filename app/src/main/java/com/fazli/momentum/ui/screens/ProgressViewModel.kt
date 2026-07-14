package com.fazli.momentum.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fazli.momentum.data.Milestone
import com.fazli.momentum.data.MomentumRepository
import com.fazli.momentum.data.ProgressCounter
import com.fazli.momentum.data.SettingsRepository
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskCompletion
import com.fazli.momentum.data.TaskTier
import com.fazli.momentum.data.WeeklyReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val TRACKED_BONUS_IDS = mapOf(
    "t_gym_session" to "Gym/workout",
    "t_presentation_rep" to "Presentation rep",
    "t_refleksi_karakter" to "Refleksi karakter",
    "t_kabar_ortu" to "Kabar orang tua",
    "t_hunting_client" to "Hunting/income"
)

data class BonusTally(val label: String, val count: Int)

data class ProgressUiState(
    val isLoading: Boolean = true,
    val counters: List<ProgressCounter> = emptyList(),
    val milestonesByMonth: Map<Int, List<Milestone>> = emptyMap(),
    val reviews: List<WeeklyReview> = emptyList(),
    val totalSuccessDays: Int = 0,
    val overallRate: Float = 0f,
    val longestStreak: Int = 0,
    val bonusTallies: List<BonusTally> = emptyList()
)

class ProgressViewModel(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        repository.getCounters(),
        repository.getAllCompletions(),
        repository.getMilestones(),
        repository.getReviews(),
        repository.getActiveTasks(),
        settingsRepository.startDateFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST") val counters = values[0] as List<ProgressCounter>
        @Suppress("UNCHECKED_CAST") val completions = values[1] as List<TaskCompletion>
        @Suppress("UNCHECKED_CAST") val milestones = values[2] as List<Milestone>
        @Suppress("UNCHECKED_CAST") val reviews = values[3] as List<WeeklyReview>
        @Suppress("UNCHECKED_CAST") val tasks = values[4] as List<Task>
        val startDateStr = values[5] as String

        val startDate = LocalDate.parse(startDateStr)
        val today = LocalDate.now()
        val totalDays = (ChronoUnit.DAYS.between(startDate, today) + 1).coerceAtLeast(0)

        val wajibTaskIds = tasks.filter { it.tier == TaskTier.WAJIB }.map { it.id }
        val completedByDate = completions.filter { it.completed }
            .groupBy { it.date }
            .mapValues { it.value.map { c -> c.taskId }.toSet() }

        var successDays = 0
        var longestStreak = 0
        var currentRun = 0
        for (i in 0 until totalDays) {
            val d = startDate.plusDays(i).toString()
            val done = completedByDate[d] ?: emptySet()
            val success = wajibTaskIds.isNotEmpty() && wajibTaskIds.all { done.contains(it) }
            if (success) {
                successDays++
                currentRun++
                if (currentRun > longestStreak) longestStreak = currentRun
            } else {
                currentRun = 0
            }
        }
        val rate = if (totalDays > 0) (successDays.toFloat() / totalDays.toFloat()) * 100f else 0f

        val bonusTallies = TRACKED_BONUS_IDS.map { (id, label) ->
            BonusTally(label, completions.count { it.taskId == id && it.completed })
        }

        ProgressUiState(
            isLoading = false,
            counters = counters,
            milestonesByMonth = milestones.groupBy { it.month }.toSortedMap(),
            reviews = reviews,
            totalSuccessDays = successDays,
            overallRate = rate,
            longestStreak = longestStreak,
            bonusTallies = bonusTallies
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    fun stepCounter(counter: ProgressCounter, delta: Int) {
        val newValue = (counter.currentValue + delta).coerceAtLeast(0)
        viewModelScope.launch { repository.updateCounterValue(counter.id, newValue) }
    }

    fun setCounterValue(id: String, value: Int) {
        viewModelScope.launch { repository.updateCounterValue(id, value.coerceAtLeast(0)) }
    }

    fun toggleMilestone(id: String, done: Boolean) {
        viewModelScope.launch { repository.updateMilestoneDone(id, done) }
    }

    fun submitReview(win: String, struggle: String, adjust: String) {
        val weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        viewModelScope.launch {
            repository.insertReview(
                WeeklyReview(
                    id = "wr_${weekStart}",
                    weekStartDate = weekStart.toString(),
                    win = win,
                    struggle = struggle,
                    adjust = adjust,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}

class ProgressViewModelFactory(
    private val repository: MomentumRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
