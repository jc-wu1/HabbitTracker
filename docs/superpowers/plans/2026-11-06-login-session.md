# Login and encrypted session persistence

Implement POST https://dummyjson.com/user/login with username/password and expiresInMins=60. Persist every documented response field, including both tokens, as one encrypted session entry. Do not store the submitted password. Report success only after persistence completes.

- [x] Extract core:storage: one platform KSafe singleton, a small suspend SecureStore interface, and encrypted adapter. Preserve existing dependency versions. Add Android persistence/encryption integration proof.
- [x] Replace the auth login stub with remote data source and repository orchestration; test request/response, invalid credentials, malformed payloads, storage failure, cancellation, and write-before-success using MockEngine and fake secure storage.
- [x] Use username/nonblank password for login, retain signup email/password validation, and expose loading/failure/success through ViewModel state. Keep other auth operations unavailable.
- [x] Build Android/JS/Wasm, run all host tests and secure-storage device test, update architecture notes, sync IDE, and smoke-test safe UI paths.

Success UI: confirmation on the existing screen after a committed session write; no new signed-in destination, automatic restoration, refresh, logout, or backend signup flow in this task.

Verification: 26 host tests pass (auth 16, network 3, onboarding 1, shared 6). The Android storage device test passed after first failing for the missing write; it verifies encryption metadata and persistence across reopen. Android assembly, JS/Wasm web compilation, IDE sync, and app deployment succeeded. Safe emulator checks verified username validation, unchanged signup email/password guidance, retained navigation state, and construction of the new DI chain. Independent review found no actionable issues. iOS compilation remains unverified on Windows.

Live demo request was blocked by automatic approval review because the documentation's demo credentials were not explicitly authorized as a payload. No live request was made and no retry via another tool/device was attempted. Live verification remains optional and requires explicit approval.
