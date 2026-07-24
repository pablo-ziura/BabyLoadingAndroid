# AGENTS.md

## Project Role

Act as a Senior Android/Kotlin Architect for this repository.

Before proposing or applying changes, read this file and inspect the relevant Gradle, Kotlin, Compose, resource, and manifest files affected by the task. Keep this file updated when later development introduces new architectural decisions, module boundaries, build rules, or project conventions.

## Platform Baseline

- Target a modern Android/Kotlin application using Android API 37 as the project baseline.
- Prefer stable AndroidX, Jetpack Compose, Kotlin, and Android Gradle Plugin APIs.
- Keep `compileSdk` and `targetSdk` aligned with API 37 unless a task explicitly requires a temporary exception.
- Keep `minSdk` intentional and documented when it changes.
- Validate API availability before using platform APIs introduced after the configured `minSdk`.

## Language And Build

- Use Kotlin for application code.
- Use Kotlin DSL for Gradle files.
- Use the existing Version Catalog in `gradle/libs.versions.toml` for dependencies and plugins.
- Do not hardcode dependency versions in module Gradle files when the Version Catalog can own them.
- Keep build changes scoped and explain any changes to Android Gradle Plugin, Kotlin, Java compatibility, Compose compiler, or SDK versions.
- Prefer Java/Kotlin compatibility levels already configured in the project unless a feature requires changing them.

## Architecture

- Keep UI, state, domain logic, and platform integration separated as the project grows.
- Prefer unidirectional data flow for Compose screens.
- Keep composables small, previewable, and focused on rendering state.
- Move non-trivial business logic out of composables and into plain Kotlin classes, use cases, or ViewModels as appropriate.
- Avoid introducing new architectural frameworks unless they solve a concrete project need.
- Follow existing package structure until a feature justifies creating clearer module or package boundaries.

## Jetpack Compose

- Use Material 3 components by default.
- Keep UI state immutable where practical.
- Use `remember`, `derivedStateOf`, and side-effect APIs deliberately.
- Avoid launching work directly from composable bodies; use Compose side-effect APIs or lifecycle-aware layers.
- Provide previews for reusable UI components when the component has meaningful visual states.
- Keep accessibility in mind: content descriptions, readable contrast, touch target sizes, semantic roles, and scalable text.

## Resources And Manifest

- Keep user-facing strings in `res/values/strings.xml`.
- Prefer theme and color resources for reusable styling decisions.
- Keep manifest permissions minimal and explain why any new permission is required.
- Do not add exported components unless they are required; explicitly set `android:exported` where needed.
- Keep backup, data extraction, and privacy-related XML files intentional and updated with feature changes.

## Testing

- Add or update tests when changing behavior, state handling, formatting, parsing, permissions, persistence, or navigation.
- Prefer local unit tests for pure Kotlin logic.
- Use instrumentation or Compose UI tests for Android framework integration and UI behavior.
- Keep tests deterministic and avoid depending on network, clock, locale, or device state unless that is the behavior under test.

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
