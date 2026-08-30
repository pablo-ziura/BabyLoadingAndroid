# Baby Loading

Android application built with Kotlin, Jetpack Compose Material 3, native edge-to-edge, and type-safe Navigation Compose routes.

The Android application ID and root package are `com.pablo.ruiz.babyloading`. Application data is intentionally excluded from Android Auto Backup and device-to-device transfer.

## Architecture

The project follows modular, feature-first Clean Architecture. `:app` is the composition root; product capabilities live in independent `:feature:*` Android libraries, and reusable technical or domain infrastructure lives in `:core:*`. Feature modules depend only on core modules, never on another feature module. Packages are created only when they contain concrete code.

The Compose design system provides a fixed light Material 3 palette, typography, shapes, spacing, gradient background, and reusable surface card. Dynamic color is intentionally disabled so the product identity remains consistent.

The main shell adapts at 600 dp: compact windows use Android bottom navigation, while medium and expanded windows use a navigation rail without changing the feature navigation graphs.

Pregnancy calculations use `LocalDate` and an injected `Clock`. The estimated due date is 280 days after the last menstrual period. Pregnancy remains ongoing through 40+6, is late term from 41+0 through 41+6, and is postterm from 42+0 onward; a future stored date is invalid.

On first launch, a mandatory Material date picker captures the last-period date. Preferences DataStore persists it as ISO-8601, and app startup waits for that state before choosing onboarding or the main shell.
The picker accepts dates from today through exactly 42 weeks ago. A previously valid stored date can continue ageing as postterm progress beyond week 42.

Validated local JSON provides weekly editorial content exclusively for weeks 6–40 in English and Spanish. Unsupported locales fall back to English. Weeks below 6, late-term weeks, and postterm weeks never reuse week 40 content.

The dashboard combines the saved date and local weekly content in an immutable UI state. It shows estimated week and day, progress, remaining days, due date, baby-size imagery, milestones, and explicit early, late-term, and postterm guidance.

The journey presents the available weeks 6–40 as an Android timeline with completed, current, and upcoming semantics. Weeks can be expanded for details, the list opens near the current position, and early, late-term, or postterm states use purpose-built guidance instead of inventing missing editorial data.

Settings lets the user revise the last-period date within the same validated range, previews the estimated due date, links to Android’s native per-app language settings for English or Spanish, and states the local-only backup policy.

The unified Gallery and Guided Tracking module imports up to 10 images per Photo Picker selection without storage permission, deduplicates source URIs, limits each private copy to 25 MiB, and stores metadata in Room. Partial imports keep successful items. Each tile identifies imported or guided-tracking origin; deleting an item removes only its private copy and never removes an independent MediaStore export.

Guided belly tracking uses CameraX with a consistent alignment overlay, front/back camera selection, and optional rear flash. Each capture is saved privately before Android exports an independent JPEG to `Pictures/Baby Loading`; a MediaStore failure never discards the private copy.

The fixed 4×2 Glance home-screen widget shows the current week and day, remaining days, progress, and estimated due date. It resolves the app launcher through `PackageManager`, refreshes immediately after date changes, and schedules at most one refresh at the next local midnight while pregnancy is ongoing, late term, or postterm. Setup and invalid-date states cancel the alarm; the widget uses neither WorkManager nor notifications.

## Lab build variant

The `lab` build type installs **Baby Loading Lab** alongside the production app for manual testing. It is a debuggable, non-distributable variant; production `debug` and `release` builds keep their original identity and behavior.

- Application ID: `com.pablo.ruiz.babyloading.lab` (`.lab` suffix).
- The variant has its own violet launcher icon, made from the production artwork with a uniform `#7C3AED` overlay at 28% opacity.
- Android application sandboxes are separate by application ID. Lab additionally uses distinct names for its Preferences DataStores, Room database, app-private gallery directory, widget refresh action, and MediaStore exports (`Pictures/Baby Loading Lab`), so it cannot read or write production data.
- Lab is for local and QA testing only; never distribute it as a production build.

Build, test, lint, or install it with:

```bash
./gradlew assembleLab
./gradlew lintLab
./gradlew installLab
```

