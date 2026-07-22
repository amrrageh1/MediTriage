# MediTriage — Android Application

## Overview

MediTriage is an AI-powered medical triage app built for Android using Kotlin. It guides patients through a structured symptom questionnaire, analyzes responses with the Google Gemini API, and provides a severity assessment, probable condition, medical advice, and a recommended specialist — all matched against a live Oracle database of real doctors.

## Architecture

The app follows strict **MVVM (Model–View–ViewModel)** architecture:

```
View (Fragments)
    │  observes LiveData / sealed states
    ▼
ViewModel (AuthViewModel, HomeViewModel, HistoryViewModel,
           AnalyzingViewModel, ConfirmVisitViewModel, DoctorProfileViewModel)
    │  calls
    ▼
Repository (MediTriageRepository)
    │  calls via Retrofit + Coroutines on Dispatchers.IO
    ▼
Remote Data Source (Oracle REST API via ApiService)
```

- Fragments **never** call the API directly — all network calls go through a ViewModel → Repository.
- ViewModels survive configuration changes (screen rotation) — no data is lost.
- All database/network operations run on `Dispatchers.IO` via Kotlin Coroutines.
- Fragments observe LiveData with `viewLifecycleOwner` to prevent memory leaks.
- ViewBinding is used throughout — no `findViewById()` calls anywhere.
- All string literals are defined in `res/values/strings.xml`.

## Fragment Navigation Map

```
SplashFragment
  ├── → RegisterFragment → HomeFragment
  └── → LoginFragment   → HomeFragment

HomeFragment
  ├── → TriageFragment → AnalyzingFragment → ResultFragment
  │                                          ├── → DoctorProfileFragment
  │                                          └── → ConfirmVisitFragment → HistoryFragment
  ├── → HistoryFragment
  └── → ProfileFragment → SplashFragment (logout)
```

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (no Java) |
| Architecture | MVVM + Repository |
| UI | XML layouts, ViewBinding |
| Navigation | Android Navigation Component |
| Networking | Retrofit 2 + Gson |
| Async | Kotlin Coroutines |
| AI | Google Gemini API (via OkHttp) |
| Remote DB | Oracle (via Node.js REST backend) |

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or newer
- Android SDK API 26+
- A running instance of the Node.js Oracle backend (see backend repo)

### Configuration

1. **Clone the repository** using your university email GitHub account.

2. **Set your Gemini API key** — open `AnalyzingViewModel.kt` and replace the placeholder:
   ```kotlin
   private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
   ```
   > ⚠️ Never commit a real API key. Use a placeholder in version control.

3. **Set your backend URL** — open `ApiClient.kt` and update:
   ```kotlin
   private const val BASE_URL = "http://<YOUR_PC_IP>:3000/"
   ```
   Your PC's local IP (e.g. `192.168.1.x`) must be reachable from the emulator/device.

4. **Sync Gradle** — Android Studio will prompt you. Click *Sync Now*.

5. **Run on emulator or device** — minimum API 26 (Android 8.0 Oreo).

### Minimum SDK

- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 36
- **compileSdk**: 36

## AI-Assisted Code

Sections annotated with `// AI-assisted` in source files indicate areas where AI tooling helped with boilerplate patterns (e.g., ViewBinding inflation/destruction pattern, ViewModel `by viewModels()` delegation). All architecture decisions, Room schema design, Fragment navigation logic, and business logic are original work by the team.

