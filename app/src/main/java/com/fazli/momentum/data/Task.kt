package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

enum class TaskTier { WAJIB, BONUS }
enum class TaskRecurrence { DAILY, WEEKLY, MONTHLY, ONCE }

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Pillar::class,
            parentColumns = ["id"],
            childColumns = ["pillarId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pillarId")]
)
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val pillarId: String,
    val tier: TaskTier,
    val recurrence: TaskRecurrence,
    val targetMinutes: Int?,
    val daysOfWeek: String?, // CSV of 1-7 (Monday-Sunday)
    val active: Boolean,
    val order: Int,
    val createdAt: Long
)
