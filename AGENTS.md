# AGENTS.md

## Project Role

Act as a Senior Android/Kotlin Architect for this repository.

Before proposing or applying changes, read this file and inspect the relevant Gradle, Kotlin, Compose, resource, and manifest files affected by the task. Keep this file updated when later development introduces new architectural decisions, module boundaries, build rules, or project conventions.

Before creating or modifying any screen, reusable component, or widget containing text, read `DESIGN.md` and follow its typography and accessibility rules.
`DESIGN.md` is the shared Android/iOS visual contract: when a visual decision changes, update its equivalent iOS document in the same task and map each semantic token to both platforms.

## Platform Baseline

- Target a modern Android/Kotlin application using Android API 37 as the project baseline.
- Prefer stable AndroidX, Jetpack Compose, Kotlin, and Android Gradle Plugin APIs.
- Keep `compileSdk` and `targetSdk` aligned with API 37 unless a task explicitly requires a temporary exception.
- Keep `minSdk` at API 34 (Android 14) to support the Redmi Note 12 Pro 5G while `compileSdk` and `targetSdk` remain at API 37; document any future change.
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
- Until Kotlin is upgraded to a stable 2.4.20 or newer release, do not enable remote, shared, or untrusted build caches. Use only isolated caches produced by trusted local or per-job builds; see `SECURITY.md`.
- The `lab` build type is a debuggable, non-distributable manual-testing variant. It uses application ID `com.pablo.ruiz.babyloading.lab`, must keep production `debug` and `release` variants unchanged, and must never gain a production release counterpart.

## Architecture

