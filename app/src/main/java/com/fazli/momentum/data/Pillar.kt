package com.fazli.momentum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pillars")
data class Pillar(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val colorKey: String,
    val order: Int
)
