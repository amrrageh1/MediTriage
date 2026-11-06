package com.example.meditriage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for LoginFragment and RegisterFragment.
 * Survives configuration changes (screen rotation) — no data loss.
 * Never touches the View layer directly.
 */
class AuthViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    // ── Register ─────────────────────────────────────────────────────────────

    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    fun register(req: RegisterRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.registerPatient(req)
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.patientId != null) {
                    _registerResult.postValue(Result.success(body))
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Unknown error"
                    _registerResult.postValue(Result.failure(Exception(errMsg)))
                }
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            }
        }
    }

    // ── Login ────────────────────────────────────────────────────────────────

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(req: LoginRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.loginPatient(req)
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.patientId != null) {
                    _loginResult.postValue(Result.success(body))
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Account not found"
                    _loginResult.postValue(Result.failure(Exception(errMsg)))
                }
            } catch (e: Exception) {
                _loginResult.postValue(Result.failure(e))
            }
        }
    }
}
