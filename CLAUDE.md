# IslamNotify2 — Claude Code Guide

## Project Overview
Android app (Kotlin + Jetpack Compose) that delivers Islamic prayer time notifications, alarms, azan sounds, Qibla finder, and Hijri calendar. Targets API 24+, compileSdk 36.

## Architecture
Clean architecture with feature-based packages under `com.islamnotify`:
- `domain/` — pure Kotlin models, interfaces, use cases (no Android deps)
- `data/` — implementations, DataStore, Room DAOs, Workers, Retrofit
- `di/` — Hilt modules
- `presentation/` — Composables, ViewModels, UI state

Key features: `alarms/`, `prayer_times/`, `sounds/`, `events/`, `location/`, `calendar/`, `qibla_finder/`, `settings/`, `notification/`

## Tech Stack
- **DI**: Hilt 2.57.2 (KSP, not kapt)
- **DB**: Room (KSP)
- **Background**: WorkManager 2.11.0 + AlarmManager
- **Prefs**: DataStore Preferences
- **Network**: Retrofit 3 + Gson
- **UI**: Compose BOM 2025.12.01, Material3, Navigation Compose
- **Audio**: Media3 ExoPlayer + MediaSession
- **Maps**: MapLibre 12.3.1
- **Prayer calc**: batoul/adhan2 library
- **Coroutines**: 1.10.2

## Build Commands
```
# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

## Secrets
`local.properties` holds the release keystore credentials (`RELEASE_STORE_FILE`,
`RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) — never commit this file.
The build guards the read with an existence check, so CI/off-machine builds without
`local.properties` still configure (only `assembleRelease`/`bundleRelease` need the keystore).

## Current State
- Alarms feature is newly added (all `alarms/` files are new/staged)
- Settings screen is not fully implemented
- Navigation uses type-safe Navigation Compose (kotlinx.serialization)
