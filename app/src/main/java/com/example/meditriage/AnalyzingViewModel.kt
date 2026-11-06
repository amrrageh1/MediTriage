package com.example.meditriage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * ViewModel for AnalyzingFragment.
 * Handles the AI call and Oracle persistence entirely off the main thread.
 * The Fragment observes [analysisResult] and navigates once it's ready.
 */
class AnalyzingViewModel : ViewModel() {

    private val repository = MediTriageRepository()

    // ⚠️ Replace with your actual key — never commit real secrets
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val models = listOf(
        "gemini-2.0-flash-lite",
        "gemini-2.0-flash",
        "gemini-2.5-flash",
        "gemini-flash-latest"
    )

    // Sealed state so the Fragment reacts to loading / success / failure
    sealed class AnalysisState {
        object Loading : AnalysisState()
        data class Success(val result: TriageAiResult) : AnalysisState()
        data class Error(val message: String) : AnalysisState()
    }

    private val _analysisState = MutableLiveData<AnalysisState>(AnalysisState.Loading)
    val analysisState: LiveData<AnalysisState> = _analysisState

    fun analyzeSymptoms(answers: List<String>) {
        if (answers.isEmpty()) {
            _analysisState.postValue(AnalysisState.Success(makeDefaultResult("No answers found")))
            return
        }

        val labels = listOf("Fever", "Breathing Difficulty", "Pain",
            "Dizziness", "Nausea or Vomiting", "Bleeding", "Duration")

        val answersText = answers.mapIndexed { i, a ->
            "${labels.getOrElse(i) { "Question ${i + 1}" }}: $a"
        }.joinToString("\n")

        val prompt = buildPrompt(answersText)

        viewModelScope.launch {
            var rawText = ""
            var success = false

            // Try each Gemini model on IO dispatcher
            for (model in models) {
                try {
                    val result = withContext(Dispatchers.IO) { callGeminiHttp(model, prompt) }
                    if (!result.isNullOrBlank()) {
                        rawText = result
                        success = true
                        Log.d("MediTriage", "Success with $model")
                        break
                    }
                } catch (e: Exception) {
                    Log.e("MediTriage", "$model failed: ${e.message}")
                }
            }

            val parsed = if (success) parseFlexible(rawText) else makeDefaultResult("AI unavailable")

            // Doctor lookup on IO dispatcher
            var matchedDoctor: DoctorResponse? = null
            try {
                matchedDoctor = withContext(Dispatchers.IO) {
                    val resp = repository.getDoctorBySpecialty(parsed.recommendedDoctor.trim())
                    if (resp.isSuccessful) resp.body()?.firstOrNull() else null
                }
            } catch (e: Exception) {
                Log.e("MediTriage", "Doctor lookup: ${e.message}")
            }

            TriageSession.matchedDoctor = matchedDoctor

            val finalResult = if (matchedDoctor != null) {
                parsed.copy(
                    recommendedDoctor = "Dr. ${matchedDoctor.firstName} ${matchedDoctor.lastName} — ${matchedDoctor.specialtyName}"
                )
            } else parsed

            // Persist to Oracle on IO dispatcher
            try {
                withContext(Dispatchers.IO) {
                    val sessionResp = repository.createSession(TriageSession.patientId)
                    val sessionId = sessionResp.body()?.sessionId ?: 1
                    TriageSession.currentSessionId = sessionId

                    repository.saveSymptoms(
                        sessionId,
                        SymptomsRequest(answers.mapIndexed { i, ans ->
                            SymptomAnswer(
                                symptomName   = labels.getOrElse(i) { "Symptom ${i + 1}" },
                                answerValue   = ans.take(10),
                                severityScore = when {
                                    ans.contains("severe", true) || ans.contains("high", true) -> 4
                                    ans.contains("mild", true)  || ans.contains("low", true)  -> 2
                                    else -> 3
                                },
                                durationDays = 1
                            )
                        })
                    )

                    val diagResp = repository.saveDiagnosis(
                        DiagnosisRequest(
                            sessionId            = sessionId,
                            primaryCondition     = finalResult.possibleCondition,
                            otherConditions      = "",
                            severityLevel        = finalResult.severity,
                            basicAdvice          = finalResult.advice,
                            recommendedSpecialty = finalResult.recommendedDoctor,
                            confidenceScore      = finalResult.confidence
                                .replace("%", "").trim().toDoubleOrNull() ?: 0.0,
                            doctorId             = matchedDoctor?.doctorId
                        )
                    )
                    TriageSession.currentDiagnosisId = diagResp.body()?.diagnosisId ?: -1
                    repository.completeSession(sessionId)
                }
            } catch (e: Exception) {
                Log.e("MediTriage", "Oracle save: ${e.message}")
            }

            TriageSession.aiResult = finalResult
            _analysisState.postValue(AnalysisState.Success(finalResult))
        }
    }

    private fun buildPrompt(answersText: String) = """
You are a medical triage assistant. Analyze these symptoms:

$answersText

Reply with EXACTLY these 5 lines:
Severity: Low Risk
Possible Condition: Common Cold
Confidence: 75%
Advice: Rest and drink fluids. See a doctor if symptoms worsen.
Recommended Doctor: General Practitioner

Rules:
- Severity must be: Low Risk, Medium Risk, or High Risk
- Recommended Doctor must be ONE of: General Practitioner, Cardiology, Pulmonology, Gastroenterology, Neurology, Dermatology, Orthopedics, ENT, Emergency Medicine, Internal Medicine
- No extra text, no markdown, no asterisks
    """.trimIndent()

    private suspend fun callGeminiHttp(modelName: String, prompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 300)
            })
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) return null

        val json = JSONObject(responseBody)
        return json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
    }

    private fun parseFlexible(text: String): TriageAiResult {
        val lines = text.lines().map { it.trim() }
        fun find(vararg keys: String): String {
            for (key in keys) {
                val line = lines.firstOrNull { l ->
                    l.replace("*", "").replace("#", "")
                        .startsWith(key, ignoreCase = true)
                }
                if (line != null) {
                    val v = line.substringAfter(":").trim()
                        .replace("**", "").replace("*", "").trim()
                    if (v.isNotEmpty()) return v
                }
            }
            return ""
        }

        val severity = find("Severity").let {
            when {
                it.contains("high", true) || it.contains("critical", true) -> "High Risk"
                it.contains("medium", true) || it.contains("moderate", true) -> "Medium Risk"
                it.contains("low", true) -> "Low Risk"
                it.isNotEmpty() -> it
                else -> "Medium Risk"
            }
        }

        return TriageAiResult(
            severity          = severity,
            possibleCondition = find("Possible Condition", "Condition").ifEmpty { "Consult a doctor" },
            confidence        = find("Confidence").let { if (it.contains("%")) it else "$it%" }.ifEmpty { "70%" },
            advice            = find("Advice", "Recommendation").ifEmpty { "Please consult a doctor." },
            recommendedDoctor = find("Recommended Doctor", "Doctor").ifEmpty { "General Practitioner" }
        )
    }

    private fun makeDefaultResult(reason: String) = TriageAiResult(
        severity = "Medium Risk", possibleCondition = reason,
        confidence = "N/A", advice = "Please consult a doctor.",
        recommendedDoctor = "General Practitioner"
    )
}
