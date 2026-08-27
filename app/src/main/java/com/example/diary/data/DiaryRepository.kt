package com.example.diary.data

import com.example.diary.data.model.DiaryEntry
import com.example.diary.data.model.EntryDraft
import kotlinx.coroutines.flow.Flow

/**
 * The single data seam. Phase 1-5 ship [LocalDiaryRepository]; a future
 * Firebase phase adds a remote source behind the same interface so UI and
 * ViewModels stay untouched.
 */
interface DiaryRepository {
    /** All entries newest-first, each with its images. Reactive. */
    fun observeEntries(): Flow<List<DiaryEntry>>

    /** One entry (or null) with its images. Reactive. */
    fun observeEntry(id: Long): Flow<DiaryEntry?>

    /** Create (draft.id == null) or update (draft.id set). Returns the row id. */
    suspend fun upsertEntry(draft: EntryDraft): Long

    /** Deletes the entry row (cascade images) and its image files. */
    suspend fun deleteEntry(id: Long)
}