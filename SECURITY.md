# Security Status

## CVE-2026-53914: Pending Kotlin Build Cache Remediation

- **Status:** Pending and not remediated.
- **Affected project version:** Kotlin 2.4.10.
- **Risk:** Kotlin versions before 2.4.20 can execute code through unsafe deserialization of build cache metadata.
- **Runtime impact:** The issue affects the build toolchain and its cache metadata, not the installed Android application runtime.

Until a stable patched Kotlin version is available and validated:

- Do not configure or consume remote or shared Gradle/Kotlin build caches.
- Do not reuse cache artifacts produced by untrusted branches, forks, users, or CI jobs.
- Use only isolated local or per-job caches generated from trusted source and dependencies.
- Treat unexpected build cache archives or metadata as untrusted input.

This risk can be closed only after all of the following conditions are met:

1. Kotlin 2.4.20 or a newer stable release is publicly available.
2. The selected version is compatible with the configured Android Gradle Plugin, Compose compiler plugin, KSP, and Kotlinx Serialization plugin.
3. Debug, Lab, Release, unit-test, and Android-test builds pass with the patched version.
4. This document and the temporary build-cache restriction in `AGENTS.md` are removed or updated in the same dependency upgrade.

References:

- [Kotlin release process](https://kotlinlang.org/docs/releases.html)
- [CVE-2026-53914 advisory](https://github.com/advisories/GHSA-r937-wjx7-w2jp)
