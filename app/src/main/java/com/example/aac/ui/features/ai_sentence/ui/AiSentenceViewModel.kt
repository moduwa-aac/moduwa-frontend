package com.example.aac.ui.features.ai_sentence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.AiContext
import com.example.aac.data.remote.dto.AiPredictionRequest
import com.example.aac.data.remote.dto.AiPredictionResponse
import com.example.aac.data.remote.dto.MainWordItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.aac.data.repository.SentenceDataRepository

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

    // 1. 초기 데이터 세팅 (메인화면에서 넘어온 단어들)
    fun setInitialWords(words: List<MainWordItem>) {
        _uiState.value = _uiState.value.copy(selectedWords = words)
        fetchAiSentences(words.map { it.word }, isRefresh = false)
    }

    // 2. 단어 삭제 (상단 리스트)
    fun removeWord(index: Int) {
        val currentList = _uiState.value.selectedWords.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedWords = currentList)
            SentenceDataRepository.selectedWords = currentList
            fetchAiSentences(currentList.map { it.word }, isRefresh = false)
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

    // 3. API 호출 (문장 생성)
    fun fetchAiSentences(words: List<String>, isRefresh: Boolean) {
        if (words.isEmpty()) {
            _uiState.value = _uiState.value.copy(sentences = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val dateFormat = SimpleDateFormat("yyyy년 M월 d일 (E) HH:mm", Locale.KOREA)
                val currentTimeStr = dateFormat.format(Date())

                val request = AiPredictionRequest(
                    words = words,
                    context = AiContext(currentTime = currentTimeStr),
                    refresh = isRefresh
                )

                // API 호출
                val response = RetrofitInstance.api.getAiPredictions(request)

                if (response.success && response.data != null) {
                    val newSentences = response.data.predictions.mapIndexed { index, text ->
                        SentenceItem(id = index, text = text)
                    }
                    _uiState.value = _uiState.value.copy(sentences = newSentences)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onEvent(event: AiSentenceUiEvent) {
    }
}

sealed class AiSentenceUiEvent {
    object ClickPlayTop : AiSentenceUiEvent()
    data class ClickFavorite(val id: Int) : AiSentenceUiEvent()
    data class ClickPlaySentence(val id: Int) : AiSentenceUiEvent()
}