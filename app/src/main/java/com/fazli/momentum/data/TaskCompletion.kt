package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("date", "taskId", unique = true)]
)
data class TaskCompletion(
    @PrimaryKey val id: String,
    val taskId: String,
    val date: String, // ISO-8601 (YYYY-MM-DD)
    val completed: Boolean,
    val note: String?
)
