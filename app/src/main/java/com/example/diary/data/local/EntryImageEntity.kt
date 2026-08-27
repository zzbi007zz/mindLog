package com.example.diary.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Gallery image belonging to an entry. `path` is relative to app filesDir
 * (e.g. "images/uuid.jpg") — the DB never stores image bytes. Deleting the
 * parent entry cascades the rows (`onDelete = CASCADE`); deleting the actual
 * files is the repository's job.
 */
@Entity(
    tableName = "entry_images",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("entryId")],
)
data class EntryImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val path: String,
    val position: Int,
)