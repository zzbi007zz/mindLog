---
title: "Diary App MVP"
description: "Native Android private, on-device diary: create/edit/browse/delete dated entries with text + image gallery. Offline-only. Repository-pattern data layer ready for a future Firebase-backed anonymous-social phase."
status: pending
priority: P1
effort: "3-5d"
tags: [android, kotlin, compose, room, offline, mvp]
created: 2026-08-27
blockedBy: []
blocks: []
---

# Diary App MVP

## Overview

A native Android app where a single user privately creates, edits, browses, and
deletes dated diary entries containing plain text and an optional gallery of
images. All data lives on-device (Room + app-private files); the app is fully
functional offline with zero network calls. The data layer sits behind a
`DiaryRepository` interface so a future phase can add a Firebase-backed remote
source (anonymous accounts + social interaction) without touching UI or
ViewModels.

**Stack:** Kotlin, Jetpack Compose, Room, DataStore, Coil, Navigation-Compose,
coroutines/Flow. Min SDK 26, target SDK 35. Single Gradle module.

**Settled decisions (from brainstorm):**
- MVP scope: private diary only — no accounts, backend, or social.
- Storage: on-device only (Room rows + app-private image files; DB stores paths, not blobs).
- Entry model: **multiple timestamped entries per day**, list grouped by date.
- Images: **0–N per entry**, shown as a gallery.
- Text: **plain text** body (no rich text / markdown in MVP).
- Future (out of scope): Firebase anonymous auth + Firestore/Storage behind a
  `RemoteDiarySource`, sync, social interactions, and a moderation/reporting budget.

## Goals

| # | Goal | Priority |
|---|------|----------|
| 1 | Create/edit/browse/delete diary entries with text + image gallery | P1 |
| 2 | 100% on-device, offline-first: zero network calls | P1 |
| 3 | Persist across app restarts; no orphaned image files after delete | P1 |
| 4 | Repository-pattern data layer that a future remote source drops into | P2 |
| 5 | Verified on emulator/device: create → kill → relaunch → intact | P1 |

## Phases

| # | Phase | Status |
|---|-------|--------|
| 1 | [Phase 1: Project Scaffold](./phase-01-start.md) | Pending |
| 2 | [Phase 2: Data Layer](./phase-02-data-layer.md) | Pending |
| 3 | [Phase 3: List and Detail Screens](./phase-03-list-and-detail-screens.md) | Pending |
| 4 | [Phase 4: Entry Editor](./phase-04-entry-editor.md) | Pending |
| 5 | [Phase 5: Delete Cleanup and Verification](./phase-05-delete-cleanup-and-verification.md) | Pending |

**Dependencies:** strictly sequential. 1 → 2 → 3 → 4 → 5. Phase 3 and 4 both
consume the Phase 2 repository; 3 before 4 so navigation targets exist when the
editor wires its save-and-return.

## Architecture

```
Compose UI (EntryListScreen / EntryDetailScreen / EntryEditorScreen)
    │  observes StateFlow, sends intents
    ▼
ViewModels (EntryListViewModel / EntryDetailViewModel / EntryEditorViewModel)
    │  suspend calls
    ▼
DiaryRepository  (interface)  ◄── the one future-proofing seam
    │
    ▼
LocalDiaryRepository (impl)
    ├── EntryDao (Room)                 → entries + entry_images tables
    └── ImageStorage                    → copy picked images into filesDir/images/
    │
    └┈┈ (future) RemoteDiaryRepository → Firebase, behind same interface
```

- Room stores image **file paths**, never bytes. Images load lazily via Coil.
- Picked images are **copied** into app-private `filesDir/images/` — the app
  never holds a borrowed external `content://` URI (they get revoked/deleted).
- Single source of truth: DAO exposes `Flow<List<...>>`; UI is reactive.

## Success Criteria

- [ ] Create an entry with title, body, and 0–N images; persists across restart.
- [ ] List shows entries newest-first, grouped by date, with preview + thumbnail.
- [ ] Detail screen renders full text + image gallery.
- [ ] Edit updates the entry; added/removed images reconcile on save.
- [ ] Delete removes the row AND its image files (no orphans in filesDir/images/).
- [ ] Adding an image copies it into app-private storage; works after the source app clears its cache.
- [ ] App runs fully in airplane mode — verified zero network permission in manifest.
- [ ] Manual check on the `diary_api36` emulator: create → kill process → relaunch → entry + images intact.

## Non-Goals

- Accounts, login, anonymous identity, backend, cloud sync, Firebase.
- Social features: posting, feeds, likes, comments.
- Cross-device sync, encryption-at-rest, export/backup, iOS.
-
## Validation Log

### Session 1 — 2026-08-27
**Trigger:** `validate` subcommand, default mode on the 5-phase plan.
**Questions asked:** 3

#### Verification Results
- **Tier:** Full (5 phases) — greenfield repo, no codebase to grep; toolchain claims checked against the live host instead.
- **Claimed check:** JDK 17 ✓, platforms 34/35/36 ✓, build-tools 34-36 ✓, adb ✓, `emulator` pkg ✓; ✗ system image, ✗ AVD, ✗ cmdline-tools, ✗ attached device.
- **Verified:** 6 | **Failed:** 1 (no runnable Android test target) | **Unverified:** 0

#### Questions & Answers

1. **[Environment]** No emulator, AVD, system image, or physical device; cmdline-tools absent. How should instrumented tests + on-emulator verification run?
   - Options: Provision an AVD | Switch tests to JVM | Use a physical phone
   - **Answer:** Provision an AVD (install `cmdline-tools;latest`, download `system-images;android-36;google_apis;arm64-v8a`, create `diary_api36` AVD).
   - **Rationale:** keeps instrumented Room tests + a real UI verification matrix; no physical device available.
2. **[Assumption]** On which API target should the verification matrix run? (Plan said "API 26 emulator"; only 34/35/36 installed.)
   - Options: Test on android-36 | Also provision API 26
   - **Answer:** Test on android-36 only.
   - **Rationale:** newest installed image, no extra download; min SDK 26 guarded at compile time. Runtime gap: real API-26 behavior untested in MVP (documented risk).
3. **[Assumptions]** Application ID is placeholder `com.example.diary`. Finalize?
   - Options: Keep com.example.diary | Set a real id now
   - **Answer:** Keep `com.example.diary`.
   - **Rationale:** mechanical rename is cheap before Play Store; no acceptance risk now.

#### Confirmed Decisions
- AVD: provision on-device `diary_api36` (API 36) in Phase 1.
- Verification target: API 36 only; runtime min-SDK gap accepted.
- Application ID: `com.example.diary` kept.

#### Action Items
- [x] Phase 1: add AVD-provisioning steps (cmdline-tools → system image → AVD).
- [x] Phase 5: verification matrix + success criteria target `diary_api36`; add runtime min-SDK risk.
- [x] plan.md SC wording → `diary_api36`; Validation Log appended.

#### Impact on Phases
- Phase 1: AVD provisioning added (requirements, steps, risk).
- Phase 5: verification target API 36, min-SDK-gap risk added.

### Whole-Plan Consistency Sweep
- Files reread: plan.md, phase-01 .. phase-05.
- Decision deltas checked: AVD api-36 target, provisioning in Phase 1, `com.example.diary` kept, runtime min-SDK caveat.
- Reconciled stale references: "API 26 emulator" wording → `diary_api36` (phase-01 SC, phase-05 requirement + SC, plan.md SC).
- Unresolved contradictions: **0**.

<!-- slug: diary-app-mvp -->
