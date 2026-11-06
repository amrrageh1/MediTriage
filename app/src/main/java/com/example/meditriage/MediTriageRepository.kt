package com.example.meditriage

/**
 * Repository — single source of truth for all network operations.
 * Fragments never call ApiClient directly; they go through a ViewModel
 * which delegates here. This layer can be extended with Room caching
 * for offline-first support.
 */
class MediTriageRepository {

    // ── Auth ────────────────────────────────────────────────────────────────

    suspend fun registerPatient(req: RegisterRequest) =
        ApiClient.api.registerPatient(req)

    suspend fun loginPatient(req: LoginRequest) =
        ApiClient.api.loginPatient(req)

    // ── Sessions & Symptoms ─────────────────────────────────────────────────

    suspend fun createSession(patientId: Int) =
        ApiClient.api.createSession(SessionRequest(patientId))

    suspend fun saveSymptoms(sessionId: Int, req: SymptomsRequest) =
        ApiClient.api.saveSymptoms(sessionId, req)

    suspend fun saveDiagnosis(req: DiagnosisRequest) =
        ApiClient.api.saveDiagnosis(req)

    suspend fun completeSession(sessionId: Int) =
        ApiClient.api.completeSession(sessionId)

    // ── History ─────────────────────────────────────────────────────────────

    suspend fun getHistory(patientId: Int) =
        ApiClient.api.getHistory(patientId)

    suspend fun saveHistory(req: HistoryRequest) =
        ApiClient.api.saveHistory(req)

    // ── Doctors ─────────────────────────────────────────────────────────────

    suspend fun getDoctorBySpecialty(specialty: String) =
        ApiClient.api.getDoctorBySpecialty(specialty)

    // ── Audit ───────────────────────────────────────────────────────────────

    suspend fun saveAuditLog(req: AuditLogRequest) =
        ApiClient.api.saveAuditLog(req)
}
