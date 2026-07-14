package com.fazli.momentum.ui.screens

import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskRecurrence
import com.fazli.momentum.data.TaskTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanViewModelTest {

    private fun task(
        id: String,
        recurrence: TaskRecurrence,
        daysOfWeek: String? = null
    ) = Task(
        id = id,
        title = id,
        description = "",
        pillarId = "tubuh",
        tier = TaskTier.WAJIB,
        recurrence = recurrence,
        targetMinutes = null,
        daysOfWeek = daysOfWeek,
        active = true,
        order = 1,
        createdAt = 0
    )

    @Test
    fun dailyTaskAlwaysAppears() {
        val tasks = listOf(task("t1", TaskRecurrence.DAILY))
        val result = tasksForDate(tasks, LocalDate.of(2026, 7, 15)) // Wednesday
        assertEquals(1, result.size)
    }

    @Test
    fun weeklyTaskAppearsOnlyOnMatchingDay() {
        // 2026-07-15 is a Wednesday (ISO day 3)
        val wednesday = task("t_wed", TaskRecurrence.WEEKLY, daysOfWeek = "3")
        val monday = task("t_mon", TaskRecurrence.WEEKLY, daysOfWeek = "1")
        val result = tasksForDate(listOf(wednesday, monday), LocalDate.of(2026, 7, 15))
        assertEquals(listOf("t_wed"), result.map { it.id })
    }

    @Test
    fun weeklyTaskWithMultipleDays() {
        val t = task("t_multi", TaskRecurrence.WEEKLY, daysOfWeek = "1,3,5")
        assertTrue(tasksForDate(listOf(t), LocalDate.of(2026, 7, 15)).isNotEmpty()) // Wed
        assertTrue(tasksForDate(listOf(t), LocalDate.of(2026, 7, 16)).isEmpty()) // Thu
    }

    @Test
    fun monthlyAndOnceAlwaysAppear() {
        val monthly = task("t_monthly", TaskRecurrence.MONTHLY)
        val once = task("t_once", TaskRecurrence.ONCE)
        val result = tasksForDate(listOf(monthly, once), LocalDate.of(2026, 7, 15))
        assertEquals(2, result.size)
    }
}