- Keep UI, state, domain logic, and platform integration separated as the project grows.
- Represent pregnancy calendar values with `java.time.LocalDate` and inject `Clock` whenever current time affects behavior.
- Treat pregnancies as ongoing through 40+6, late term from 41+0 through 41+6, and postterm from 42+0 onward. Future stored last-period dates are invalid states, never week zero.
- Date selection accepts today through exactly 42 weeks in the past; older stored dates remain readable as postterm progress. Editorial content and the fetal-size journey cover only weeks 6-40 and never reuse week 40 for later phases.
- Prefer unidirectional data flow for Compose screens.
- Use a feature-first Clean Architecture structure. Every product capability, including onboarding, belongs in `feature/<feature-name>/`.
- Keep Android modules aligned to this dependency graph. `:app` may compose every feature and the core modules required by bootstrap, shell, navigation, and network configuration. Feature modules may depend on core modules but never on another feature module. Core modules never depend on features or `:app`.
- The current feature modules are `:feature:onboarding`, `:feature:dashboard`, `:feature:journey`, `:feature:gallery`, `:feature:settings`, and `:feature:widget`. Guided Tracking is owned by `:feature:gallery`; do not create a `:feature:tracking` module.
- The current core modules are `:core:storage`, `:core:coroutines`, `:core:designsystem`, `:core:localization`, `:core:pregnancy`, `:core:pregnancy-content`, and `:core:network`.
- Put screens, `ViewModel`s, immutable UI state, and UI events in `feature/<feature-name>/presentation/`.
- Add `feature/<feature-name>/domain/` only when the feature has real business rules, use cases, domain models, or repository contracts. Domain code must not depend on Android, Compose, or data implementations.
- Add `feature/<feature-name>/data/` only when the feature owns persisted or remote data. Data implementations may depend on the feature domain contracts and shared core infrastructure.
- Name platform or persistence boundaries `*DataSource`, repository implementations `Default*Repository` or another explicit implementation name, domain orchestration `*UseCase`, transformations `*Mapper`, and presentation-only binary decoding `*Decoder`. ViewModels depend on use cases rather than repositories.
- Keep `Clock.systemDefaultZone()` in the Hilt composition module in `:app`; production code injects `Clock`, and deterministic tests use `Clock.fixed`.
- Store the mandatory last-period date as an ISO-8601 string in Preferences DataStore; its presence decides whether onboarding or the main shell starts.
- Resolve the effective app language through the read-only Android per-app language provider. It prioritizes Android's per-app setting, then the device language, and finally English; never read or write a separate app language preference.
- When the app returns to the foreground after a language change, reload language-dependent pregnancy content and refresh widgets.
- Keep feature-specific Retrofit services, DTOs, mappers, local sources, and repository implementations inside that feature's `data` layer.
- Store unified gallery metadata in Room and image bytes in app-private files; use Android Photo Picker for imports without broad media permissions.
- Limit each Photo Picker selection to 10 items, deduplicate source URIs, cap each private file at 25 MiB, and preserve successful items when a multi-item import partially fails.
- Lab is isolated by its application ID and must also use Lab-specific names for any explicitly named DataStores, databases, app-private directories, widget actions or identifiers, and MediaStore paths. Production names and data contracts must remain unchanged.
- Store the belly-tracking cadence as 7, 14, or 28 days in the feature-owned Preferences DataStore; invalid stored values fall back to 7 days.
- Label gallery items as imported or guided tracking. Deleting a guided tracking item removes only its private app copy and never deletes an exported MediaStore copy.
- Use CameraX for guided captures and request only the runtime camera permission. Save the private Room-backed copy first, then export a separate JPEG to `Pictures/Baby Loading` through MediaStore without storage permissions.
- Treat MediaStore export as recoverable: if it fails, preserve and report the successfully saved private gallery copy.
- Provide a single fixed 4x2 home-screen widget with Jetpack Glance. Refresh it immediately after pregnancy-date changes and at most daily while an ongoing, late-term, or postterm pregnancy is active; do not schedule a daily refresh for setup or invalid-date states, frequent background work, or notifications.
- Export Room schemas to `app/schemas/` and add migrations whenever the database version changes.
- Keep `BabyLoadingDatabase` in package `com.pablo.ruiz.babyloading.feature.gallery.data.local`; database version 1, the `gallery_items` schema, and persisted `Imported` / `GuidedTracking` values are compatibility contracts.
- Put genuinely reusable technical infrastructure in `core/`. Design-system code and assets belong to `:core:designsystem`; pregnancy JSON and fetal images belong to `:core:pregnancy-content`; application-specific databases remain with their owning feature.
- Keep weekly editorial pregnancy content local-only in validated `pregnancy-content.en.json` and `pregnancy-content.es.json` assets; do not add remote refresh behavior without an explicit product decision.
- Do not create empty packages or speculative layers. Create a package only with its first concrete production or test file.
- Keep visual application chrome in `app/shell/`; root and nested navigation contracts and hosts belong in `navigation/`.
- Keep navigation routes in a dedicated `navigation` package using `@Serializable` type-safe routes.
- Model top-level tabs as nested navigation graphs and preserve tab state with Navigation save/restore options.
- Keep tab-to-route mapping in `navigation`; `MainTab` owns only shell metadata such as label, icon, accessibility description, and test tag.
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

## Module Ownership

```text
:app
├── :feature:onboarding ──> :core:designsystem, :core:pregnancy
├── :feature:dashboard ───> :core:coroutines, :core:designsystem, :core:localization,
│                            :core:pregnancy, :core:pregnancy-content
├── :feature:journey ─────> :core:coroutines, :core:designsystem, :core:localization,
│                            :core:pregnancy, :core:pregnancy-content
├── :feature:gallery ─────> :core:coroutines, :core:designsystem, :core:pregnancy, :core:storage
├── :feature:settings ────> :core:designsystem, :core:localization, :core:pregnancy
├── :feature:widget ──────> :core:designsystem, :core:localization, :core:pregnancy,
│                            :core:pregnancy-content, :core:storage
└── composition-only core dependencies: :core:designsystem, :core:localization,
                                         :core:network, :core:pregnancy

:core:pregnancy ─────────> :core:storage
:core:pregnancy-content ─> :core:localization, :core:coroutines
```

There are no feature-to-feature module dependencies.

