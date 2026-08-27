---
phase: 2
title: "Data Layer"
status: pending
priority: P1
effort: "1d"
dependencies: [1]
---

# Phase 2: Data Layer

## Overview

The on-device persistence layer: Room schema for entries and their images,
app-private image file storage, and the `DiaryRepository` interface with its
`LocalDiaryRepository` implementation. This is the single future-proofing seam
and the foundation both UI phases consume.

## Requirements

- Functional: create, read (list + by id, reactive), update, delete entries with 0–N images.
- Functional: images stored as files in `filesDir/images/`; DB holds relative paths only.
- Non-functional: all reads exposed as `Flow`; all writes `suspend` on `Dispatchers.IO` (Room handles dispatch).
- Non-functional: deleting an entry deletes its image rows (FK cascade) — file deletion is coordinated in the repository (Phase 5 hardens the reconciliation).

## Architecture

### Schema (two tables, 1-to-many)

```
entries
  id            INTEGER PK autogenerate
  title         TEXT (may be empty)
  body          TEXT (plain text, may be empty)
  createdAt     INTEGER (epoch millis)   -- timestamp; multiple per day allowed
  updatedAt     INTEGER (epoch millis)

entry_images
  id            INTEGER PK autogenerate
  entryId       INTEGER FK → entries.id  ON DELETE CASCADE  (indexed)
  path          TEXT   -- relative path under filesDir, e.g. "images/uuid.jpg"
  position      INTEGER -- gallery order
```

Room types:
- `@Entity EntryEntity`, `@Entity(foreignKeys=…, indices=…) EntryImageEntity`.
- `data class EntryWithImages(@Embedded entry; @Relation images)` for reads.
- Enable `foreignKeys` + set `ON DELETE CASCADE` so image rows vanish with the entry. (Cascade covers DB rows; **files** are handled in the repository — see Phase 5.)

### Types crossing the repository boundary

Domain models (not Room entities) so the future remote source shares them:
`DiaryEntry(id, title, body, createdAt, updatedAt, images: List<DiaryImage>)`,
`DiaryImage(id, path, position)`. Mappers convert entity ↔ domain.

### ImageStorage

```kotlin
interface ImageStorage {
    /** Copies a picked content URI into filesDir/images, returns relative path. */
    suspend fun importImage(source: Uri): String
    /** Deletes the file at a relative path; no-op if missing. */
    suspend fun deleteImage(relativePath: String): Unit
    fun fileFor(relativePath: String): File
}
```
`LocalImageStorage(context)` implements via `contentResolver.openInputStream` →
copy to `File(filesDir, "images/${UUID}.jpg")`. Runs on `Dispatchers.IO`.

### Repository

```kotlin
interface DiaryRepository {
    fun observeEntries(): Flow<List<DiaryEntry>>          // newest-first
    fun observeEntry(id: Long): Flow<DiaryEntry?>
    suspend fun upsertEntry(draft: EntryDraft): Long      // create or update
    suspend fun deleteEntry(id: Long)
}
```
`EntryDraft(id: Long?, title, body, images: List<ImageRef>)` where `ImageRef` is
either an already-stored path or a newly-picked `Uri` to import. `upsertEntry`
imports new URIs via `ImageStorage`, reconciles removed images (delete files for
paths no longer present), writes rows in a Room `@Transaction`.

## Related Code Files

- Create: `data/local/EntryEntity.kt`, `EntryImageEntity.kt`, `EntryWithImages.kt`
- Create: `data/local/EntryDao.kt`, `DiaryDatabase.kt`
- Create: `data/ImageStorage.kt` (+ `LocalImageStorage.kt`)
- Create: `data/DiaryRepository.kt` (interface), `LocalDiaryRepository.kt`
- Create: `data/model/DiaryEntry.kt`, `DiaryImage.kt`, `EntryDraft.kt`
- Create: `data/mapper/EntryMappers.kt`
- Modify: `DiaryApp.kt` — construct `DiaryDatabase`, `LocalImageStorage`, `LocalDiaryRepository`.

## Implementation Steps

1. Add Room entities + `EntryWithImages` relation.
2. `EntryDao`: `@Query observeEntries()` (ORDER BY createdAt DESC) returning `Flow<List<EntryWithImages>>`; `observeEntry(id)`; `@Upsert` entry; `insert/delete images`; `@Delete` entry. Mark multi-write methods `@Transaction`.
3. `DiaryDatabase` (`@Database(version=1)`, exportSchema=true → `app/schema/`).
4. Domain models + mappers (entity ↔ domain).
5. `ImageStorage` + `LocalImageStorage` (copy-in, delete, `fileFor`).
6. `DiaryRepository` + `LocalDiaryRepository` implementing upsert (import new URIs, reconcile removed paths, transactional row writes) and delete (delete files then row; Phase 5 hardens ordering/failure handling).
7. Wire singletons in `DiaryApp`.
8. Write instrumented Room tests (androidTest, in-memory DB) for CRUD + cascade.

## Success Criteria

- [x] Instrumented test: insert entry with 2 images → `observeEntries` emits it with 2 images, newest-first.
- [x] Instrumented test: update entry removing 1 image → row gone AND file deleted.
- [x] Instrumented test: delete entry → entry + image rows gone (cascade).
- [x] `importImage` copies bytes into `filesDir/images/` and returns a working relative path.
- [x] Schema JSON exported under `app/schema/`.

## Risk Assessment

- **Orphaned image files** — cascade deletes DB rows but never touches files; the repository must delete files explicitly. *Signal:* files linger in `filesDir/images/` after delete/edit. *Response:* repository owns file lifecycle; Phase 5 adds a startup orphan sweep as backstop.
- **Assumption:** copying full-size images into filesDir is acceptable for MVP. *Breaks when:* users add many large photos (storage bloat). *Signal:* app storage grows fast in manual testing. *Response:* acceptable for MVP; downscale-on-import is a fast follow, noted not built.
- **URI permission loss** — a picked `content://` URI is only readable briefly. *Signal:* `openInputStream` throws SecurityException if read is deferred. *Response:* import (copy) synchronously within `upsertEntry`, never persist the raw URI.
