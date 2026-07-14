package com.fazli.momentum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Pillar::class,
        Task::class,
        TaskCompletion::class,
        ProgressCounter::class,
        WeeklyReview::class,
        Milestone::class,
        JournalEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pillarDao(): PillarDao
    abstract fun taskDao(): TaskDao
    abstract fun completionDao(): TaskCompletionDao
    abstract fun counterDao(): ProgressCounterDao
    abstract fun reviewDao(): WeeklyReviewDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun journalDao(): JournalEntryDao

    companion object {
        const val DATABASE_NAME = "momentum_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                // Seed data on first launch
                                database.pillarDao().insertPillars(DatabaseSeeder.getPreloadedPillars())
                                database.taskDao().insertTasks(DatabaseSeeder.getPreloadedTasks())
                                database.counterDao().insertCounters(DatabaseSeeder.getPreloadedProgressCounters())
                                database.milestoneDao().insertMilestones(DatabaseSeeder.getPreloadedMilestones())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
