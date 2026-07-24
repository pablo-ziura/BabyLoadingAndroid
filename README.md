# Baby Loading

Android application built with Kotlin, Jetpack Compose Material 3, native edge-to-edge, and type-safe Navigation Compose routes.

## Architecture

The project follows a pragmatic, feature-first Clean Architecture. Packages are created only when they contain concrete code; no empty `data` or `domain` layers are kept as placeholders.

```text
com.example.babyloading/
├── app/shell/                    # Main application chrome and bottom bar UI
├── navigation/                   # Type-safe routes and navigation hosts
├── core/designsystem/theme/      # Shared Compose theme and visual tokens
└── feature/
    ├── onboarding/presentation/  # Onboarding UI
    ├── dashboard/presentation/   # Dashboard UI
    ├── journey/presentation/     # Journey UI
    ├── gallery/presentation/     # Gallery UI
    └── settings/presentation/    # Settings UI
```

Each feature owns its implementation:

- `presentation/` contains Compose screens, `ViewModel`s, immutable UI state, and UI events.
- `domain/` is added only when business rules, use cases, domain models, or repository contracts are needed. It has no Android, Compose, or data implementation dependencies.
- `data/` is added only when a feature requires local or remote data. It contains feature-specific DTOs, Retrofit services, mappers, data sources, and repository implementations.
- Shared infrastructure belongs in `core/`. For example, a reusable HTTP client belongs in `core/network/`, while an endpoint used only by Gallery belongs in `feature/gallery/data/remote/`.

The `app/shell` layer renders the main application chrome. The `navigation` layer owns `NavController`s and app transitions. Features stay navigation-agnostic and expose user events through lambdas.

## Navigation

Navigation uses Kotlin serialization type-safe routes. Onboarding is the root start destination. The main shell hosts four isolated tab graphs: Dashboard, Journey, Gallery, and Settings. Switching tabs preserves each graph's back stack through Navigation state save and restore.

## Verification

Run these commands from the repository root:

```bash
./gradlew assembleDebug
./gradlew check
./gradlew clean assembleDebug testDebugUnitTest
```

The primary debugging target is a Pixel 9a running API 36.1. The project compiles and targets Android API 37, while keeping runtime behavior compatible with the configured minimum SDK.
