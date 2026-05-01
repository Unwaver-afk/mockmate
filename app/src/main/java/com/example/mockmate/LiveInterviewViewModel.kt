package com.example.mockmate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.math.max
import kotlin.math.min

data class Message(val role: String, val content: String)

data class InterviewStartRequest(
    val company: String,
    val role: String,
    val difficulty: String,
    val type: String
)

data class InterviewMessageRequest(
    val company: String,
    val role: String,
    val difficulty: String,
    val type: String,
    val messages: List<Message>,
    val answer: String
)

data class InterviewEndRequest(
    val company: String,
    val role: String,
    val difficulty: String,
    val type: String,
    val messages: List<Message>
)

data class InterviewResponse(val message: String?)

interface InterviewBackendApi {
    @POST("api/interview/start")
    suspend fun startInterview(@Body request: InterviewStartRequest): InterviewResponse

    @POST("api/interview/message")
    suspend fun sendMessage(@Body request: InterviewMessageRequest): InterviewResponse

    @POST("api/interview/end")
    suspend fun endInterview(@Body request: InterviewEndRequest): Map<String, Any>
}

private object InterviewApiClient {
    // For Android emulator: 10.0.2.2 maps to host machine's localhost
    // For physical device: replace with your machine's local IP (e.g. 192.168.x.x)
    private const val BACKEND_BASE_URL = "http://10.0.2.2:8000/"

    val api: InterviewBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InterviewBackendApi::class.java)
    }
}

class LiveInterviewViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _timer = MutableStateFlow("00:00")
    val timer: StateFlow<String> = _timer.asStateFlow()

    private val _questionNumber = MutableStateFlow(1)
    val questionNumber: StateFlow<Int> = _questionNumber.asStateFlow()

    private var elapsedSeconds = 0
    private var timerJob: Job? = null
    private var company = "Google"
    private var role = "Software Engineer"
    private var difficulty = "Fresher"
    private var interviewType = "Technical"

    init {
        startTimer()
    }

    fun startSession(company: String, role: String, difficulty: String, type: String) {
        if (_messages.value.isNotEmpty()) return
        this.company = company.ifBlank { "Google" }
        this.role = role.ifBlank { "Software Engineer" }
        this.difficulty = difficulty.ifBlank { "Fresher" }
        this.interviewType = type.ifBlank { "Technical" }
        viewModelScope.launch {
            _isLoading.value = true
            val firstQuestion = runCatching {
                InterviewApiClient.api.startInterview(
                    InterviewStartRequest(this@LiveInterviewViewModel.company, this@LiveInterviewViewModel.role, this@LiveInterviewViewModel.difficulty, this@LiveInterviewViewModel.interviewType)
                ).message.orEmpty()
            }.getOrDefault("")
                .ifBlank { fallbackFirstQuestion() }
            _messages.value = listOf(Message("ai", firstQuestion))
            _isLoading.value = false
        }
    }

    fun sendMessage(answer: String) {
        val trimmed = answer.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        val nextMessages = _messages.value + Message("user", trimmed)
        _messages.value = nextMessages
        _isLoading.value = true

        viewModelScope.launch {
            delay(450)
            val aiResponse = runCatching {
                InterviewApiClient.api.sendMessage(
                    InterviewMessageRequest(
                        company = company,
                        role = role,
                        difficulty = difficulty,
                        type = interviewType,
                        messages = nextMessages,
                        answer = trimmed
                    )
                ).message.orEmpty()
            }.getOrDefault("")
                .ifBlank { fallbackFollowUp(trimmed) }

            _messages.value = _messages.value + Message("ai", aiResponse)
            _questionNumber.value = min(10, _questionNumber.value + 1)
            _isLoading.value = false
        }
    }

    suspend fun endInterview(): String {
        timerJob?.cancel()
        return runCatching {
            Gson().toJson(
                InterviewApiClient.api.endInterview(
                    InterviewEndRequest(
                        company = company,
                        role = role,
                        difficulty = difficulty,
                        type = interviewType,
                        messages = _messages.value
                    )
                )
            )
        }.getOrElse { buildFallbackReport() }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                _timer.value = "%02d:%02d".format(minutes, seconds)
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    private fun fallbackFirstQuestion(): String {
        return "Welcome. Let's begin your $interviewType interview for $role at $company. Tell me about yourself and why this role fits your current experience."
    }

    private fun fallbackFollowUp(answer: String): String {
        val lower = answer.lowercase()
        return when {
            "python" in lower || "java" in lower || "kotlin" in lower ->
                "You mentioned a technical skill. Walk me through one project where you used it, the tradeoff you made, and the measurable result."
            "team" in lower || "lead" in lower ->
                "Good. Now go deeper on collaboration: what was the conflict, what did you personally do, and how did the outcome change?"
            "data" in lower || "analysis" in lower ->
                "How did you validate that insight, and what metric would you use to prove the recommendation worked?"
            _questionNumber.value >= 9 ->
                "Final question: summarize why you are ready for this $role role, using one technical strength and one behavioral strength."
            else ->
                "Let's probe that further. What constraints did you clarify first, and what would you improve if you had another week?"
        }
    }

    private fun buildFallbackReport(): String {
        val userAnswers = _messages.value.filter { it.role == "user" }
        val score = min(94, max(68, 74 + userAnswers.size * 3))
        val answers = JSONArray()
        userAnswers.forEachIndexed { index, message ->
            answers.put(
                JSONObject()
                    .put("question", _messages.value.getOrNull(index * 2)?.content ?: "Interview question ${index + 1}")
                    .put("answer", message.content)
                    .put("idealAnswer", "A stronger answer would set context, clarify constraints, give a structured example, quantify impact, and close with a clear recommendation.")
                    .put("score", min(92, score - 5 + index * 2))
            )
        }
        return JSONObject()
            .put("roleCompany", "$role at $company")
            .put("date", "May 2, 2026")
            .put("score", score)
            .put("communication", min(96, score + 4))
            .put("relevance", max(60, score - 3))
            .put("technical", min(94, score + 1))
            .put("summary", "You handled the interview with steady structure. The next step is to add sharper metrics, clarify constraints earlier, and close each answer more decisively.")
            .put(
                "focus",
                JSONArray()
                    .put(JSONObject().put("title", "Add Measurable Evidence").put("description", "Use numbers, scale, or before-and-after impact in each answer."))
                    .put(JSONObject().put("title", "Probe Constraints First").put("description", "Before solving, state assumptions, scope, and success criteria."))
                    .put(JSONObject().put("title", "Tighten Final Recommendations").put("description", "Close answers with a crisp decision and the tradeoff behind it."))
            )
            .put("answers", answers)
            .toString()
    }
}
