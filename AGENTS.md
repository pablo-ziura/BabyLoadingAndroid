# AGENTS.md

## Project Role

Act as a Senior Android/Kotlin Architect for this repository.

Before proposing or applying changes, read this file and inspect the relevant Gradle, Kotlin, Compose, resource, and manifest files affected by the task. Keep this file updated when later development introduces new architectural decisions, module boundaries, build rules, or project conventions.

Before creating or modifying any screen, reusable component, or widget containing text, read `DESIGN.md` and follow its typography and accessibility rules.

## Platform Baseline

- Target a modern Android/Kotlin application using Android API 37 as the project baseline.
- Prefer stable AndroidX, Jetpack Compose, Kotlin, and Android Gradle Plugin APIs.
- Keep `compileSdk` and `targetSdk` aligned with API 37 unless a task explicitly requires a temporary exception.
- Keep `minSdk` intentional and documented when it changes.
- Validate API availability before using platform APIs introduced after the configured `minSdk`.

## Language And Build

- Use Kotlin for application code.
- Use `com.pablo.ruiz.babyloading` as the application ID, namespace, and root source package.
- Use Kotlin DSL for Gradle files.
- Use the existing Version Catalog in `gradle/libs.versions.toml` for dependencies and plugins.
- Target Java 17 bytecode across Android modules; the Gradle daemon may use the configured Java 21 toolchain.
- Use KSP for supported annotation processors. Do not introduce KAPT while AGP built-in Kotlin is enabled.
- Do not hardcode dependency versions in module Gradle files when the Version Catalog can own them.
- Keep build changes scoped and explain any changes to Android Gradle Plugin, Kotlin, Java compatibility, Compose compiler, or SDK versions.
- Prefer Java/Kotlin compatibility levels already configured in the project unless a feature requires changing them.

## Architecture

- Keep UI, state, domain logic, and platform integration separated as the project grows.
- Represent pregnancy calendar values with `java.time.LocalDate` and inject `Clock` whenever current time affects behavior.
- Treat weeks 0-5 as early, 6-40 as active, 41-42 as post-term, and 43 or later as requiring review; the journey ends at week 42.
- Date selection accepts today through exactly 42 weeks in the past; older stored dates remain readable so elapsed pregnancies can enter the needs-review state.
- Prefer unidirectional data flow for Compose screens.
- Use a feature-first Clean Architecture structure. Every product capability, including onboarding, belongs in `feature/<feature-name>/`.
- Put screens, `ViewModel`s, immutable UI state, and UI events in `feature/<feature-name>/presentation/`.
- Add `feature/<feature-name>/domain/` only when the feature has real business rules, use cases, domain models, or repository contracts. Domain code must not depend on Android, Compose, or data implementations.
- Add `feature/<feature-name>/data/` only when the feature owns persisted or remote data. Data implementations may depend on the feature domain contracts and shared core infrastructure.
- Store the mandatory last-period date as an ISO-8601 string in Preferences DataStore; its presence decides whether onboarding or the main shell starts.
- Keep feature-specific Retrofit services, DTOs, mappers, local sources, and repository implementations inside that feature's `data` layer.
- Store unified gallery metadata in Room and image bytes in app-private files; use Android Photo Picker for imports without broad media permissions.
- Label gallery items as imported or guided tracking. Deleting a guided tracking item removes only its private app copy and never deletes an exported MediaStore copy.
- Use CameraX for guided captures and request only the runtime camera permission. Save the private Room-backed copy first, then export a separate JPEG to `Pictures/Baby Loading` through MediaStore without storage permissions.
- Treat MediaStore export as recoverable: if it fails, preserve and report the successfully saved private gallery copy.
- Provide a single fixed 4x2 home-screen widget with Jetpack Glance. Refresh it immediately after pregnancy-date changes and at most daily while the app is inactive; do not schedule frequent background work or notifications.
- Export Room schemas to `app/schemas/` and add migrations whenever the database version changes.
- Put genuinely reusable technical infrastructure in `core/`: the current design system is in `core/designsystem/theme/`; future shared HTTP configuration, database setup, preferences, and test utilities belong under dedicated `core` packages when implemented.
- Keep weekly editorial pregnancy content local-only in validated `pregnancy-content.en.json` and `pregnancy-content.es.json` assets; do not add remote refresh behavior without an explicit product decision.
- Do not create empty packages or speculative layers. Create a package only with its first concrete production or test file.
- Keep visual application chrome in `app/shell/`; root and nested navigation contracts and hosts belong in `navigation/`.
- Keep navigation routes in a dedicated `navigation` package using `@Serializable` type-safe routes.
- Model top-level tabs as nested navigation graphs and preserve tab state with Navigation save/restore options.
- Keep destination screens independent from `NavController`; expose navigation through event lambdas.
- Keep feature screens in separate files; do not use a shared tab placeholder composable.
- Presentation code may depend on its feature domain layer and `core`, but it must not access another feature's internals directly.
- Navigation is the only layer that owns `NavController` instances and coordinates transitions between features.
- Keep composables small, previewable, and focused on rendering state.
- Use a bottom navigation bar below 600 dp and a navigation rail at 600 dp and above.
- Move non-trivial business logic out of composables and into plain Kotlin classes, use cases, or ViewModels as appropriate.
- Inject qualified coroutine dispatchers for blocking data work so ViewModel tests can control execution deterministically.
- Avoid introducing new architectural frameworks unless they solve a concrete project need.
- Follow existing package structure until a feature justifies creating clearer module or package boundaries.

