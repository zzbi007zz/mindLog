package com.example.diary.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    /** All entries newest-first, each with its images. Reactive. */
    @Transaction
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeEntries(): Flow<List<EntryWithImages>>

    /** A single entry with its images, reactive. */
    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    fun observeEntry(id: Long): Flow<EntryWithImages?>

    @Insert
    suspend fun insertEntry(entry: EntryEntity): Long

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    @Query("DELETE FROM entry_images WHERE entryId = :entryId")
    suspend fun deleteImagesForEntry(entryId: Long)

    @Insert
    suspend fun insertImages(images: List<EntryImageEntity>)

    @Query("SELECT * FROM entry_images WHERE entryId = :entryId")
    suspend fun imagesForEntry(entryId: Long): List<EntryImageEntity>

    /** All referenced image paths across every entry (for the orphan sweep). */
    @Query("SELECT path FROM entry_images")
    suspend fun allImagePaths(): List<String>
    /** Bare row lookup (preserves createdAt on update). */
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntry(id: Long): EntryEntity?
}