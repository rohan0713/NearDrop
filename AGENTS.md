# Project Baseline & Memory: NearDrop

> **CRITICAL INSTRUCTION FOR ALL AGENTS & SESSIONS:**
> This file is the single source of truth for the NearDrop project rules, architecture, conventions, and operational baselines established by the user. 
> You MUST read and follow these baselines across all conversations until explicitly instructed by the user to change them.
> Whenever the user establishes a new rule, architectural decision, or convention, update this document immediately.

---

## 1. Project Overview & Technology Stack

- **Project Name:** NearDrop
- **Architecture Model:** Kotlin Multiplatform (KMP)
- **Application ID / Package Name:** `com.drop.near`
- **JDK Target:** JDK 17 (Temurin recommended)
- **Android SDK Targets:** `compileSdk = 37`, `targetSdk = 37`, `minSdk = 24`

### UI Paradigm: Strictly Native (No Shared UI)
- **NO Shared UI:** Under no circumstances should Compose Multiplatform or shared UI frameworks be introduced into `shared/`.
- **Android UI:** 100% native Android in `androidApp/` (Jetpack Compose, Material 3, Android Jetpack).
- **iOS UI:** 100% native iOS in `iosApp/` (SwiftUI / UIKit).
- **Shared Code (`shared/`):** Contains purely business logic, domain models, repository interfaces, data sources, platform abstractions (via `expect`/`actual`), and utilities.

### Keystore & Release Signing Resolution Hierarchy
For release builds (`:androidApp:bundleRelease` or `:androidApp:assembleRelease`), credentials are automatically resolved in this priority order:
1. `keystore.properties` (in `androidApp/` or root project)
2. `local.properties` (keys: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`)
3. Environment variables (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — used by CI/CD)
4. Local file `release.keystore` (in `androidApp/` or root project)
5. Fallback: Android debug keystore (`~/.android/debug.keystore`) for local development if no release keystore is found.

---

## 2. Architecture & Code Conventions

### Clean Architecture Layers
1. **Domain Layer (`shared/.../domain`):**
   - Pure Kotlin, zero Android/iOS framework dependencies.
   - Contains Entities, Value Objects, Repository Interfaces, and Use Cases / Interactors.
2. **Data Layer (`shared/.../data`):**
   - Implements Domain Repository interfaces.
   - Contains data sources (local cache, remote API, Bluetooth/Wi-Fi Direct engines), mappers, and DTOs.
3. **Core Layer (`shared/.../core`):**
   - Result handling via `AppResult<T>` and `AppError`.
   - Asynchronous execution and threading via injected `CoroutineDispatchers` (no hardcoded `Dispatchers.IO`).
4. **Presentation Layer (Native per platform):**
   - MVI / MVVM state flow patterns.
   - Exposes immutable State (`StateFlow`) and Events/Effects to the UI.

### Coding Principles
- Strict Object-Oriented Programming (OOP) and SOLID design principles.
- High cohesion, loose coupling, dependency inversion.
- Consistent error handling using `AppResult.Success` and `AppResult.Error(AppError)`.

---

## 3. Firebase & Cloud Services

- **Firebase Project ID:** `neardrop-968eb`
- **Firebase Android App ID:** `1:107890352208:android:4329ca640f57eb44a6f155`
- **Firebase Testing Group:** `bluetooth-testers`
- **Configuration Files:**
  - `androidApp/google-services.json`
  - `firebase.json`
  - `.firebaserc`

---

## 4. Git Branching & CI/CD Deployment Workflows

Automated deployments are driven entirely by GitHub Actions:

### 1. Feature Branches (`feature/**`, `feature_*`)
- **Workflow File:** `.github/workflows/firebase-app-distribution.yml`
- **Trigger:** Push to `feature/**` or `feature_*` branches, or manual `workflow_dispatch`.
- **Output:** Builds Release APK (`:androidApp:assembleRelease`).
- **Destination:** Distributes to Firebase App Distribution targeting tester group `bluetooth-testers`.

### 2. Production Branch (`production`)
- **Workflow File:** `.github/workflows/play-store-deployment.yml`
- **Trigger:** Push or PR merge to `production`, or manual `workflow_dispatch`.
- **Output:** Builds Signed Android App Bundle (`:androidApp:bundleRelease`).
- **Destination:** Publishes to Google Play Store track (`internal` by default).

### 3. Strict Branch Protection & Push Policy
- **NEVER push to `production` until explicitly instructed by the user.**
- **ALWAYS push to feature/development branches** (`feature/**`, `feature_*`, or working task branches) to protect the product from unnecessary builds, unintentional releases, and CI/CD costs.

### 4. GitHub Actions Hard Rule
- **Never evaluate secrets directly in `if:` conditionals:**
  - ❌ `if: ${{ secrets.MY_SECRET != '' }}` causes `Unrecognized named-value: 'secrets'`.
  -  Map the secret to a job-level environment variable (`jobs.<job_id>.env`), then check `if: ${{ env.MY_SECRET != '' }}`.

### 5. Play Store Release Versioning Rule
- **ALWAYS prompt the user for `versionCode` and `versionName` before pushing to `production` or triggering a Google Play Store release.**
- Never assume, hardcode, or automatically increment versions without explicit confirmation from the user.

---

## 5. Maintenance Protocol

- When the user gives a new rule, baseline requirement, or workflow change:
  1. Immediately apply the change to the codebase.
  2. Update this `AGENTS.md` file to record the new baseline.
  3. Ensure no regressions against established conventions.
