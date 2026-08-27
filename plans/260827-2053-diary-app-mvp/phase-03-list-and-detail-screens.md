---
phase: 3
title: "List and Detail Screens"
status: pending
priority: P1
effort: "1d"
dependencies: [2]
---

# Phase 3: List and Detail Screens

## Overview

The read-side UI: a home screen listing entries newest-first grouped by date
with a thumbnail + text preview, and a detail screen rendering a single entry's
full text and image gallery. Navigation wiring the editor (Phase 4) will target.

## Requirements

- Functional: list shows all entries, newest-first, grouped by calendar date header.
- Functional: each row shows title (or a dash if empty), a body snippet, entry time, and first-image thumbnail when present.
- Functional: tapping a row opens the detail screen for that entry id.
- Functional: detail renders full title, body, and image gallery; empty state when no images.
- Functional: list empty state ("No entries yet") + a FAB to create (route stubbed to Phase 4).
- Non-functional: UI is reactive — driven by `repository.observeEntries()` / `observeEntry(id)` via `StateFlow`.

## Architecture

### Navigation

Extend `DiaryNavHost` with routes:
- `list` (start) → `EntryListScreen`
- `detail/{entryId}` → `EntryDetailScreen`
- `editor?entryId={entryId}` → placeholder now; Phase 4 implements. FAB and row-edit navigate here.

### ViewModels

- `EntryListViewModel(repository)`: exposes `StateFlow<EntryListUiState>` collected from `observeEntries()`, mapped into date-grouped sections. `UiState = Loading | Empty | Data(sections)`.
- `EntryDetailViewModel(repository, entryId)`: `StateFlow<DiaryEntry?>` from `observeEntry(id)`; expose a `delete()` that calls `repository.deleteEntry` then signals nav-back (delete UI lands here; file-cleanup correctness is Phase 5).

Both built via a factory reading the repository from `DiaryApp`.

### Composables

- `EntryListScreen`: `LazyColumn` with sticky date headers, `EntryRow` items, `FloatingActionButton` (+) → `editor`. Thumbnail via Coil `AsyncImage` from `ImageStorage.fileFor(path)`.
- `EntryDetailScreen`: `TopAppBar` with back + edit + delete actions; body `Text`; image gallery (`LazyRow` or grid) of `AsyncImage`. Tapping edit → `editor?entryId=`.
- Date grouping/formatting helper (`java.time.LocalDate` from `createdAt`, `ZoneId.systemDefault()`).

## Related Code Files

- Modify: `ui/navigation/DiaryNavHost.kt` (add list/detail/editor routes)
- Create: `ui/entry/list/EntryListScreen.kt`, `EntryListViewModel.kt`, `EntryRow.kt`
- Create: `ui/entry/detail/EntryDetailScreen.kt`, `EntryDetailViewModel.kt`
- Create: `ui/entry/ImageThumbnail.kt` (shared Coil AsyncImage wrapper using ImageStorage)
- Create: `ui/common/DateFormatting.kt`
- Create: `ui/ViewModelFactory.kt` (or per-VM factories) reading repository from DiaryApp

## Implementation Steps

1. Add a `ViewModelProvider.Factory` (or `viewModel { }` initializer) that supplies the repository from `DiaryApp`.
2. `EntryListViewModel`: collect `observeEntries()`, map to date-grouped `UiState`.
3. `EntryListScreen`: LazyColumn + sticky date headers + `EntryRow` + FAB. Loading/empty states.
4. `ImageThumbnail`: Coil `AsyncImage` resolving relative path → `File` via `ImageStorage.fileFor`.
5. `EntryDetailViewModel` + `EntryDetailScreen`: full text + gallery, top-bar edit/delete/back.
6. Wire nav routes; FAB → `editor` (no entryId), row-edit → `editor?entryId=`; back navigation from detail/editor.
7. Verify with the actual UI on an emulator using seeded data (a temporary debug seed or a Room prepopulate) since the editor doesn't exist yet.

## Success Criteria

- [x] With seeded entries, list renders newest-first, grouped by date, with thumbnails and previews.
- [x] Empty DB shows the "No entries yet" empty state + FAB.
- [x] Tapping a row opens detail with correct full text and all images.
- [x] Detail edit/delete/back actions navigate correctly (delete removes and returns to list).
- [x] Rotating the device preserves scroll/state (ViewModel-backed).

## Risk Assessment

- **No editor yet to create data** — this phase can't create entries through the UI. *Signal:* nothing to display. *Response:* verify with a temporary debug seed (removed in Phase 4/5) or Room prepopulate; do not fake the list contents.
- **Thumbnail path resolution** — Coil needs a `File`/`Uri`, not a bare relative string. *Signal:* blank thumbnails. *Response:* always resolve through `ImageStorage.fileFor`; centralize in `ImageThumbnail`.
- **Assumption:** date-grouped `LazyColumn` performs fine. *Breaks when:* thousands of entries. *Signal:* scroll jank in testing. *Response:* acceptable for MVP volume; paging is a future concern, not built now.
