# MediTriage

Educational Android symptom-triage prototype built with Kotlin, XML views, ViewModel/LiveData, Retrofit, and a direct Gemini HTTP integration. The app includes registration/login, symptom entry, AI result display, doctor lookup, and visit history flows.

**Status:** Android source is now browsable. This is an incomplete prototype; a successful build and end-to-end operation have not been verified. Use synthetic data only, not real patient data or clinical decisions.

## Repository layout

- `app/src/main/java/com/example/meditriage/` — application code and API contract
- `app/src/main/res/` — layouts, navigation, themes, icons
- `app/src/test/` and `app/src/androidTest/` — original example tests
- `app/src/debug/res/xml/` — debug-only emulator HTTP allowance
- `gradle/` — dependency versions and wrapper configuration
- `docs/sample_schema.sql` — partial historical Oracle schema example
- `docs/database.md` — missing database components and secure setup guidance
- `SECURITY.md` — credential rotation and deployment limitations

## Build prerequisites and known blockers

The original archive specifies Android Gradle Plugin 9.1.1, Gradle 9.3.1, compile/target SDK 35, and minimum SDK 26. These settings are preserved, not certified as a working combination. Install a compatible Android Studio, Android SDK, and JDK. Dependency compatibility must be checked during the first full build.

**The original archive is missing `gradle/wrapper/gradle-wrapper.jar`.** The wrapper scripts cannot launch until it is restored from a trusted Gradle 9.3.1 installation. For example, generate a wrapper in an empty temporary project with `gradle wrapper --gradle-version 9.3.1`, then copy its wrapper JAR here; verify its provenance before committing. Keep the checked-in distribution checksum.

Open this root directory in Android Studio and configure the local SDK. Do not commit `local.properties`. After restoring the wrapper, run:

```sh
chmod +x gradlew  # Browser uploads do not preserve executable permissions
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The included tests are starter examples, not evidence of application correctness.

## Backend configuration

Debug builds default to `http://10.0.2.2:3000/`, the Android emulator's host address. Override the non-secret URL at build time:

```sh
./gradlew :app:assembleDebug -PMEDITRIAGE_API_BASE_URL=https://your-backend.example/
```

The URL must end in `/`. Release builds require HTTPS through their network policy; set an HTTPS backend URL when building a release. Debug cleartext is allowed only for `10.0.2.2`, `localhost`, and `127.0.0.1`. Physical devices require a reachable HTTPS backend or an explicitly reviewed debug-only local configuration.

The Node.js server and its package manifest are **not included**. Implement or restore the routes and payloads in `ApiService.kt` (patients/login, sessions, symptoms, diagnoses, history, audit, and doctor lookup). Oracle schema examples are incomplete and do not fully match those payloads. See [database setup](docs/database.md).

## AI integration

`AnalyzingViewModel.kt` contains a placeholder Gemini key. No working key is supplied; AI requests will not succeed as shipped. Move model calls to an authenticated backend and validate supported models there. Never replace the placeholder with a production key in this public Android project.

## Migration and security

Source was recovered from the original `MediTriage.zip`; local caches, machine paths, and macOS metadata were excluded. Original Kotlin source, resources, and example tests were preserved except for API URL configuration. All 90 cleaned files were verified on GitHub before the obsolete ZIP was removed from the current branch.

The old SQL administration scripts contained a database password, broad grants, and a destructive statement. Those were replaced with documentation, while partial table definitions were preserved under `docs/`. **Rotate the exposed database password if it was ever active.** Deleting current files does not erase Git history. See [security notes](SECURITY.md).
