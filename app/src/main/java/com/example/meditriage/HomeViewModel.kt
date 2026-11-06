package com.example.meditriage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for HomeFragment.
 * Loads patient history and exposes it via LiveData so the Fragment
 * simply observes — no direct API calls in the View layer.
 */
class HomeViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    private val _historyResult = MutableLiveData<Result<List<HistoryItem>>>()
    val historyResult: LiveData<Result<List<HistoryItem>>> = _historyResult

    fun loadHistory(patientId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getHistory(patientId)
                if (response.isSuccessful) {
                    _historyResult.postValue(Result.success(response.body() ?: emptyList()))
                } else {
                    _historyResult.postValue(Result.failure(Exception("Server error")))
                }
            } catch (e: Exception) {
                _historyResult.postValue(Result.failure(e))
            }
        }
    }
}
