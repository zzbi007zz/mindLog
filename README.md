# MindLog — Diary App MVP

A native **offline-first** Android diary. Create, edit, and browse dated entries
with text + image galleries. All data lives **on-device** — no accounts, no
backend, no network.

> **Status:** MVP complete (Phases 1–5). Deviations from the original plan:
> the verification target is the `diary_api36` API-36 emulator (provisioned in
> Phase 1) rather than a literal API-26 image; real API-26 runtime behavior is
> untested (accepted, documented in the plan).

## Stack

- **Kotlin 2.0** + **Jetpack Compose** (Material 3), single `:app` module
- **Room** persistence (entries + entry_images), **Coil** image loading
- **DataStore** (declared; used in later phases), Navigation-Compose
- Min SDK 26, target/compile SDK 35
- Gradle 8.13 / AGP 8.7.3 / JDK 17 (see `gradle/libs.versions.toml`)

## Build & run

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew connectedDebugAndroidTest     # instrumented tests (needs a device/emulator)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app is **fully offline** — the manifest declares **no** `INTERNET` or
media/storage permissions. All data lives under app-private storage:

- Room DB: `files/databases/diary.db`
- Entry images: `files/images/` (DB stores relative paths, never bytes)

## Architecture

```
Compose UI → ViewModels → DiaryRepository (interface) → LocalDiaryRepository
                                                    ├── Room (entries + images)
                                                    └── ImageStorage (files)
```

`DiaryRepository` is the persistence seam — a future Firebase-backed
`RemoteDiaryRepository` can drop in behind it without touching UI/ViewModels.

## Data model

- **Multiple entries per day** (timestamped, listed grouped by date).
- **0–N images per entry**, shown as a gallery.
- **Plain-text** body (no rich text in MVP).

## Offline / image-lifecycle guarantees

- Every delete removes the entry, its images rows (cascade), **and** its image
  files.
- Editing that drops an image deletes that file.
- A startup **orphan sweep** removes any unreferenced file under `images/`
  (backstop for crashes/partial writes).
- Picked images are **copied** into app-private storage — the app never holds a
  borrowed external URI.

## Tests

`app/src/androidTest/.../DiaryRepositoryTest.kt` — CRUD, cascade, edit-remove
file cleanup, import-copy, and the orphan sweep (6 tests). Run with
`connectedDebugAndroidTest` on an emulator/device.

## Project layout

```
plans/260827-2053-diary-app-mvp/   # the implementation plan (validated)
app/src/main/                      # source
app/schemas/                       # Room schema JSON (v1)
```

## Roadmap (future, not in this MVP)

- Firebase anonymous auth + sync via a remote repository implementation
- Social posting/feeds/comments + moderation account
- Cross-device sync, export/backup