---
phase: 1
title: "Project Scaffold"
status: pending
priority: P1
effort: "0.5d"
dependencies: []
---

# Phase 1: Project Scaffold

## Overview

Create a runnable, empty Compose Android project with the module structure,
dependencies, and offline guarantees the rest of the plan builds on. Ends with
an app that launches to a placeholder Home screen.

## Requirements

- Functional: `./gradlew assembleDebug` builds; app launches to a placeholder screen.
- Non-functional: **no `INTERNET` permission** in the manifest (enforces offline-only).
- Non-functional: Kotlin + Compose + version catalog; min SDK 26, target/compile SDK 35.
- Non-functional: a runnable API 36 AVD exists on the host before local-run verification. (No AVD/system image/cmdline-tools present and no physical device attached — approve provisioning.)

## Architecture

Single Gradle module `app`. Package `com.example.diary`. Source layout the later
phases fill in:

```
app/src/main/java/com/example/diary/
  DiaryApp.kt              # Application subclass (holds DB + repository singletons)
  MainActivity.kt          # single-activity, setContent { DiaryTheme { DiaryNavHost() } }
  ui/theme/                # Material 3 theme (Color, Type, Theme)
  ui/navigation/           # DiaryNavHost (routes added in Phase 3+)
  data/                    # filled in Phase 2
  ui/entry/                # filled in Phase 3/4
```

Dependency wiring in MVP: manual construction in `DiaryApp` (no Hilt). Rationale:
one impl, one graph — a DI framework is future-phase weight. ViewModels get the
repository via a small `ViewModelProvider.Factory` or `viewModel { }` lambda.

## Related Code Files

- Create: `settings.gradle.kts`, `build.gradle.kts` (root), `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml` (version catalog)
- Create: `app/src/main/AndroidManifest.xml` (no INTERNET permission)
- Create: `app/src/main/java/com/example/diary/DiaryApp.kt`
- Create: `app/src/main/java/com/example/diary/MainActivity.kt`
- Create: `app/src/main/java/com/example/diary/ui/theme/{Color,Type,Theme}.kt`
- Create: `app/src/main/java/com/example/diary/ui/navigation/DiaryNavHost.kt`
- Create: `.gitignore` (Android/Gradle template)

## Implementation Steps

### AVD provisioning (before local-run verification)

Only `platforms/android-34/35/36`, `emulator`, and build-tools are installed —
no system image, no AVD, no `cmdline-tools`. Install `cmdline-tools;latest`
(via SDK manager or Android Studio), then:
1. `sdkmanager "system-images;android-36;google_apis;arm64-v8a"`
2. `avdmanager create avd -n diary_api36 -k "system-images;android-36;google_apis;arm64-v8a" -d pixel_6`
3. Boot it (`emulator -avd diary_api36`), confirm `adb devices` lists it as `device`.

1. Scaffold Gradle project (AGP 8.5+, Kotlin 2.0+, Compose BOM). Use the version catalog for: `androidx.core:core-ktx`, `lifecycle-viewmodel-compose`, `activity-compose`, `compose-bom`, `material3`, `navigation-compose`, `room-runtime`/`room-ktx`/`room-compiler` (KSP), `datastore-preferences`, `coil-compose`. Add KSP plugin.
2. Write `AndroidManifest.xml` with `<application android:name=".DiaryApp">` and MainActivity. Do **not** add `<uses-permission android:name="android.permission.INTERNET"/>`.
3. Implement `DiaryTheme` (Material 3, dynamic color optional, light/dark).
4. Implement `MainActivity` → `setContent { DiaryTheme { DiaryNavHost() } }`.
5. Implement `DiaryNavHost` with a single placeholder `home` route showing centered text "Diary".
6. Implement `DiaryApp : Application` with lazy `lateinit`/`by lazy` holders for the DB and repository (populated in Phase 2 — leave TODO stubs typed but empty now, or add in Phase 2; do not fake implementations).

## Success Criteria

- [x] `./gradlew assembleDebug` succeeds.
- [x] App installs and launches to the "Diary" placeholder on an API 36 emulator (`diary_api36`).
- [x] `AndroidManifest.xml` contains no INTERNET permission (grep confirms).
- [x] Version catalog resolves; no hardcoded dependency versions in module `build.gradle.kts`.

## Risk Assessment

- **Toolchain drift** (AGP/Kotlin/Compose BOM version mismatch is the #1 greenfield failure). *Signal:* Gradle sync/KSP errors on build. *Response:* pin to a known-good triple from the current Compose BOM release notes; if a version is unresolvable, adjust to the nearest compatible rather than replanning.
- **Assumption:** manual DI stays manageable. *Breaks when:* a second data source (future phase) is added. *Signal:* `DiaryApp` construction grows tangled. *Response:* that is the documented trigger to introduce Hilt — a future-phase change, not MVP.
- **Emulator provisioning cost** (system-image download + AVD creation is ~2-4 GB). *Signal:* `sdkmanager`/download fails or disk is short. *Response:* the AVD is a verification prerequisite, not app code — if provisioning stalls, manually verify with Android Studio's Device Manager and continue; never fake the run to mark the criterion done.
