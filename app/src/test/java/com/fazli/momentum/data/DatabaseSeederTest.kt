package com.fazli.momentum.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseSeederTest {

    @Test
    fun testPillarsSeeding() {
        val pillars = DatabaseSeeder.getPreloadedPillars()
        assertEquals(3, pillars.size)
        assertTrue(pillars.any { it.id == "tubuh" })
        assertTrue(pillars.any { it.id == "cyber" })
        assertTrue(pillars.any { it.id == "diri" })
    }

    @Test
    fun testTasksSeeding() {
        val tasks = DatabaseSeeder.getPreloadedTasks()
        assertEquals(21, tasks.size) // 11 tubuh, 5 cyber, 5 diri
        assertEquals(3, tasks.count { it.tier == TaskTier.WAJIB }) // 1 wajib per pilar
        assertTrue(tasks.all { it.title.isNotEmpty() })
    }

    @Test
    fun testMilestonesSeeding() {
        val milestones = DatabaseSeeder.getPreloadedMilestones()
        assertEquals(9, milestones.size)
        assertTrue(milestones.all { it.month in 1..3 })
    }

    @Test
    fun testProgressCountersSeeding() {
        val counters = DatabaseSeeder.getPreloadedProgressCounters()
        assertEquals(6, counters.size)
        assertTrue(counters.any { it.id == "c_portswigger" })
    }
}
