package com.example.meditriage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for HistoryFragment.
 */
class HistoryViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    private val _historyItems = MutableLiveData<Result<List<HistoryItem>>>()
    val historyItems: LiveData<Result<List<HistoryItem>>> = _historyItems

    fun loadHistory(patientId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getHistory(patientId)
                if (response.isSuccessful) {
                    _historyItems.postValue(Result.success(response.body() ?: emptyList()))
                } else {
                    _historyItems.postValue(Result.failure(Exception("Server error")))
                }
            } catch (e: Exception) {
                _historyItems.postValue(Result.failure(e))
            }
        }
    }
}
