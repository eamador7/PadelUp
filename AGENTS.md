# Padel Up — Agent Guide

Padel Up is a native Android application (Kotlin, Jetpack Compose, Material 3, Room SQLite, Offline-First) for padel match scoring, dynamic skill rating (Playtomic-style 1.00–7.00), social groups, and an optimized Americano & Mexicano tournament pairing engine.

## Architecture & Build Environment
- **Platform & Identifiers**:
  - Android `applicationId = "com.padelscoring"` (Kotlin `namespace = "com.padelscoring"`, `minSdk = 26`, `compileSdk = 35`, `targetSdk = 35`, JVM Target 17).
  - Clean Architecture: Domain (models, business logic, algorithms), Data (Room SQLite database, DAOs, repositories), Presentation (MVI/MVVM, Jetpack Compose, Impeccable Design System).
- **Local Host (Orange Pi 5 Plus)**:
  - Do **not** install Android Studio, the Android SDK, or run heavy Gradle builds locally on the Orange Pi host.
- **CI / Verification Pipeline (`.github/workflows/android-ci.yml`)**:
  - All branches (`agent/**`, `feat/**`, etc.) and PRs run `./gradlew testDebugUnitTest assembleDebug` on GitHub Actions and upload `padelup-debug-apk`.
  - The background Agent Factory (`worker.py`) watches CI runs (`gh run watch`), downloads `.apk` artifacts upon completion, and pushes them directly to Telegram.
- **Automated QA Strategy (`./gradlew testDebugUnitTest`)**:
  - All automated QA runs on the JVM inside `testDebugUnitTest` (zero Android Emulators needed in CI):
    1. **Pure Kotlin Domain & Math Tests**: Americano pairing engine, Mexicano tiered parity, Social Golfer CSP constraints, Playtomic dynamic rating (PDR) formulas, score conservation.
    2. **Robolectric + Room In-Memory SQLite Integration Tests**: Tests real `@Dao` SQL queries, foreign keys, cascading deletions, and offline-first queries.
    3. **Robolectric + Headless Jetpack Compose UI Tests (`createComposeRule`)**: Mounts `@Composable` screens/components on the JVM to verify UI rendering and interactions.
- **Agent Factory Ticket Header**: When creating Agent Factory tickets for `/mnt/storage/projects/padelup`, always set:
  ```yaml
  ---
  repo: /mnt/storage/projects/padelup
  branch: agent/<ticket-slug>
  complexity: standard
  test_cmd: github-actions
  auto_merge: 1
  ---
  ```

## Agent skills

### Issue tracker

Issues and specs for this repo are tracked in GitHub Issues (`eamador7/padelup`). See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default 5 canonical triage labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.
