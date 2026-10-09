# Learning Dashboard (Kotlin Multiplatform, Compose Multiplatform, MVVM)

**Run:** open in Android Studio (JDK 17, Gradle 8.9 pinned in the wrapper) and run `composeApp`.
**Test:** `./gradlew :composeApp:testDebugUnitTest`
**Login credentials:** Email `test@example.com` (any valid email format works) / Password `password123`.
**Offline demo:** load courses, flip the "Offline" switch, press Refresh.

## 1. Architecture
MVVM with a repository layer: `Compose UI -> ViewModel (StateFlow<UiState>) -> Repository -> Mock API + SQLDelight`.
- Each layer has one job, so UI, business logic and data can be tested and replaced independently (the mock API can become a Ktor client without touching any other layer).
- Screens render a single immutable `UiState` (Loading / Success / Empty / Error), so every state is handled explicitly.
- Kotlin Multiplatform keeps ViewModels, repositories, DB and UI in `commonMain`; only the DB driver and back handler are platform-specific (`expect/actual`).
- Manual DI (`AppContainer`) keeps it simple at this size; Koin would be the next step.

## 2. Offline Support
Offline-first with SQLDelight (SQLite) as the single source of truth.
- The UI only observes database Flows. The repository fetches from the API and writes into the DB; the UI updates automatically.
- If a refresh fails, cached courses stay visible with an "Offline" banner. Data also survives app restarts.
- Marking a lesson complete runs in one DB transaction that also recalculates course progress. A refresh never overwrites local progress.

## 3. Security
- Store access/refresh tokens in **Android Keystore-backed EncryptedSharedPreferences/DataStore** (iOS: **Keychain**), never in plain prefs, the DB or logs.
- Short-lived access token + rotating refresh token, HTTPS only with certificate pinning, tokens cleared on logout/401, `allowBackup=false`.
- Real login would use OAuth2/OIDC (PKCE) rather than a password sent to a custom endpoint.

## 4. Scale (1M users, hundreds of courses)
1. **Pagination and server-side filtering/search** (Paging 3 or cursor-based) instead of loading all courses.
2. **Sync progress to the server** with a pending-changes queue (WorkManager / BGTaskScheduler), idempotent writes and conflict rules.
3. **HTTP caching and CDN** (ETag/If-None-Match) plus incremental fetches ("changed since") to cut bandwidth and backend load.
4. **Observability**: crash reporting, structured logging, performance and API-error metrics, remote feature flags and staged rollouts.
5. **Modularization and CI/CD**: feature modules, Koin DI, automated tests (ViewModel, DB migrations, UI), schema migrations.

## 5. Second Platform (iOS)
Because the app is KMP, the shared module already compiles for iOS (`iosMain` has the SQLite driver and `MainViewController`).
- **Fastest:** host `MainViewController()` in a thin SwiftUI/Xcode app, reusing the same Compose UI and logic.
- **Fully native UI:** keep the shared ViewModels/repositories and write SwiftUI screens that observe the `StateFlow`s (via SKIE or a small Flow wrapper). Alternatively, rebuild natively: SwiftUI + `@Observable` ViewModels, `async/await` URLSession, SwiftData/Core Data for the cache, Keychain for tokens, same repository-as-source-of-truth design.
