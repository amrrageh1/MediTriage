# Security status

MediTriage is an educational prototype, not a validated clinical system. Use synthetic data only.

- Rotate the Oracle password formerly committed in both SQL files if it was ever used. Historical commits and archive copies still require consideration.
- Never put production Gemini keys or database credentials in Android source, Gradle properties, or BuildConfig: values shipped in an APK are extractable. The checked-in Gemini value is only a placeholder. Move AI calls and credentials to an authenticated backend before enabling production use.
- Release network policy rejects cleartext HTTP. Debug builds allow it only for emulator/loopback hosts. Use HTTPS for other hosts.
- The API contract uses email/phone login and client-supplied patient IDs; backend authentication and authorization cannot be verified because the server is absent.
- Review diagnostic logging, backup behavior, patient-data retention, AI output validation, and failure handling before any real deployment. Existing fallback triage results and confidence values are not clinically validated.

Report vulnerabilities privately to the repository owner through an established private channel. Do not include secrets or patient information in public issues.