- `:app` owns `Application`, `Activity`, bootstrap and foreground composition, shell, root and nested navigation, the production `Clock`, API configuration, and the final manifest. It passes `BuildConfig.VERSION_NAME` into Settings and does not expose app `BuildConfig` to libraries.
- `:core:storage` owns `AppStorageConfig` and `AppStorageConfigFactory`, derived from `ApplicationContext.packageName`. Physical Prod and Lab names must never be reconstructed from a library `BuildConfig`.
- `:core:coroutines` owns qualified dispatchers and the shared `MainDispatcherRule` test fixture.
- `:core:designsystem` owns shared Compose components, theme, typography, fonts, font license, shared illustrations, and resource keep rules.
- `:core:localization` owns Android locale data access, language mapping, and observable effective-language changes.
- `:core:pregnancy` owns last-period persistence, pregnancy calculation and validation, repository contracts, and setup/save use cases.
- `:core:pregnancy-content` owns validated local JSON, weeks 6–40 fetal assets, content parsing/cache, repository implementation, and drawable mapping.
- `:core:network` owns shared HTTP clients, safe-call infrastructure, origin-restricted authentication, and `AccessTokenDataSource` / `AccessTokenRefreshDataSource` contracts.
- Each `:feature:*` module owns its screens, feature resources, ViewModels, use cases, models, and any feature-specific persistence or platform integration. Use the module's own generated `R` class.
- `:feature:gallery` owns both imported Gallery and Guided Tracking: Room metadata, private files, tracking DataStore, MediaStore export, CameraX, and both presentation packages.
- `:feature:widget` owns Glance UI/resources, widget state mapping, alarm data source/repository, and prepare/cancel orchestration. The receiver declaration remains in the `:app` manifest and its FQCN is stable.

## Persisted Compatibility

- Production and Lab DataStores are `pregnancy_preferences[-lab]` and `tracking_preferences[-lab]`; keys are `last_period_date` and `tracking_cadence_days`.
- Room databases are `baby-loading[-lab].db`. Keep version 1 and `app/schemas/1.json` unchanged unless a deliberate schema migration is introduced.
- Private image directories are `gallery[-lab]`; MediaStore paths are `Pictures/Baby Loading` and `Pictures/Baby Loading Lab`; filenames retain the configured `BabyLoading` / `BabyLoadingLab` prefix.
- The widget daily action is based on the real application ID through `AppStorageConfig`. Keep the fixed 4x2 provider XML, the receiver FQCN `com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidgetReceiver`, and the absence of WorkManager and notifications.
- Route internal widget alarms only through an explicit non-exported receiver. The exported Glance receiver must accept only app-widget and protected system broadcasts.

## Networking

- Shared HTTP infrastructure lives in the `:core:network` Android library module. The app depends on it, never the reverse.
- Keep Retrofit services, DTOs, mappers, remote data sources, and repository implementations in the owning feature's `data` layer.
- Use `NetworkResult` only at remote and data boundaries. Map `NetworkError` to feature domain errors before exposing results to presentation.
- Define feature API services with Retrofit `Response<T>` return types and execute them through `safeApiCall`.
- Use the authenticated Retrofit instance for normal feature traffic and the qualified unauthenticated instance for login, token refresh, or explicitly public clients.
- `AccessTokenDataSource` implementations must be thread-safe. `AccessTokenRefreshDataSource` implementations must use the unauthenticated client so refresh cannot recurse through the authenticator.
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
- Reuse `MainDispatcherRule` from `:core:coroutines` with `testImplementation(testFixtures(project(":core:coroutines")))` in modules whose ViewModel tests need the main dispatcher.
- Prefer small in-memory fake repositories/data sources beside the owning module's tests. Inject `Clock.fixed`, test locale providers explicitly, and keep Room integration tests under the Gallery module's `androidTest` source set.
- Changes to Gallery persistence must verify `app/schemas/1.json` has no diff when the database version remains 1.

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
