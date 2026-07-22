# MediTriage 🩺

**An AI-powered Android triage app that guides patients through a symptom questionnaire, analyzes responses with Google's Gemini API, and connects them to the right specialist.**

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Architecture](#architecture)
- [Fragment Navigation Map](#fragment-navigation-map)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Minimum SDK](#minimum-sdk)
- [AI-Assisted Code](#ai-assisted-code)
- [Project Deliverables](#project-deliverables)

---

## Overview

MediTriage is an AI-powered medical triage app built for Android using Kotlin. It guides patients through a structured symptom questionnaire, analyzes responses with the **Google Gemini API**, and provides a severity assessment, probable condition, medical advice, and a recommended specialist — all matched against a live Oracle database of real doctors.

## Features

- 🔐 **User authentication** — register and log in (`RegisterFragment`, `LoginFragment`)
- 📋 **Structured symptom questionnaire** — guided triage flow (`TriageFragment`)
- 🤖 **AI-powered analysis** — symptoms sent to the Gemini API for assessment (`AnalyzingFragment` / `AnalyzingViewModel`)
- 📊 **Triage results** — severity level, probable condition, medical advice, and recommended specialist (`ResultFragment`)
- 👨‍⚕️ **Doctor matching** — recommended specialists pulled from a live Oracle database (`DoctorProfileFragment`)
- 📅 **Visit confirmation** — book/confirm a visit with the matched doctor (`ConfirmVisitFragment`)
- 🕓 **Triage history** — view past triage sessions (`HistoryFragment`)
- 👤 **Profile & logout** (`ProfileFragment`)

## Screenshots

<p align="center">
  <img src="screenshots/screenshot-01.png" width="180"/>
  <img src="screenshots/screenshot-02.png" width="180"/>
  <img src="screenshots/screenshot-03.png" width="180"/>
  <img src="screenshots/screenshot-04.png" width="180"/>
  <img src="screenshots/screenshot-05.png" width="180"/>
</p>
<p align="center">
  <img src="screenshots/screenshot-06.png" width="180"/>
  <img src="screenshots/screenshot-07.png" width="180"/>
  <img src="screenshots/screenshot-08.png" width="180"/>
  <img src="screenshots/screenshot-09.png" width="180"/>
  <img src="screenshots/screenshot-10.png" width="180"/>
</p>

> Pulled from the project report — rename the files in `screenshots/` to match each actual screen (e.g. `login.png`, `home.png`, `triage.png`) if you'd like clearer labels.

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

## Project Structure

```
MediTriage/
├── app/
│   ├── src/main/java/com/example/meditriage/
│   │   ├── MainActivity.kt
│   │   ├── SplashFragment.kt
│   │   ├── LoginFragment.kt / RegisterFragment.kt / AuthViewModel.kt
│   │   ├── HomeFragment.kt / HomeViewModel.kt
│   │   ├── TriageFragment.kt / TriageSession.kt
│   │   ├── AnalyzingFragment.kt / AnalyzingViewModel.kt / TriageAiResult.kt
│   │   ├── ResultFragment.kt
│   │   ├── DoctorProfileFragment.kt / DoctorProfileViewModel.kt
│   │   ├── ConfirmVisitFragment.kt / ConfirmVisitViewModel.kt
│   │   ├── HistoryFragment.kt / HistoryViewModel.kt
│   │   ├── ProfileFragment.kt
│   │   ├── MediTriageRepository.kt
│   │   ├── ApiClient.kt / ApiService.kt
│   ├── src/test/          # unit tests
│   └── src/androidTest/   # instrumented tests
├── gradle/
├── meditriage_database.sql   # Oracle DB schema
├── queries_database.txt      # sample queries
└── README.md
```

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or newer
- Android SDK API 26+
- A running instance of the Node.js Oracle backend (see backend repo, if published separately)

### Configuration

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/MediTriage.git
   ```

2. **Set your Gemini API key** — open `AnalyzingViewModel.kt` and replace the placeholder:
   ```kotlin
   private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
   ```
   > ⚠️ Never commit a real API key. Keep the placeholder in version control and load your real key locally (e.g. from `local.properties` or an environment variable) instead.

3. **Set your backend URL** — open `ApiClient.kt` and update:
   ```kotlin
   private const val BASE_URL = "http://<YOUR_PC_IP>:3000/"
   ```
   Your PC's local IP (e.g. `192.168.1.x`) must be reachable from the emulator/device.

4. **Sync Gradle** — Android Studio will prompt you. Click *Sync Now*.

5. **Run on emulator or device** — minimum API 26 (Android 8.0 Oreo).

## Minimum SDK

- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 36
- **compileSdk**: 36

## AI-Assisted Code

Sections annotated with `// AI-assisted` in source files indicate areas where AI tooling helped with boilerplate patterns (e.g., ViewBinding inflation/destruction pattern, ViewModel `by viewModels()` delegation, Retrofit service/response scaffolding). All architecture decisions, Fragment navigation logic, Oracle schema design, and business logic are original work by the team.

## Project Deliverables

- 📄 [Final Report](../../releases/latest)
- 📊 [Presentation Slides](../../releases/latest)
- 🎥 [Demo Video](../../releases/latest)

*(Uploaded as assets on the [Releases](../../releases) page rather than committed directly, since they're large binary files.)*
