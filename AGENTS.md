# AGENTS.md — Motmaenbash-Android

## Overview

**Motmaenbash** (`nu.milad.motmaenbash`, «مطمئن باش») is an open-source Android anti-phishing and security application targeting Iranian users. It detects phishing SMS, blocks or warns on scam URLs/fake payment gateways in browsers via an Accessibility Service and system overlays, scans installed/newly installed APKs (package, APK hash, signing cert, DEX classes, hidden/disabled apps), and displays verified-gateway badges.

## Tech Stack & Requirements

- **Languages**: Kotlin `2.3.10` (primary), small Java legacy utilities (`UiUtils.java`, `VazirTextView.java`), JVM target 8 (`jvmToolchain(8)`)
- **UI**: Jetpack Compose (`compose-bom:2025.11.00`, Material 3 `1.4.0`, Navigation Compose `2.9.7`, Accompanist Permissions `0.37.3`, Coil `2.7.0`) + ViewBinding enabled for overlay/dialog views
- **Background & Storage**: WorkManager `2.10.4` (`DatabaseUpdateWorker`), Jetpack DataStore Preferences `1.1.7`, raw Android SQLite (`DatabaseHelper`, `motmaenbash.db` v6)
- **Firebase**: Firebase BOM `33.16.0` (Analytics, Cloud Messaging, Performance; Crashlytics in `releaseImplementation` only)
- **Build**: Android Gradle Plugin `9.0.1`, Gradle wrapper (`./gradlew` / `gradlew.bat`), `compileSdk = 36`, `targetSdk = 36`, `minSdk = 21`
- **Tests**: JUnit 4 (`4.13.2`)

## Repository Structure

```text
motmaenbash-android/
├── build.gradle.kts                          # Root plugin aliases
├── settings.gradle.kts                       # Single module (:app)
├── gradle/libs.versions.toml                 # Version catalog (AGP, Kotlin, Compose, Firebase)
├── gradlew / gradlew.bat                     # Gradle wrapper
└── app/
    ├── build.gradle.kts                      # App config, localeFilters ("en", "fa"), R8 release
    ├── google-services.json                  # Firebase project configuration (tracked)
    ├── proguard-rules.pro                    # R8 keep rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml           # Permissions, receivers, services, tools:node="remove" ad perms
        │   ├── java/nu/milad/motmaenbash/
        │   │   ├── App.kt                    # Application class: schedules periodic WorkManager DB update
        │   │   ├── consts/                   # AppConstants (DB schema, GitHub threat-feed URLs), NavRoutes, Pages
        │   │   ├── models/                   # Alert, ApkStatus, AppThreatType, UrlThreatType, Guard, Stats, Tip
        │   │   ├── receivers/                # MonitoringStarterReceiver, SmsReceiver, AppInstallReceiver
        │   │   ├── services/                 # MonitoringService (FGS), UrlGuardService (Accessibility),
        │   │   │                             # AlertOverlayService, VerifiedBadgeOverlayService, FirebaseMessagingService
        │   │   ├── ui/
        │   │   │   ├── activities/           # MainActivity (Compose NavHost), AlertHandlerActivity, PermissionTutorialActivity
        │   │   │   ├── screens/              # MainScreen, IntroScreen, UrlScanScreen, AppScanScreen, SettingsScreen, etc.
        │   │   │   ├── components/           # Shared Compose cards, dialogs, tickers, intro widgets
        │   │   │   └── theme/                # Material 3 Color, Theme, Type
        │   │   ├── viewmodels/               # MainViewModel, AppScanViewModel, UrlScanViewModel, SettingsViewModel, IntroViewModel
        │   │   ├── workers/                  # DatabaseUpdateWorker (WorkManager periodic sync)
        │   │   ├── utils/                    # DatabaseHelper, UpdateManager, UrlUtils, SmsUtils, ScanUtils,
        │   │   │                             # ApkClassesExtractor, PermissionAnalyzer, HiddenAppDetector, DisabledAppDetector, etc.
        │   │   └── widgets/                  # VazirTextView.java
        │   └── res/
        │       ├── raw/                      # Bundled seed JSONs (malicious_data, trusted_data, tips) & alert audio (ding1-6.mp3)
        │       ├── values/ & values-ckb/     # Localized strings, FAQs, changelogs; values-v26/bool.xml toggles manifest receivers
        │       └── xml/                      # accessibility_service.xml, shortcuts.xml, backup_rules.xml
        └── test/java/nu/milad/motmaenbash/   # Unit tests (UrlUtilsTest, ExampleUnitTest)
```

