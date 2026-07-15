package com.fazli.momentum.data

import kotlinx.coroutines.flow.Flow

class MomentumRepository(
    private val pillarDao: PillarDao,
    private val taskDao: TaskDao,
    private val completionDao: TaskCompletionDao,
    private val counterDao: ProgressCounterDao,
    private val reviewDao: WeeklyReviewDao,
    private val milestoneDao: MilestoneDao,
    private val journalDao: JournalEntryDao
) {
    fun getPillars(): Flow<List<Pillar>> = pillarDao.getPillarsFlow()
    suspend fun getPillarsList(): List<Pillar> = pillarDao.getPillars()
    suspend fun insertPillars(pillars: List<Pillar>) = pillarDao.insertPillars(pillars)
    suspend fun insertPillar(pillar: Pillar) = pillarDao.insertPillars(listOf(pillar))
    suspend fun deletePillar(pillar: Pillar) = pillarDao.deletePillar(pillar)

    fun getTasks(): Flow<List<Task>> = taskDao.getTasksFlow()
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasksFlow()
    suspend fun getTasksList(): List<Task> = taskDao.getTasks()
    suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)
    suspend fun insertTasks(tasks: List<Task>) = taskDao.insertTasks(tasks)
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    fun getCompletionsForDate(date: String): Flow<List<TaskCompletion>> = completionDao.getCompletionsForDateFlow(date)
    suspend fun getCompletionsForDateList(date: String): List<TaskCompletion> = completionDao.getCompletionsForDate(date)
    fun getAllCompletions(): Flow<List<TaskCompletion>> = completionDao.getAllCompletionsFlow()
    suspend fun getAllCompletionsList(): List<TaskCompletion> = completionDao.getAllCompletions()
    suspend fun insertCompletions(completions: List<TaskCompletion>) = completionDao.insertCompletions(completions)
    suspend fun toggleCompletion(taskId: String, date: String, completed: Boolean, note: String? = null) {
        if (completed) {
            completionDao.insertCompletion(
                TaskCompletion(
                    id = "${taskId}_$date",
                    taskId = taskId,
                    date = date,
                    completed = true,
                    note = note
                )
            )
        } else {
            completionDao.deleteCompletion(taskId, date)
        }
    }

    fun getCounters(): Flow<List<ProgressCounter>> = counterDao.getCountersFlow()
    suspend fun getCountersList(): List<ProgressCounter> = counterDao.getCounters()
    suspend fun insertCounters(counters: List<ProgressCounter>) = counterDao.insertCounters(counters)
    suspend fun updateCounterValue(id: String, value: Int) = counterDao.updateValue(id, value)

    fun getReviews(): Flow<List<WeeklyReview>> = reviewDao.getReviewsFlow()
    suspend fun getReviewsList(): List<WeeklyReview> = reviewDao.getReviews()
    suspend fun insertReview(review: WeeklyReview) = reviewDao.insertReview(review)
    suspend fun insertReviews(reviews: List<WeeklyReview>) = reviewDao.insertReviews(reviews)

    fun getMilestones(): Flow<List<Milestone>> = milestoneDao.getMilestonesFlow()
    suspend fun getMilestonesList(): List<Milestone> = milestoneDao.getMilestones()
    suspend fun insertMilestones(milestones: List<Milestone>) = milestoneDao.insertMilestones(milestones)
    suspend fun updateMilestoneDone(id: String, done: Boolean) = milestoneDao.updateDone(id, done)

    fun getJournalEntryForDate(date: String): Flow<JournalEntry?> = journalDao.getEntryForDateFlow(date)
    suspend fun getJournalEntryForDateValue(date: String): JournalEntry? = journalDao.getEntryForDate(date)
    suspend fun getJournalEntriesList(): List<JournalEntry> = journalDao.getAllEntries()
    suspend fun insertJournalEntry(entry: JournalEntry) = journalDao.insertEntry(entry)
    suspend fun insertJournalEntries(entries: List<JournalEntry>) = journalDao.insertEntries(entries)

    suspend fun resetProgress() {
        completionDao.deleteAll()
        reviewDao.deleteAll()
        journalDao.deleteAll()
        counterDao.resetAllValues()
        milestoneDao.resetAllDone()
    }
}
