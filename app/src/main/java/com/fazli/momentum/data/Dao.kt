package com.fazli.momentum.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PillarDao {
    @Query("SELECT * FROM pillars ORDER BY `order` ASC")
    fun getPillarsFlow(): Flow<List<Pillar>>

    @Query("SELECT * FROM pillars ORDER BY `order` ASC")
    suspend fun getPillars(): List<Pillar>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPillars(pillars: List<Pillar>)

    @Delete
    suspend fun deletePillar(pillar: Pillar)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY `order` ASC")
    fun getTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE active = 1 ORDER BY `order` ASC")
    fun getActiveTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE active = 1 ORDER BY `order` ASC")
    suspend fun getActiveTasks(): List<Task>

    @Query("SELECT * FROM tasks ORDER BY `order` ASC")
    suspend fun getTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completions WHERE date = :date")
    fun getCompletionsForDateFlow(date: String): Flow<List<TaskCompletion>>

    @Query("SELECT * FROM task_completions WHERE date = :date")
    suspend fun getCompletionsForDate(date: String): List<TaskCompletion>

    @Query("SELECT * FROM task_completions")
    fun getAllCompletionsFlow(): Flow<List<TaskCompletion>>

    @Query("SELECT * FROM task_completions")
    suspend fun getAllCompletions(): List<TaskCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<TaskCompletion>)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND date = :date")
    suspend fun deleteCompletion(taskId: String, date: String)

    @Query("DELETE FROM task_completions")
    suspend fun deleteAll()
}

@Dao
interface ProgressCounterDao {
    @Query("SELECT * FROM progress_counters ORDER BY `order` ASC")
    fun getCountersFlow(): Flow<List<ProgressCounter>>

    @Query("SELECT * FROM progress_counters ORDER BY `order` ASC")
    suspend fun getCounters(): List<ProgressCounter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounters(counters: List<ProgressCounter>)

    @Query("UPDATE progress_counters SET currentValue = :value WHERE id = :id")
    suspend fun updateValue(id: String, value: Int)

    @Query("UPDATE progress_counters SET currentValue = 0")
    suspend fun resetAllValues()
}

@Dao
interface WeeklyReviewDao {
    @Query("SELECT * FROM weekly_reviews ORDER BY createdAt DESC")
    fun getReviewsFlow(): Flow<List<WeeklyReview>>

    @Query("SELECT * FROM weekly_reviews ORDER BY createdAt DESC")
    suspend fun getReviews(): List<WeeklyReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: WeeklyReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<WeeklyReview>)

    @Query("DELETE FROM weekly_reviews")
    suspend fun deleteAll()
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY month ASC, id ASC")
    fun getMilestonesFlow(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones ORDER BY month ASC, id ASC")
    suspend fun getMilestones(): List<Milestone>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Query("UPDATE milestones SET done = :done WHERE id = :id")
    suspend fun updateDone(id: String, done: Boolean)

    @Query("UPDATE milestones SET done = 0")
    suspend fun resetAllDone()
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    fun getEntryForDateFlow(date: String): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): JournalEntry?

    @Query("SELECT * FROM journal_entries")
    suspend fun getAllEntries(): List<JournalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<JournalEntry>)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()
}