## Architecture & Key Flows

- **Entry Points**: `App.onCreate()` ensures unique periodic WorkManager job `database_update_work` is scheduled via `UpdateManager`. `MainActivity` resolves share/process-text intents (`SEND` / `PROCESS_TEXT` for URL scanning) and onboarding state (`PREF_KEY_HAS_SEEN_INTRO`), hosts the Compose `NavHost`, and starts `MonitoringService` on Android 8+.
- **Dual-Path Background Monitoring**:
  - **API 26+ (Android 8+)**: `MonitoringStarterReceiver` (on `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED`) and `MainActivity` start foreground `MonitoringService` (`foregroundServiceType="specialUse"`), which dynamically registers `SmsReceiver` and `AppInstallReceiver`.
  - **API 21–25**: Manifest-declared `SmsReceiver` and `AppInstallReceiver` are enabled via `@bool/enable_manifest_receivers` (disabled in `values-v26/bool.xml`).
- **Real-Time Web & Payment Gateway Guard**: `UrlGuardService` (AccessibilityService) reads browser URLs, evaluates them against `DatabaseHelper` + `UrlUtils`, and triggers `AlertOverlayService` (scam warning) or `VerifiedBadgeOverlayService` (trusted Iranian payment gateway indicator).
- **Threat Database Sync**: Seeded from `res/raw/*.json` into SQLite `motmaenbash.db` (tables: `flagged_urls`, `flagged_sms`, `flagged_apps`, `trusted_entities`, `tips`, `stats`, `update_history`, `alert_history`) and updated in the background by `DatabaseUpdateWorker` from `https://github.com/miladnouri/motmaenbash-data/raw/main/data/`.
- **App Scanner**: `AppScanViewModel` / `ScanUtils` inspect installed apps using `ApkClassesExtractor`, `HashUtils`, `PermissionAnalyzer`, `HiddenAppDetector`, and `DisabledAppDetector`.

## Development & Commands

```bash
# Run JVM unit tests (e.g. UrlUtilsTest)
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build R8-minified release APK
./gradlew assembleRelease

# Run Android lint
./gradlew lintDebug
```

## Configuration & Environment

- **Firebase**: Configured via `app/google-services.json`. Do not commit private signing keystores or modified production Firebase credentials.
- **Remote Threat Data**: Endpoints are hardcoded in `consts/AppConstants.kt` (`BASE_URL` pointing to `miladnouri/motmaenbash-data`).
- **Localization**: `androidResources.localeFilters` in `app/build.gradle.kts` is restricted to `en` and `fa` (note `res/values-ckb/` also exists in resources).

## Conventions & Gotchas

- **Ad/Tracking Permission Removal**: `AndroidManifest.xml` intentionally uses `tools:node="remove"` to strip `AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_AD_ID`, and `BIND_GET_INSTALL_REFERRER_SERVICE` pulled in transitively by Firebase/Play Services. Preserve these removals.
- **API-Level Receiver Gating**: Never unconditionally enable `SmsReceiver`/`AppInstallReceiver` in the manifest — keep `@bool/enable_manifest_receivers` so API 26+ avoids duplicate events alongside `MonitoringService`.
- **SQLite Schema Migrations**: `DatabaseHelper` manages raw SQLite (`DATABASE_VERSION = 6`); any schema change requires incrementing `AppConstants.DATABASE_VERSION` and handling `onUpgrade`.
- **Trademark Notice**: Licensed under Apache 2.0, but reuse of the name/logo **"MotmaenBash" / "Motmaen Bash" / «مطمئن باش»** in forks or derivative distributions is explicitly prohibited in `LICENSE` / `README.md`.
