package com.example.diary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single diary entry. Multiple entries per calendar day are allowed. */
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)