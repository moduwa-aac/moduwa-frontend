package com.example.aac.ui.features.ai_sentence.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.AiFavoriteRequest
import com.example.aac.data.remote.dto.AiPredictionRequest
import com.example.aac.data.remote.dto.AiStyleRequest
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.remote.dto.TtsRequest // TTS 요청 DTO 임포트 확인
import com.example.aac.data.repository.SentenceDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// UI 상태 관리용 클래스
data class AiSentenceUiState(
    val sentences: List<SentenceItem> = emptyList(),
    val selectedWords: List<MainWordItem> = emptyList(),
    val isLoading: Boolean = false
)

data class SentenceItem(
    val id: Int,
    val text: String,
    val isFavorite: Boolean = false
)

class AiSentenceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiSentenceUiState())
    val uiState: StateFlow<AiSentenceUiState> = _uiState.asStateFlow()

    // 1. 초기 데이터 세팅
    fun setInitialWords(words: List<MainWordItem>, tone: String = "HONORIFIC") {
        _uiState.value = _uiState.value.copy(selectedWords = words)
        fetchAiSentences(words, isRefresh = false, tone = tone)
    }

    // 2. 단어 삭제 및 이동 (생략 없이 그대로)
    fun removeWord(index: Int) {
        val currentList = _uiState.value.selectedWords.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedWords = currentList)
            SentenceDataRepository.selectedWords = currentList
            fetchAiSentences(currentList, isRefresh = false)
        }
    }

    fun moveWord(fromIndex: Int, toIndex: Int) {
        val currentList = _uiState.value.selectedWords.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            java.util.Collections.swap(currentList, fromIndex, toIndex)
            _uiState.value = _uiState.value.copy(selectedWords = currentList)
            SentenceDataRepository.selectedWords = currentList
        }
    }

    // 3. 문장 추천 API 호출
    fun fetchAiSentences(wordItems: List<MainWordItem>, isRefresh: Boolean, tone: String = "HONORIFIC") {
        if (wordItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(sentences = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val endingCards = wordItems.filter {
                    it.partOfSpeech == "E" || it.partOfSpeech == "ENDING" || it.categoryName == "어미"
                }.map { it.word }

                val contentWords = wordItems.filter {
                    it.partOfSpeech != "E" && it.partOfSpeech != "ENDING" && it.categoryName != "어미"
                }.map { it.word }

                val api = RetrofitInstance.api
                val resultSentences: List<String>

                if (endingCards.isNotEmpty()) {
                    val request = AiStyleRequest(contentWords, endingCards, tone, isRefresh)
                    val response = api.getAiStyles(request)
                    resultSentences = if (response.success && response.data != null) response.data.sentences else emptyList()
                } else {
                    val request = AiPredictionRequest(contentWords, tone, isRefresh)
                    val response = api.getAiPredictions(request)
                    resultSentences = if (response.success && response.data != null) response.data.sentences else emptyList()
                }

                val sentenceItems = resultSentences.mapIndexed { index, text ->
                    SentenceItem(id = index, text = text)
                }
                _uiState.value = _uiState.value.copy(sentences = sentenceItems)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(sentences = emptyList())
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // ✅ 4. 전용 API를 사용한 문장 즐겨찾기
    fun toggleFavorite(sentenceId: Int, text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // 명세서에 맞춘 Request Body
                val request = AiFavoriteRequest(sentence = text, sentenceSource = "AI_SUGGESTED")
                val response = RetrofitInstance.api.addSentenceFavorite(request)

                if (response.success) {
                    val updatedSentences = _uiState.value.sentences.map {
                        if (it.id == sentenceId) it.copy(isFavorite = true) else it
                    }
                    _uiState.value = _uiState.value.copy(sentences = updatedSentences)
                    onResult("즐겨찾기에 추가되었습니다.")
                } else {
                    onResult("즐겨찾기 실패: ${response.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("네트워크 오류가 발생했습니다.")
            }
        }
    }

    // ✅ 5. TTS 재생 로직 (메인 화면 로직 완벽 이식)
    // ✅ MainRepository를 뷰모델 안에 하나 만들어줍니다.
    private val mainRepository = com.example.aac.data.repository.MainRepository()

    // ✅ 5. TTS 재생 로직 (메인 화면의 Repository 완벽 재사용!)
    private fun playTts(context: Context, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                // 1. 메인 레포지토리에서 확실하게 voiceKey를 가져옴
                val voiceKey = mainRepository.getTtsVoiceKey()

                // 2. 메인 레포지토리의 오디오 요청 함수를 그대로 사용
                val audioBytes = mainRepository.fetchTtsAudio(text, voiceKey)

                if (audioBytes != null) {
                    playAudioFromBytes(context, audioBytes)
                } else {
                    android.util.Log.e("AiSentence", "TTS 오디오 데이터를 받아오지 못했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ 6. 바이트 배열을 임시 파일로 만들어 재생 (메인 화면 로직)
    private fun playAudioFromBytes(context: Context, audioBytes: ByteArray) {
        try {
            val tempFile = File.createTempFile("temp_audio", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ✅ 7. UI 이벤트 핸들러
    fun onEvent(event: AiSentenceUiEvent, context: Context, showSnackbar: (String) -> Unit) {
        when (event) {
            is AiSentenceUiEvent.ClickPlayTop -> {
                val combinedText = _uiState.value.selectedWords.joinToString(" ") { it.word }
                playTts(context, combinedText)
            }
            is AiSentenceUiEvent.ClickPlaySentence -> {
                playTts(context, event.text)
            }
            is AiSentenceUiEvent.ClickFavorite -> {
                toggleFavorite(event.id, event.text) { message ->
                    showSnackbar(message)
                }
            }
        }
    }
    sealed class AiSentenceUiEvent {
        object ClickPlayTop : AiSentenceUiEvent()
        data class ClickFavorite(val id: Int, val text: String) : AiSentenceUiEvent()
        data class ClickPlaySentence(val id: Int, val text: String) : AiSentenceUiEvent()
    }
}