```text
:app
├── :feature:onboarding
├── :feature:dashboard
├── :feature:journey
├── :feature:gallery       # Gallery + Guided Tracking
├── :feature:settings
├── :feature:widget
├── :core:designsystem     # App shell and theme
├── :core:localization     # Foreground language refresh
├── :core:pregnancy        # Bootstrap state
└── :core:network          # API composition

:feature:* ───────────────> required :core:* modules only
:feature:widget ──────────> :core:designsystem, :core:localization, :core:pregnancy,
                            :core:pregnancy-content, :core:storage
:core:pregnancy ──────────> :core:storage
:core:pregnancy-content ──> :core:localization, :core:coroutines

Other core modules: :core:designsystem, :core:localization, :core:coroutines, :core:storage
```

Each feature owns its implementation:

- `presentation/` contains Compose screens, `ViewModel`s, immutable UI state, and UI events.
- `domain/` is added only when business rules, use cases, domain models, or repository contracts are needed. It has no Android, Compose, or data implementation dependencies.
- `data/` is added only when a feature requires local or remote data. It contains feature-specific DTOs, Retrofit services, mappers, data sources, and repository implementations.
- Shared infrastructure belongs in `core/`. `:core:storage` centralizes Prod/Lab physical names, `:core:coroutines` provides qualified dispatchers and test fixtures, `:core:designsystem` owns shared Compose resources, `:core:localization` resolves Android app language, `:core:pregnancy` owns pregnancy domain and DataStore persistence, `:core:pregnancy-content` owns JSON and fetal assets, and `:core:network` owns HTTP infrastructure.

`:app` contains only application/activity startup, bootstrap and foreground composition, shell, navigation, network configuration, the production `Clock`, and final manifest integration. It passes app-owned values such as `BuildConfig.VERSION_NAME` into libraries rather than exposing app `BuildConfig` to them.

The `app/shell` layer renders the main application chrome. The `navigation` layer owns `NavController`s and app transitions. Features stay navigation-agnostic and expose user events through lambdas.

## Networking

The `:core:network` module provides the shared Kotlinx Serialization, OkHttp, Retrofit, Hilt, authentication, and safe-call infrastructure. API-specific services, DTOs, mappers, remote data sources, and repositories remain in the owning feature's `data` package.

Configure the API root as a Gradle property. The URL is normalized with a trailing slash:

```bash
./gradlew assembleDebug \
  -PBABYLOADING_API_BASE_URL=https://api.example.com/
```

When the property is absent, the app uses `https://example.invalid/`, a deliberately non-routable fallback. This keeps builds reproducible before a backend is connected without risking calls to a real service.

Normal feature services use the default authenticated `Retrofit`. Authentication and refresh implementations bind the optional `AccessTokenDataSource` and `AccessTokenRefreshDataSource` contracts. Login and refresh services must use the qualified unauthenticated client or Retrofit instance to prevent recursive `401` handling. Automatic Bearer credentials are sent only to the configured API origin; caller-provided `Authorization` headers are preserved and excluded from automatic refresh.

Network calls return `NetworkResult` through `safeApiCall`. A feature maps `NetworkError` to its own domain error before crossing from `data` into `domain` or `presentation`.

## Navigation

Navigation uses Kotlin serialization type-safe routes. Onboarding is the root start destination. The main shell hosts four isolated tab graphs: Dashboard, Journey, Gallery, and Settings. Switching tabs preserves each graph's back stack through Navigation state save and restore.

## Verification

Run these commands from the repository root:

```bash
./gradlew projects
./gradlew testDebugUnitTest
./gradlew assembleDebug assembleLab assembleRelease
./gradlew assembleDebugAndroidTest
./gradlew check
```

Android Studio includes the shared **All Unit Tests** Gradle configuration. Select it
from the run-configuration menu and press Run to execute all local unit tests: the
`debug` variant across every Android module and the release-only network tests. It
deliberately excludes instrumentation tests, which require a connected device or
emulator.

The primary debugging target is a Pixel 9a running API 36.1. CameraX, MediaStore, the Glance widget, Compose navigation, and `GalleryDaoTest` require device validation when that target is connected. The project compiles and targets Android API 37 with `minSdk` 34 and Java 17 bytecode.