## Networking

- Shared HTTP infrastructure lives in the `:core:network` Android library module. The app depends on it, never the reverse.
- Keep Retrofit services, DTOs, mappers, remote data sources, and repository implementations in the owning feature's `data` layer.
- Use `NetworkResult` only at remote and data boundaries. Map `NetworkError` to feature domain errors before exposing results to presentation.
- Define feature API services with Retrofit `Response<T>` return types and execute them through `safeApiCall`.
- Use the authenticated Retrofit instance for normal feature traffic and the qualified unauthenticated instance for login, token refresh, or explicitly public clients.
- `AccessTokenStore` implementations must be thread-safe. `AccessTokenRefresher` implementations must use the unauthenticated client so refresh cannot recurse through the authenticator.
- Bearer attachment belongs in `AuthInterceptor`; `401` refresh and the single retry belong in `TokenRefreshAuthenticator`.
- Automatic Bearer credentials are restricted to the configured API origin. Explicit `Authorization` headers are caller-owned and never participate in automatic refresh.
- Production builds must not include HTTP logging. Debug logging stays metadata-only unless an explicit architectural decision changes the policy.
- Configure the API root with the `BABYLOADING_API_BASE_URL` Gradle property. Never store credentials or tokens in Gradle properties.

## Jetpack Compose

- Use Material 3 components by default.
- Use the fixed light-only `BabyLoadingTheme`; do not enable dynamic color or dark mode unless the product direction changes explicitly.
- Reuse design-system spacing, shapes, backgrounds, and cards instead of defining feature-local visual constants.
- Keep UI state immutable where practical.
- Use `remember`, `derivedStateOf`, and side-effect APIs deliberately.
- Avoid launching work directly from composable bodies; use Compose side-effect APIs or lifecycle-aware layers.
- Provide previews for reusable UI components when the component has meaningful visual states.
- Provide Compose previews for primary screens and app shell components.
- Keep accessibility in mind: content descriptions, readable contrast, touch target sizes, semantic roles, and scalable text.

## Resources And Manifest

- Keep user-facing strings in `res/values/strings.xml`.
- Declare supported English and Spanish app locales in `res/xml/locales_config.xml` and delegate language choice to Android per-app language settings.
- Prefer theme and color resources for reusable styling decisions.
- Keep manifest permissions minimal and explain why any new permission is required.
- Do not add exported components unless they are required; explicitly set `android:exported` where needed.
- Keep backup, data extraction, and privacy-related XML files intentional and updated with feature changes.
- Keep Android Auto Backup and device-to-device transfer disabled for all pregnancy and media data.

## Testing

- Add or update tests when changing behavior, state handling, formatting, parsing, permissions, persistence, or navigation.
- Prefer local unit tests for pure Kotlin logic.
- Use MockWebServer for deterministic HTTP integration tests and `kotlinx-coroutines-test` for suspend and Flow behavior.
- Use instrumentation or Compose UI tests for Android framework integration and UI behavior.
- Keep tests deterministic and avoid depending on network, clock, locale, or device state unless that is the behavior under test.

## Workflow And Verification

- To verify the project compiles successfully, run `./gradlew assembleDebug`.
- To verify logic and automated checks, run `./gradlew check` or `./gradlew testDebugUnitTest`.
- Before completing complex refactors or feature implementations, run `./gradlew clean assembleDebug testDebugUnitTest`.
- The primary debugging device is a Pixel 9a running API 36.1. Ensure UI, UX, and hardware integrations remain compatible with this API level and hardware profile.

## Git And Commits

- Keep changes focused and avoid unrelated formatting churn.
- Preserve user changes already present in the working tree.
- Commit messages must be in English and use Conventional Commits:
  `type(scope): brief description`
- Examples:
  `feat(app): add loading progress screen`
  `fix(theme): correct dark mode colors`
  `chore(project): update Android baseline`

## Documentation Maintenance

- Update this file when new development establishes conventions for architecture, dependencies, testing, naming, release process, or tooling.
- Update `README.md` when project setup, commands, environment requirements, or feature descriptions become useful to future contributors.
