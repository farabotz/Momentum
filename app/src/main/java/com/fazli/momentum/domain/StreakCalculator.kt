package com.fazli.momentum.domain

import com.fazli.momentum.data.TaskCompletion
import java.time.LocalDate

fun calculateStreakAndWarning(wajibTaskIds: List<String>, completions: List<TaskCompletion>, today: LocalDate): Pair<Int, Boolean> {
    if (wajibTaskIds.isEmpty()) return Pair(0, false)

    val completionsByDate = completions.filter { it.completed }
        .groupBy { it.date }
        .mapValues { it.value.map { c -> c.taskId }.toSet() }

    var streak = 0
    var current = today
    var warning = false

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

fun calculateRate(wajibTaskIds: List<String>, completions: List<TaskCompletion>, start: LocalDate, today: LocalDate): Float {
    if (wajibTaskIds.isEmpty()) return 0f
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, today) + 1
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
