# Gradle module extraction plan

Goal: turn package boundaries into separately compiled Kotlin Multiplatform modules without changing application behavior.

Keep existing dependency versions, Koin annotations/compiler checks, platform targets, preference file/key, and entry points. Preserve uncommitted work. Work in the current IDE workspace because its buffers are authoritative.

- [x] Extract core:network, including HTTP client DI and host tests. Verify module tests, app build, JS/Wasm compilation.
- [x] Extract core:designsystem (theme/components) and core:navigation (destination contracts). Keep app graph/feature composition in shared. Verify app and web compilation.
- [x] Extract feature:onboarding, including platform storage factories, UI resources, and tests. Verify host tests, app build, JS/Wasm compilation.
- [x] Extract feature:auth, including login/register, validation, repository, resources, and tests. Verify host tests, app build, JS/Wasm compilation.
- [x] Narrow shared dependencies, update architecture documentation, synchronize IDE, and smoke-test navigation/DI.

Default: auth remains one feature because login/register share state, validation, rendering, and data access. App-specific initKoin stays in shared; each extracted module owns its own annotated bindings. No feature-to-feature or core-to-feature dependencies.

Verification baseline: existing shared tests and Android/JS/Wasm builds passed before extraction.

Completed verification: core:network 3 tests, feature:onboarding 1 test, feature:auth 4 tests, shared 6 tests (14 total). Android debug assembly and both webApp JS/Wasm compilation tasks passed. IDE Gradle sync and emulator deployment succeeded. Emulator checks verified validation, registration navigation, auth repository discovery, common dialog resources, dismissal, and retained login state. Independent review found no actionable introduced findings. Native iOS compilation remains unverified on Windows.

Ownership decision: AppViewModel and its tests stay in shared under startup; AppModule owns its Koin scan. Feature:onboarding owns persistent preferences and onboarding UI.
