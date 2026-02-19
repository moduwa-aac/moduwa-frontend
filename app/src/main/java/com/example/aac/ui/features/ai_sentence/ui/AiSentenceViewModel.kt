package com.example.aac.ui.features.ai_sentence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
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
    fun setInitialWords(words: List<MainWordItem>) {
        _uiState.value = _uiState.value.copy(selectedWords = words)
        // MainWordItem 리스트 전체를 넘겨서 처리
        fetchAiSentences(words, isRefresh = false)
    }

    // 2. 단어 삭제
    fun removeWord(index: Int) {
        val currentList = _uiState.value.selectedWords.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedWords = currentList)
            SentenceDataRepository.selectedWords = currentList

            // 변경된 리스트로 재요청
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

    // 3. API 호출 (어미/일반 분기 처리 적용)
    // 인자를 List<MainWordItem>으로 받도록 변경 (품사 확인을 위해)
    fun fetchAiSentences(wordItems: List<MainWordItem>, isRefresh: Boolean, tone: String = "HONORIFIC") {
        if (wordItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(sentences = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 🅰️ 낱말 vs 어미 분리
                val endingCards = wordItems.filter {
                    it.partOfSpeech == "E" || it.partOfSpeech == "ENDING" || it.categoryName == "어미"
                }.map { it.word }

                val contentWords = wordItems.filter {
                    it.partOfSpeech != "E" && it.partOfSpeech != "ENDING" && it.categoryName != "어미"
                }.map { it.word }

                val api = RetrofitInstance.api
                val resultSentences: List<String>

                if (endingCards.isNotEmpty()) {
                    // ✅ 어미가 있음 -> Styles API 호출
                    val request = AiStyleRequest(
                        words = contentWords,
                        endingCards = endingCards,
                        tone = tone,
                        refresh = isRefresh
                    )
                    val response = api.getAiStyles(request)
                    // Styles API는 "sentences" 필드로 줌
                    resultSentences = if (response.success && response.data != null) {
                        response.data.sentences
                    } else emptyList()

                } else {
                    // ✅ 어미가 없음 -> Predictions API 호출
                    val request = AiPredictionRequest(
                        words = contentWords,
                        tone = tone,
                        refresh = isRefresh
                    )
                    val response = api.getAiPredictions(request)
                    // Predictions API는 "predictions" 필드로 주지만 DTO에서 sentences로 매핑해둠
                    resultSentences = if (response.success && response.data != null) {
                        response.data.sentences
                    } else emptyList()
                }

                // UI 업데이트
                val sentenceItems = resultSentences.mapIndexed { index, text ->
                    SentenceItem(id = index, text = text)
                }
                _uiState.value = _uiState.value.copy(sentences = sentenceItems)

            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 시 빈 리스트
                _uiState.value = _uiState.value.copy(sentences = emptyList())
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onEvent(event: AiSentenceUiEvent) {
        // 필요 시 구현
    }
}

sealed class AiSentenceUiEvent {
    object ClickPlayTop : AiSentenceUiEvent()
    data class ClickFavorite(val id: Int) : AiSentenceUiEvent()
    data class ClickPlaySentence(val id: Int) : AiSentenceUiEvent()
}