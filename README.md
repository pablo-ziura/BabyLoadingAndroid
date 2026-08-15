# Baby Loading

Android application built with Kotlin, Jetpack Compose Material 3, native edge-to-edge, and type-safe Navigation Compose routes.

The Android application ID and root package are `com.pablo.ruiz.babyloading`. Application data is intentionally excluded from Android Auto Backup and device-to-device transfer.

## Architecture

The project follows a pragmatic, feature-first Clean Architecture. Packages are created only when they contain concrete code; no empty `data` or `domain` layers are kept as placeholders.

The Compose design system provides a fixed light Material 3 palette, typography, shapes, spacing, gradient background, and reusable surface card. Dynamic color is intentionally disabled so the product identity remains consistent.

The main shell adapts at 600 dp: compact windows use Android bottom navigation, while medium and expanded windows use a navigation rail without changing the feature navigation graphs.

Pregnancy calculations use `LocalDate` and an injected `Clock`. The estimated due date is 280 days after the last menstrual period, and progress state distinguishes early, active, post-term, and needs-review boundaries.

On first launch, a mandatory Material date picker captures the last-period date. Preferences DataStore persists it as ISO-8601, and app startup waits for that state before choosing onboarding or the main shell.
The picker accepts dates from today through exactly 42 weeks ago. A previously valid stored date can continue ageing into the needs-review state after week 42.

Validated local JSON provides weekly editorial content for weeks 6–40 in English and Spanish. Unsupported locales fall back to English, weeks below 6 have no weekly entry, and weeks 41–42 reuse week 40 content while retaining their distinct pregnancy stage.

The dashboard combines the saved date and local weekly content in an immutable UI state. It shows estimated week and day, progress, remaining days, due date, baby-size imagery, milestones, and explicit early, post-term, and review guidance.

The journey presents weeks 1–42 as an Android timeline with completed, current, and upcoming semantics. Weeks can be expanded for details, the list opens near the current position, and early or post-term rows use purpose-built guidance instead of inventing missing editorial data.

```text
Baby Loading
├── app/
│   └── com.pablo.ruiz.babyloading/
│       ├── app/shell/                    # Main application chrome and bottom bar UI
│       ├── navigation/                   # Type-safe routes and navigation hosts
│       ├── core/designsystem/theme/      # Shared Compose theme and visual tokens
│       └── feature/                      # Product capabilities
└── core/network/                         # Shared HTTP, serialization, auth, and error handling
```

Each feature owns its implementation:

- `presentation/` contains Compose screens, `ViewModel`s, immutable UI state, and UI events.
- `domain/` is added only when business rules, use cases, domain models, or repository contracts are needed. It has no Android, Compose, or data implementation dependencies.
- `data/` is added only when a feature requires local or remote data. It contains feature-specific DTOs, Retrofit services, mappers, data sources, and repository implementations.
- Shared infrastructure belongs in `core/`. For example, a reusable HTTP client belongs in `core/network/`, while an endpoint used only by Gallery belongs in `feature/gallery/data/remote/`.

The `app/shell` layer renders the main application chrome. The `navigation` layer owns `NavController`s and app transitions. Features stay navigation-agnostic and expose user events through lambdas.

## Networking

The `:core:network` module provides the shared Kotlinx Serialization, OkHttp, Retrofit, Hilt, authentication, and safe-call infrastructure. API-specific services, DTOs, mappers, remote data sources, and repositories remain in the owning feature's `data` package.

Configure the API root as a Gradle property. The URL is normalized with a trailing slash:

```bash
./gradlew assembleDebug \
  -PBABYLOADING_API_BASE_URL=https://api.example.com/
```

When the property is absent, the app uses `https://example.invalid/`, a deliberately non-routable fallback. This keeps builds reproducible before a backend is connected without risking calls to a real service.

Normal feature services use the default authenticated `Retrofit`. Authentication and refresh implementations bind the optional `AccessTokenStore` and `AccessTokenRefresher` contracts. Login and refresh services must use the qualified unauthenticated client or Retrofit instance to prevent recursive `401` handling. Automatic Bearer credentials are sent only to the configured API origin; caller-provided `Authorization` headers are preserved and excluded from automatic refresh.

Network calls return `NetworkResult` through `safeApiCall`. A feature maps `NetworkError` to its own domain error before crossing from `data` into `domain` or `presentation`.

## Navigation

Navigation uses Kotlin serialization type-safe routes. Onboarding is the root start destination. The main shell hosts four isolated tab graphs: Dashboard, Journey, Gallery, and Settings. Switching tabs preserves each graph's back stack through Navigation state save and restore.

## Verification

Run these commands from the repository root:

```bash
./gradlew assembleDebug
./gradlew check
./gradlew clean assembleDebug assembleRelease testDebugUnitTest testReleaseUnitTest check
```

The primary debugging target is a Pixel 9a running API 36.1. The project compiles and targets Android API 37, while keeping runtime behavior compatible with the configured minimum SDK.
