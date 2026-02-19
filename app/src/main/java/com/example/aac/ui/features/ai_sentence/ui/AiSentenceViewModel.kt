package com.example.aac.ui.features.ai_sentence.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.AiFavoriteRequest
import com.example.aac.data.remote.dto.AiPredictionRequest
import com.example.aac.data.remote.dto.AiStyleRequest
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.SentenceDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    fun setInitialWords(words: List<MainWordItem>, tone: String) {
        _uiState.value = _uiState.value.copy(selectedWords = words)
        // 넘겨받은 tone으로 첫 API 호출
        fetchAiSentences(words, isRefresh = false, tone = tone)
    }

    // 2. 단어 삭제
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

    // 3. API 호출
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
                    val request = AiStyleRequest(
                        words = contentWords,
                        endingCards = endingCards,
                        tone = tone,
                        refresh = isRefresh
                    )
                    val response = api.getAiStyles(request)
                    resultSentences = if (response.success && response.data != null) {
                        response.data.sentences
                    } else emptyList()
                } else {
                    val request = AiPredictionRequest(
                        words = contentWords,
                        tone = tone,
                        refresh = isRefresh
                    )
                    val response = api.getAiPredictions(request)
                    resultSentences = if (response.success && response.data != null) {
                        response.data.sentences
                    } else emptyList()
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

    // ✅ 즐겨찾기 API 연결
    fun toggleFavorite(sentenceId: Int, text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = AiFavoriteRequest(sentence = text)
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

    // ✅ 재생 기능 (메인 화면에서 쓰던 TTS 코드를 여기에 복붙해서 연결하시면 됩니다)
    fun playSentence(context: Context, text: String) {
        if (text.isBlank()) return
        // TODO: MainViewModel에서 쓰던 repository.fetchTtsAudio() 등을 사용해서 재생!
    }

    // ✅ 이벤트 핸들러
    fun onEvent(event: AiSentenceUiEvent, context: Context, showSnackbar: (String) -> Unit) {
        when (event) {
            is AiSentenceUiEvent.ClickPlayTop -> {
                val combinedText = _uiState.value.selectedWords.joinToString(" ") { it.word }
                playSentence(context, combinedText)
            }
            is AiSentenceUiEvent.ClickPlaySentence -> {
                playSentence(context, event.text)
            }
            is AiSentenceUiEvent.ClickFavorite -> {
                toggleFavorite(event.id, event.text) { message ->
                    showSnackbar(message)
                }
            }
        }
    }
}

sealed class AiSentenceUiEvent {
    object ClickPlayTop : AiSentenceUiEvent()
    data class ClickFavorite(val id: Int, val text: String) : AiSentenceUiEvent()
    data class ClickPlaySentence(val id: Int, val text: String) : AiSentenceUiEvent()
}