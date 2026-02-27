package com.example.aac.ui.features.auto_sentence

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class AutoSentenceRoutineUiState(
    val isLoading: Boolean = false,
    val routines: List<RoutineDto> = emptyList(),
    val errorMessage: String? = null
)

class AutoSentenceRoutineViewModel : ViewModel() {

    // ----------------------------------------------------
    // UI State
    // ----------------------------------------------------
    private val _uiState = MutableStateFlow(AutoSentenceRoutineUiState())
    val uiState: StateFlow<AutoSentenceRoutineUiState> = _uiState

    // ----------------------------------------------------
    // Modal State
    // ----------------------------------------------------
    private val _modalRoutine = MutableStateFlow<RoutineDto?>(null)
    val modalRoutine: StateFlow<RoutineDto?> = _modalRoutine

    private var currentModalId: String? = null

    // ----------------------------------------------------
    // TTS Player (MP3) & Global Setting
    // ----------------------------------------------------
    private var mediaPlayer: MediaPlayer? = null
    private var currentMp3File: File? = null

    @Volatile
    private var isTtsLoading: Boolean = false

    // 🟢 서버에서 받아올 최신 보이스키를 저장할 변수 (초기값은 여성으로 두되 서버값으로 덮어씌움)
    private var currentGlobalVoiceKey: String = "ADULT_FEMALE_DEFAULT"

    init {
        // 🟢 뷰모델이 생성될 때 서버에서 최신 TTS 설정을 가져옵니다.
        fetchTtsSetting()
    }

    private fun fetchTtsSetting() {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.getTtsSetting()
                if (res.success && res.data != null) {
                    currentGlobalVoiceKey = res.data.voiceKey
                    Log.d("ROUTINE_TTS", "✅ 최신 보이스키 로드 완료: $currentGlobalVoiceKey")
                }
            } catch (e: Exception) {
                Log.e("ROUTINE_TTS", "❌ 보이스키 가져오기 실패", e)
            }
        }
    }

    // ----------------------------------------------------
    // CRUD
    // ----------------------------------------------------
    fun createRoutine(request: CreateRoutineRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val res = RetrofitInstance.api.createRoutine(request)
                if (res.success) {
                    fetchRoutines()
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message ?: "생성 실패")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "네트워크 오류")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun fetchRoutines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val res = RetrofitInstance.api.getRoutines()
                if (res.success) {
                    _uiState.value = _uiState.value.copy(routines = res.data?.routines.orEmpty())
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message ?: "루틴 조회 실패")
                }
            } catch (e: Exception) {
                Log.e("ROUTINE", "루틴 조회 예외", e)
                _uiState.value = _uiState.value.copy(errorMessage = "네트워크 오류")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateRoutine(
        id: String,
        request: RoutineUpdateRequest,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val res = RetrofitInstance.api.updateRoutine(id, request)
                if (res.success) {
                    val updated = res.data?.routine
                    _uiState.value = _uiState.value.copy(
                        routines = _uiState.value.routines.map { if (it.id == updated?.id) updated else it }
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message ?: "수정 실패")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "네트워크 오류")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteRoutine(id: String, onSuccess: () -> Unit = {}) {
        deleteRoutines(listOf(id), onSuccess)
    }

    fun deleteRoutines(ids: List<String>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val res = RetrofitInstance.api.deleteRoutines(DeleteRoutinesRequest(ids))
                if (res.success) {
                    fetchRoutines()
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message ?: "삭제 실패")
                }
            } catch (e: Exception) {
                Log.e("ROUTINE", "삭제 예외", e)
                _uiState.value = _uiState.value.copy(errorMessage = "네트워크 오류")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteAllRoutines(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val res = RetrofitInstance.api.deleteAllRoutines()
                if (res.success) {
                    fetchRoutines()
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message ?: "삭제 실패")
                }
            } catch (e: Exception) {
                Log.e("ROUTINE", "전체 삭제 예외", e)
                _uiState.value = _uiState.value.copy(errorMessage = "네트워크 오류")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // ----------------------------------------------------
    // Modal (Polling 기반)
    // ----------------------------------------------------
    fun checkRoutineModal() {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.getRoutineModal()
                if (res.success) {
                    val routine = res.data?.routine

                    if (routine == null) {
                        Log.d("MODAL", "getRoutineModal: routine = null")
                        return@launch
                    }

                    if (routine.id != currentModalId) {
                        currentModalId = routine.id
                        _modalRoutine.value = routine
                        Log.d("MODAL", "모달 표시: id=${routine.id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MODAL", "checkRoutineModal 실패", e)
            }
        }
    }

    fun snoozeRoutine(id: String) {
        clearModal()
        stopTtsInternal()

        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.snoozeRoutineModal(id, SnoozeRequest(minutes = 5))
                if (res.success) {
                    Log.d("MODAL", "snooze 성공")
                }
            } catch (e: Exception) {
                Log.e("MODAL", "snooze 네트워크 실패", e)
            }
        }
    }

    fun dismissRoutine(id: String) {
        clearModal()
        stopTtsInternal()

        viewModelScope.launch {
            try {
                RetrofitInstance.api.dismissRoutineModal(id)
            } catch (e: Exception) {
                Log.e("MODAL", "dismiss 네트워크 실패", e)
            }
        }
    }

    fun clearModal() {
        _modalRoutine.value = null
        currentModalId = null
    }

    // ----------------------------------------------------
    // TTS (서버 MP3)
    // ----------------------------------------------------
    fun playRoutineTts(
        context: Context,
        text: String,
        voiceKey: String? = null
    ) {
        if (text.isBlank()) return

        if (isTtsLoading) return
        isTtsLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val safeVoiceKey = when (voiceKey) {
                    "default_male", "MALE" -> "ADULT_MALE_DEFAULT"
                    "default_female", "FEMALE" -> "ADULT_FEMALE_DEFAULT"
                    // 🟢 UI에서 null을 주면 아까 서버에서 긁어온 최신 설정값을 씁니다.
                    null, "" -> currentGlobalVoiceKey
                    else -> voiceKey
                }

                val res = RetrofitInstance.api.requestTtsMp3(
                    TtsRequest(text = text, voiceKey = safeVoiceKey)
                )

                if (!res.isSuccessful) return@launch

                val body = res.body() ?: return@launch
                val appCtx = context.applicationContext
                val outFile = File(appCtx.cacheDir, "tts_${UUID.randomUUID()}.mp3")

                body.use { responseBody ->
                    responseBody.byteStream().use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                currentMp3File?.let { runCatching { it.delete() } }
                currentMp3File = outFile

                withContext(Dispatchers.Main) {
                    stopTtsInternal()

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(outFile.absolutePath)
                        setOnPreparedListener { start() }
                        setOnCompletionListener { stopTtsInternal() }
                        setOnErrorListener { _, _, _ ->
                            stopTtsInternal()
                            true
                        }
                        prepareAsync()
                    }
                }
            } catch (e: Exception) {
                Log.e("TTS", "playRoutineTts 실패", e)
            } finally {
                isTtsLoading = false
            }
        }
    }

    private fun stopTtsInternal() {
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTtsInternal()
        currentMp3File?.let { runCatching { it.delete() } }
        currentMp3File = null
    }
}