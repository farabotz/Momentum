package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey val id: String,
    val month: Int,
    val title: String,
    val description: String,
    val done: Boolean,
    val targetDate: String? // ISO-8601 (YYYY-MM-DD)
)
