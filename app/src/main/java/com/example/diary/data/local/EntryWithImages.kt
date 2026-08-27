package com.example.diary.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** One-to-many read shape: an entry with its images (sorted in the mapping). */
data class EntryWithImages(
    @Embedded val entry: EntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val images: List<EntryImageEntity> = emptyList(),
)