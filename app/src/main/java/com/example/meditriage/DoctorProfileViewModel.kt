package com.example.meditriage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for DoctorProfileFragment.
 * Handles audit logging off the main thread.
 */
class DoctorProfileViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    fun logDoctorView(patientId: Int, doctorId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveAuditLog(
                    AuditLogRequest(
                        patientId  = patientId,
                        actionType = "VIEW_DOCTOR_PROFILE",
                        tableName  = "DOCTOR",
                        recordId   = doctorId
                    )
                )
            } catch (_: Exception) { /* silent */ }
        }
    }
}
