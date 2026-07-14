package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String,
    val date: String, // ISO-8601 (YYYY-MM-DD)
    val text: String
)
