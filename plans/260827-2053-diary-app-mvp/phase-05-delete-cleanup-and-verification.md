---
phase: 5
title: "Delete Cleanup and Verification"
status: pending
priority: P1
effort: "0.5d"
dependencies: [4]
---

# Phase 5: Delete Cleanup and Verification

## Overview

Harden image-file lifecycle so delete and edit never leave orphaned files, add a
startup orphan sweep as a backstop, then run the full end-to-end verification
matrix that proves every plan-level success criterion on a real emulator/device.

## Requirements

- Functional: deleting an entry deletes its row (cascade) AND all its image files.
- Functional: editing an entry that removes an image deletes that file.
- Functional: a startup sweep deletes any file in `filesDir/images/` not referenced by a row (self-heals crashes/partial writes).
- Non-functional: the full verification matrix passes on the provisioned API 36 emulator (`diary_api36`).
- Non-functional: confirmed zero network — no INTERNET permission, verified in airplane mode.

## Architecture

### Deletion ordering (repository)

In `LocalDiaryRepository.deleteEntry(id)`:
1. Read the entry's image paths (before deleting the row).
2. Delete the row (cascade removes `entry_images` rows).
3. Delete each image file via `ImageStorage.deleteImage(path)` (best-effort, no-op if missing).

For edit-remove reconciliation (already in `upsertEntry` from Phase 2): compute
`removedPaths = old.paths − new.paths`, delete those files after the transaction
commits.

### Orphan sweep

`ImageStorage.sweepOrphans(referencedPaths: Set<String>)`: list `filesDir/images/`,
delete any file whose relative path is not in `referencedPaths`. Call once on
app start from `DiaryApp` (launch a coroutine reading all referenced paths from
the DAO, then sweep). Cheap for MVP volume; backstops any missed deletion.

## Related Code Files

- Modify: `data/LocalDiaryRepository.kt` — finalize delete ordering + edit reconciliation
- Modify: `data/ImageStorage.kt` / `LocalImageStorage.kt` — add `sweepOrphans`
- Modify: `data/local/EntryDao.kt` — add a query returning all referenced image paths
- Modify: `DiaryApp.kt` — invoke `sweepOrphans` on startup (coroutine)
- Create: androidTest for delete/edit file cleanup + orphan sweep

## Implementation Steps

1. Finalize `deleteEntry` ordering (capture paths → delete row → delete files).
2. Confirm `upsertEntry` deletes files for removed images (from Phase 2); add a test.
3. Add DAO `getAllImagePaths()`; implement `sweepOrphans`; call from `DiaryApp` start.
4. Instrumented tests: delete removes files; edit-remove deletes file; sweep removes an unreferenced file and keeps referenced ones.
5. Run the full verification matrix (below) on an emulator; fix any failures.
6. Update `README.md` with build/run instructions and the offline/on-device guarantee.

## Verification Matrix (proves plan Success Criteria)

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Create entry (title+body+2 images) → kill process → relaunch | Entry + both images intact |
| 2 | List with entries across ≥2 dates | Newest-first, grouped by date, thumbnails shown |
| 3 | Open detail | Full text + all images render |
| 4 | Edit: change body, remove 1 image, add 1 | Detail shows all changes; removed file gone from filesDir |
| 5 | Delete entry | Row gone; all its image files gone (no orphans) |
| 6 | Add image, force-stop source gallery app, reopen entry | Image still renders (proves copy-in, not borrowed URI) |
| 7 | Enable airplane mode, use whole app | Everything works; no crashes |
| 8 | Inspect manifest | No INTERNET / media / storage permission |
| 9 | Manually drop a stray file in filesDir/images, restart app | Stray file swept away |

## Success Criteria

- [x] All 9 verification-matrix rows pass on the `diary_api36` emulator.
- [x] Instrumented tests for delete cleanup, edit-remove cleanup, and orphan sweep pass.
- [x] No orphaned files remain after any delete/edit in manual testing.
- [x] `README.md` documents build/run + offline/on-device guarantees.
- [x] Every `plan.md` Success Criterion is checked and evidenced.

## Risk Assessment

- **File delete fails silently, row already gone** — leaves an orphan. *Signal:* file remains after delete. *Response:* the startup `sweepOrphans` is the backstop; delete is best-effort and idempotent.
- **Sweep races with an in-flight import** — a just-picked, not-yet-committed file could look unreferenced. *Signal:* newly added image vanishes. *Response:* run the sweep once at startup only (before the user can edit), not continuously; imports during a session are safe.
- **Assumption:** manual verification on one emulator represents the device fleet. *Breaks when:* OEM storage/photo-picker quirks differ. *Signal:* field reports post-release. *Response:* MVP ships on this bar; expand device coverage if issues surface — a follow-up, not built now.
- **Runtime min-SDK gap** — the plan verifies on an API 36 image; real API-26 behavior is untested. *Breaks when:* user targets pre-Android-12 devices. *Signal:* a future field report on an old device. *Response:* accepted for MVP (compile-time minSdk guard only); provisioning an API 26 image is the documented follow-up, not built now.
