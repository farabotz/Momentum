package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reviews")
data class WeeklyReview(
    @PrimaryKey val id: String,
    val weekStartDate: String, // ISO-8601 (YYYY-MM-DD)
    val win: String,
    val struggle: String,
    val adjust: String,
    val createdAt: Long
)
