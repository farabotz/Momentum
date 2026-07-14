package com.fazli.momentum.data

import androidx.room.RoomDatabase

/**
 * Fase 0 stub — no @Database annotation yet.
 * Entities and DAOs are added in Fase 1 (Data layer) per SPEC §4.
 * The Room dependency is wired here so the compile chain is verified;
 * the actual @Database declaration comes when we have at least one @Entity.
 *
 * ponytail: add @Database(entities=[...], version=1) in Fase 1 once entities exist.
 */
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "momentum_db"
    }
}
