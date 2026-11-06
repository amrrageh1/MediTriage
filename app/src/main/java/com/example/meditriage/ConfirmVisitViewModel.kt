package com.example.meditriage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for ConfirmVisitFragment.
 */
class ConfirmVisitViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    private val _saveResult = MutableLiveData<Result<String>>()
    val saveResult: LiveData<Result<String>> = _saveResult

    fun saveHistory(visited: String) {
        val sessionId = TriageSession.currentSessionId
        if (sessionId == -1) {
            _saveResult.postValue(Result.success("skip"))
            return
        }

        val doctorId = TriageSession.matchedDoctor?.doctorId
        val labels = listOf("Fever", "Breathing", "Pain", "Dizziness", "Nausea", "Bleeding", "Duration")
        val symptomSummaries = TriageSession.answers.mapIndexed { i, ans ->
            "${labels.getOrElse(i) { "Symptom ${i + 1}" }}: $ans"
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.saveHistory(
                    HistoryRequest(
                        sessionId        = sessionId,
                        doctorId         = doctorId,
                        visited          = visited,
                        visitDate        = if (visited == "Y") today else null,
                        actualDiagnosis  = if (visited == "Y") TriageSession.aiResult?.possibleCondition else null,
                        symptomSummaries = symptomSummaries
                    )
                )
                // Audit log — silent failure is acceptable
                try {
                    repository.saveAuditLog(
                        AuditLogRequest(
                            patientId  = TriageSession.patientId,
                            actionType = if (visited == "Y") "VISIT_CONFIRMED" else "VISIT_DECLINED",
                            tableName  = "PATIENT_HISTORY",
                            recordId   = sessionId
                        )
                    )
                } catch (_: Exception) { /* silent */ }

                val msg = if (visited == "Y") "Visit recorded ✅" else "Noted — no visit yet"
                _saveResult.postValue(Result.success(msg))
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e))
            }
        }
    }
}
