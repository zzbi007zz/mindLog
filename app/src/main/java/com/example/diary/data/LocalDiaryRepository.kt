package com.example.diary.data

import androidx.room.Transaction
import com.example.diary.data.local.EntryDao
import com.example.diary.data.local.EntryEntity
import com.example.diary.data.local.EntryImageEntity
import com.example.diary.data.mapper.toDomain
import com.example.diary.data.model.DiaryEntry
import com.example.diary.data.model.EntryDraft
import com.example.diary.data.model.ImageRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDiaryRepository(
    private val dao: EntryDao,
    private val imageStorage: ImageStorage,
) : DiaryRepository {

    override fun observeEntries(): Flow<List<DiaryEntry>> =
        dao.observeEntries().map { rows -> rows.map { it.toDomain() } }

    override fun observeEntry(id: Long): Flow<DiaryEntry?> =
        dao.observeEntry(id).map { it?.toDomain() }

    override suspend fun upsertEntry(draft: EntryDraft): Long {
        val now = System.currentTimeMillis()

        // Import newly-picked images up front so a picked URI is never persisted.
        val storedPaths = draft.images.map { ref ->
            when (ref) {
                is ImageRef.Stored -> ref.path
                is ImageRef.New -> imageStorage.importImage(ref.uri)
            }
        }

        return if (draft.id == null) {
            val id = dao.insertEntry(
                EntryEntity(
                    title = draft.title,
                    body = draft.body,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            insertImagesFor(id, storedPaths)
            id
        } else {
            upsertExisting(draft.id, draft.title, draft.body, storedPaths, now)
        }
    }

    override suspend fun deleteEntry(id: Long) {
        val rows = dao.imagesForEntry(id)
        dao.deleteEntry(EntryEntity(id = id, title = "", body = "", createdAt = 0, updatedAt = 0))
        // Delete owned image files best-effort, after the DB delete (Phase 5
        // layers a startup orphan sweep as the backstop).
        rows.map { it.path }.forEach { imageStorage.deleteImage(it) }
    }

    @Transaction
    private suspend fun upsertExisting(
        id: Long,
        title: String,
        body: String,
        storedPaths: List<String>,
        now: Long,
    ): Long {
        val createdAt = dao.getEntry(id)?.createdAt ?: now
        dao.updateEntry(
            EntryEntity(
                id = id,
                title = title,
                body = body,
                createdAt = createdAt,
                updatedAt = now,
            )
        )

        // Reconcile image rows: drop removed paths (and their files), keep the
        // stored references, rewrite positions deterministically.
        val oldPaths = dao.imagesForEntry(id).map { it.path }.toSet()
        val keptPaths = storedPaths.toSet()
        val removed = oldPaths - keptPaths

        dao.deleteImagesForEntry(id)
        insertImagesFor(id, storedPaths)
        removed.forEach { imageStorage.deleteImage(it) }
        return id
    }

    @Transaction
    private suspend fun insertImagesFor(entryId: Long, storedPaths: List<String>) {
        dao.insertImages(
            storedPaths.mapIndexed { index, path ->
                EntryImageEntity(entryId = entryId, path = path, position = index)
            }
        )
    }
}