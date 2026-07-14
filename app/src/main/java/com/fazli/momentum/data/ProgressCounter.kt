package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_counters")
data class ProgressCounter(
    @PrimaryKey val id: String,
    val label: String,
    val currentValue: Int,
    val targetValue: Int?,
    val order: Int
)
