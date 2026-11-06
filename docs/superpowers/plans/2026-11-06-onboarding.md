# Shared onboarding implementation plan

**Goal:** Replace the starter UI with the approved three-slide language-learning onboarding on Android, iOS, and web.

**Design:** Follow the supplied screenshots and illustrations: pale background, indigo actions, square illustrations, left-aligned copy, three page dots, Next on slides 1–2 and Get started on slide 3. Omit the account link. Skip jumps to slide 3. Get started exposes a callback without adding a destination or persisting completion.

**Approach:** Shared Compose pager and Compose resources; Android-only implementation was rejected because the user explicitly requires all three platforms. Preserve existing staged Android illustrations and copy them into common resources. Reuse existing Compose dependencies; no DI or storage changes.

1. Copy the three PNGs into shared composeResources and add string resources. Verify resource generation during compilation.
2. Implement OnboardingScreen and a light Material theme in App. Keep the layout centered and width-limited on wide windows; allow slide content to scroll on short windows or large fonts. Use rememberPagerState for saved page state and Material buttons for keyboard/touch interaction.
3. Verify Android assembly, JS/Wasm compilation and shared host tests. Inspect the slides and exercise Next, swipe and Skip on an emulator when available. iOS execution requires macOS.

**Verification scope:** This is a reversible UI-only change with no business logic; use build checks and rendered/device interaction checks rather than adding test infrastructure or tests of Compose's own pager implementation.
