---
phase: 4
title: "Entry Editor"
status: pending
priority: P1
effort: "1d"
dependencies: [3]
---

# Phase 4: Entry Editor

## Overview

The write-side UI: one editor screen for both creating a new entry and editing
an existing one. Users type title + body and add/remove images via the system
photo picker. Saving imports new images and persists through the repository,
then returns to the list/detail. This closes the create→persist→display loop.

## Requirements

- Functional: `editor` (no id) creates; `editor?entryId={id}` loads and edits.
- Functional: title + multi-line plain-text body fields.
- Functional: add images via `PickMultipleVisualMedia` (Photo Picker — no storage permission needed on API 26+ via `ActivityResultContracts`); remove staged images before save.
- Functional: save builds an `EntryDraft` and calls `repository.upsertEntry`; on success navigate back.
- Functional: discard/back with unsaved changes prompts confirmation.
- Non-functional: image picking uses the Android Photo Picker (`PickVisualMedia`/`PickMultipleVisualMedia`) — no `READ_MEDIA_*` / `READ_EXTERNAL_STORAGE` permission in the manifest.

## Architecture

### ViewModel

`EntryEditorViewModel(repository, entryId: Long?)`:
- Holds `StateFlow<EditorUiState>` = `{ title, body, images: List<StagedImage>, isSaving, canSave }`.
- `StagedImage` = existing stored `path` OR newly-picked `Uri` not yet imported.
- On init with `entryId`: load via `observeEntry(id).first()` to seed fields (existing images become `StagedImage.Stored`).
- Intents: `onTitleChange`, `onBodyChange`, `onImagesPicked(List<Uri>)`, `onRemoveImage(index)`, `onSave()`.
- `onSave`: assemble `EntryDraft(id=entryId, title, body, images=stagedRefs)` → `repository.upsertEntry` (imports new URIs, reconciles removed) → emit saved event.

### Composables

- `EntryEditorScreen`: `TopAppBar` (back + Save action, Save disabled while `!canSave` or `isSaving`), `OutlinedTextField` title, multi-line body field, a staged-image grid with per-image remove (X), and an "Add photos" button launching the picker via `rememberLauncherForActivityResult(PickMultipleVisualMedia())`.
- Back/discard: if dirty, `AlertDialog` confirm.

### Save-and-return

Editor emits a one-shot event (`Channel`/`SharedFlow`) the screen collects to
`navController.popBackStack()`. The list is already reactive (Phase 3), so the
new/updated entry appears without manual refresh.

## Related Code Files

- Modify: `ui/navigation/DiaryNavHost.kt` — implement the `editor?entryId=` route (was placeholder in Phase 3)
- Create: `ui/entry/editor/EntryEditorScreen.kt`, `EntryEditorViewModel.kt`
- Create: `ui/entry/editor/StagedImage.kt`
- Modify: `data/model/EntryDraft.kt` if `ImageRef` needs a stored-vs-new variant (align with Phase 2 `EntryDraft`/`ImageRef` definition — do not introduce a second shape)
- Remove: any temporary debug seed added in Phase 3

## Implementation Steps

1. Implement `EntryEditorViewModel` create/edit modes with `EditorUiState` and intents.
2. Wire the Photo Picker launcher (`PickMultipleVisualMedia`), append picked URIs as `StagedImage.New`.
3. Build `EntryEditorScreen`: title/body fields, staged-image grid with remove, add-photos button, Save.
4. Implement `onSave` → `EntryDraft` → `repository.upsertEntry`; emit saved event → pop back.
5. Implement dirty-check + discard confirmation dialog.
6. Implement the real `editor` route in `DiaryNavHost`; connect list FAB (create) and detail/list edit (edit).
7. Remove the Phase 3 debug seed now that entries are creatable through the UI.
8. Manual UI verification of full loop (see Success Criteria); Phase 5 does the durability/orphan verification.

## Success Criteria

- [x] From FAB: create an entry with title, body, 2 images → appears in list newest-first with thumbnail.
- [x] Open it in detail → text + both images render.
- [x] Edit it: change body, remove 1 image, add 1 → detail reflects all three changes.
- [x] Save disabled while a save is in flight; no duplicate rows on double-tap.
- [x] Back with unsaved changes prompts confirm; discard leaves data unchanged.
- [x] No media/storage permission appears in the manifest (Photo Picker only).

## Risk Assessment

- **`EntryDraft`/`ImageRef` shape mismatch with Phase 2** — editor and repository must agree on the draft contract. *Signal:* compile error or a second draft type appears. *Response:* reuse the exact Phase 2 `EntryDraft`/`ImageRef`; if it needs a stored-vs-new distinction, amend the Phase 2 definition once, don't fork it.
- **Double-save creates duplicates** — rapid Save taps. *Signal:* two rows for one entry. *Response:* gate on `isSaving`; disable Save during the suspend call.
- **Assumption:** Photo Picker covers image selection on all target devices. *Breaks when:* an API 26–29 device lacks the modern picker backport. *Signal:* picker fails to launch in testing. *Response:* `PickVisualMedia` has a compat backport via `ActivityResultContracts`; if a device still fails, fall back to `GetMultipleContents` (still permission-free) — decide at test time.
