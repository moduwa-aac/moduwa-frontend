package com.example.aac.ui.features.main

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.R
import com.example.aac.data.mapper.IconMapper
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.MainRepository
import com.example.aac.data.repository.SentenceDataRepository
import com.example.aac.ui.components.CategoryItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

sealed class MainUiEvent {
    data class ShowSnackbar(val message: String) : MainUiEvent()
}

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    private val CATEGORY_ITEMS_PER_PAGE = 8

    // UI 상태
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _categoryPageIndex = MutableStateFlow(0)
    val categoryPageIndex: StateFlow<Int> = _categoryPageIndex.asStateFlow()

    private val _categoryTotalPageCount = MutableStateFlow(0)
    val categoryTotalPageCount: StateFlow<Int> = _categoryTotalPageCount.asStateFlow()

    private val _words = MutableStateFlow<List<MainWordItem>>(emptyList())
    val words: StateFlow<List<MainWordItem>> = _words.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    private val _selectedCards = MutableStateFlow<List<MainWordItem>>(emptyList())
    val selectedCards: StateFlow<List<MainWordItem>> = _selectedCards.asStateFlow()

    private val _endingWords = MutableStateFlow<List<MainWordItem>>(emptyList())
    val endingWords: StateFlow<List<MainWordItem>> = _endingWords.asStateFlow()

    private val _predictedSentences = MutableStateFlow<List<String>>(emptyList())
    val predictedSentences: StateFlow<List<String>> = _predictedSentences.asStateFlow()

    private val _isInformal = MutableStateFlow(false)
    val isInformal: StateFlow<Boolean> = _isInformal.asStateFlow()

    private val _wordPageIndex = MutableStateFlow(0)
    val wordPageIndex: StateFlow<Int> = _wordPageIndex.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 내부 변수
    private var currentVoiceKey: String = "ADULT_FEMALE_DEFAULT"
    private var endingCategoryId: String? = null
    private var mediaPlayer: MediaPlayer? = null

    var showAddWordDialog by mutableStateOf(false)
        private set

    fun syncWithRepository() {
        // 1. 카테고리 및 어미 리스트 새로고침 (수정/추가 반영)
        refreshAllData()

        // 2. 선택된 카드 동기화 (기존 로직)
        if (_selectedCards.value != SentenceDataRepository.selectedWords) {
            _selectedCards.value = SentenceDataRepository.selectedWords
            requestAiPrediction()
        }
    }

    private fun refreshAllData() {
        viewModelScope.launch {
            try {
                val fetchedCategories = repository.getCategories()

                // 어미 카테고리 분리
                val endingCategory = fetchedCategories.find { it.name.trim() == "어미" }
                endingCategoryId = endingCategory?.id
                if (endingCategory != null) {
                    val endings = repository.fetchWords(endingCategory.id)
                    _endingWords.value = endings.map { it.copy(partOfSpeech = "E") }
                }

                // 고정 및 서버 카테고리 매핑
                val serverCategories = fetchedCategories
                    .filter {
                        val n = it.name.trim()
                        n != "어미" && n != "전체" && n != "최근사용" && n != "즐겨찾기"
                    }
                    .map { item ->
                        CategoryItem(
                            name = item.name,
                            iconRes = IconMapper.toLocalResource(item.iconKey),
                            isSelected = false,
                            serverId = item.id,
                            iconUrl = item.iconUrl,
                            displayOrder = item.displayOrder ?: 0
                        )
                    }
                    .sortedBy { it.displayOrder }

                val fixedCategories = listOf(
                    CategoryItem(name = "최근사용", iconRes = R.drawable.ic_recent_use, isSelected = false, serverId = null, displayOrder = -2),
                    CategoryItem(name = "즐겨찾기", iconRes = R.drawable.ic_favorite, isSelected = false, serverId = null, displayOrder = -1)
                )

                val allCategories = fixedCategories + serverCategories

                // 현재 선택된 인덱스 유지하면서 리스트만 교체
                val currentIndex = _selectedCategoryIndex.value
                val updatedList = allCategories.mapIndexed { i, item ->
                    item.copy(isSelected = i == currentIndex)
                }

                _categories.value = updatedList
                calculateCategoryPages(updatedList.size)

                // 현재 카테고리의 낱말들도 다시 로드
                selectCategory(currentIndex)

            } catch (e: Exception) {
                Log.e("MainViewModel", "데이터 갱신 실패: ${e.message}")
            }
        }
    }

    init {
        refreshAllData()
    }
    // 초기 데이터 로드 (고정 카테고리 + 서버 카테고리 + 어미 분리)
    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(500)
                val fetchedCategories = repository.getCategories()

                // 1. 어미 카테고리 분리
                val endingCategory = fetchedCategories.find { it.name.trim() == "어미" }
                endingCategoryId = endingCategory?.id
                if (endingCategory != null) {
                    val endings = repository.fetchWords(endingCategory.id)
                    _endingWords.value = endings.map { it.copy(partOfSpeech = "E") }
                }

                // 2. 서버 카테고리 정렬 (고정 카테고리들 제외하고 정렬)
                val serverCategories = fetchedCategories
                    .filter {
                        val n = it.name.trim()
                        n != "어미" && n != "전체" && n != "최근사용" && n != "즐겨찾기"
                    }
                    .map { item ->
                        CategoryItem(
                            name = item.name,
                            iconRes = IconMapper.toLocalResource(item.iconKey),
                            isSelected = false,
                            serverId = item.id,
                            iconUrl = item.iconUrl,
                            displayOrder = item.displayOrder ?: 0
                        )
                    }
                    .sortedBy { it.displayOrder }

                // 3. 최근사용, 즐겨찾기 고정 (전체 삭제)
                val fixedCategories = listOf(
                    CategoryItem(name = "최근사용", iconRes = R.drawable.ic_recent_use, isSelected = true, serverId = null, displayOrder = -2),
                    CategoryItem(name = "즐겨찾기", iconRes = R.drawable.ic_favorite, isSelected = false, serverId = null, displayOrder = -1)
                )

                val allCategories = fixedCategories + serverCategories
                _categories.value = allCategories
                calculateCategoryPages(allCategories.size)

                if (allCategories.isNotEmpty()) {
                    selectCategory(0)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "초기 데이터 로드 실패: ${e.message}")
            }
        }
    }

    private fun calculateCategoryPages(totalItemCount: Int) {
        _categoryTotalPageCount.value = if (totalItemCount == 0) 1 else ceil(totalItemCount.toDouble() / CATEGORY_ITEMS_PER_PAGE).toInt()
    }

    fun nextCategoryPage() {
        if (_categoryPageIndex.value < _categoryTotalPageCount.value - 1) {
            _categoryPageIndex.value += 1
        }
    }

    fun prevCategoryPage() {
        if (_categoryPageIndex.value > 0) {
            _categoryPageIndex.value -= 1
        }
    }

    // 카테고리 선택 및 낱말 호출 (AI 문장 병합 포함)
    fun selectCategory(index: Int) {
        val currentList = _categories.value
        if (index !in currentList.indices) return

        _selectedCategoryIndex.value = index

        val updatedList = currentList.mapIndexed { i, item -> item.copy(isSelected = i == index) }
        _categories.value = updatedList

        val selectedItem = updatedList[index]

        viewModelScope.launch {
            val catName = selectedItem.name.replace(" ", "")

            if (catName == "전체") {
                val words = repository.getWords()
                _words.value = words.distinctBy { it.cardId }
            } else if (catName == "즐겨찾기") {
                val favWords = repository.getWords(categoryId = null, onlyFavorite = true)
                val filteredFavWords = favWords.filter { it.isFavorite }.distinctBy { it.word }
                val aiSentences = repository.getAiSentenceFavorites()

                _words.value = filteredFavWords + aiSentences
            } else {
                selectedItem.serverId?.let { serverId ->
                    val words = repository.getWords(categoryId = serverId)
                    _words.value = words.distinctBy { it.word }
                } ?: run {
                    _words.value = emptyList()
                }
            }
            try {
                val ttsRes = RetrofitInstance.api.getTtsSetting() // Repository에 이 함수가 있어야 합니다. (없다면 RetrofitInstance.api.getTtsSetting() 직접 호출)
                if (ttsRes.success && ttsRes.data != null) {
                    currentVoiceKey = ttsRes.data.voiceKey
                    Log.d("MainViewModel", "✅ TTS 설정 로드: $currentVoiceKey")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "TTS 설정 가져오기 실패", e)
            }
            _wordPageIndex.value = 0
        }
    }

    // AI 문장 예측 요청
    private fun requestAiPrediction() {
        val currentList = _selectedCards.value
        if (currentList.isEmpty()) {
            _predictedSentences.value = emptyList()
            return
        }

        val endingCards = currentList.filter {
            it.partOfSpeech == "E" || it.partOfSpeech == "ENDING" || it.categoryId == endingCategoryId
        }.map { it.word }

        val contentWords = currentList.filter {
            it.partOfSpeech != "E" && it.partOfSpeech != "ENDING" && it.categoryId != endingCategoryId
        }.map { it.word }

        val tone = if (_isInformal.value) "INFORMAL" else "HONORIFIC"

        viewModelScope.launch {
            val sentences = if (endingCards.isNotEmpty()) {
                repository.getAiStyles(contentWords, endingCards, tone)
            } else {
                repository.getAiPredictions(contentWords, tone)
            }
            _predictedSentences.value = sentences
        }
    }

    fun toggleTone() {
        _isInformal.value = !_isInformal.value
    }

    fun addCard(card: MainWordItem) {
        if (_selectedCards.value.size >= 20) {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("최대 20개까지만 선택 가능합니다.")) }
            return
        }
        val newList = _selectedCards.value + card
        _selectedCards.value = newList
        SentenceDataRepository.selectedWords = newList
    }

    fun removeCard(index: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedCards.value = currentList
            SentenceDataRepository.selectedWords = currentList
        }
    }

    fun clearSelectedCards() {
        _selectedCards.value = emptyList()
        SentenceDataRepository.selectedWords = emptyList()
    }

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            java.util.Collections.swap(currentList, fromIndex, toIndex)
            _selectedCards.value = currentList
            SentenceDataRepository.selectedWords = currentList
        }
    }

    fun openAddWordDialog() { showAddWordDialog = true }
    fun closeAddWordDialog() { showAddWordDialog = false }

    fun createNewWord(word: String, imageUrl: String? = null) {
        val currentCatIndex = _selectedCategoryIndex.value
        val currentCategory = _categories.value.getOrNull(currentCatIndex)
        var targetCategoryId = currentCategory?.serverId

        val isFavoritesTab = currentCategory?.name?.replace(" ", "") == "즐겨찾기"
        val isAllTab = currentCategory?.name?.replace(" ", "") == "전체"

        if (isFavoritesTab || isAllTab) {
            val firstValidCat = _categories.value.firstOrNull { it.serverId != null }
            if (firstValidCat != null) {
                targetCategoryId = firstValidCat.serverId
            } else {
                viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("저장할 카테고리가 없습니다.")) }
                return
            }
        } else if (targetCategoryId == null || currentCategory?.name == "최근 사용") {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("이 카테고리에는 추가할 수 없습니다.")) }
            return
        }

        viewModelScope.launch {
            val newId = repository.createWord(targetCategoryId ?: "", word, imageUrl)

            if (newId != null) {
                if (isFavoritesTab) repository.toggleFavorite(newId, true)
                closeAddWordDialog()
                _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말이 추가되었습니다."))
                selectCategory(_selectedCategoryIndex.value)
                _wordPageIndex.value = Int.MAX_VALUE
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("추가 실패: 서버 오류"))
            }
        }
    }

    fun updateWord(originalCard: MainWordItem, newWord: String, newCategoryName: String, newImageUrl: String?) {
        viewModelScope.launch {
            val targetCategory = _categories.value.find { it.name == newCategoryName }
            val categoryId = targetCategory?.serverId

            val updatedWordItem = repository.updateWord(
                cardId = originalCard.cardId,
                categoryId = categoryId,
                word = newWord,
                imageUrl = newImageUrl
            )

            if (updatedWordItem != null) {
                selectCategory(_selectedCategoryIndex.value)
                _eventFlow.emit(MainUiEvent.ShowSnackbar("수정되었습니다."))
                updateCardInList(originalCard.cardId, updatedWordItem.cardId, updatedWordItem.isFavorite)
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("수정에 실패했습니다."))
            }
        }
    }

    fun toggleFavorite(card: MainWordItem) {
        viewModelScope.launch {
            if (card.partOfSpeech == "AI_SENTENCE") {
                val success = repository.deleteAiSentenceFavorite(card.cardId)
                if (success) {
                    selectCategory(_selectedCategoryIndex.value)
                    _eventFlow.emit(MainUiEvent.ShowSnackbar("즐겨찾기에서 제거되었습니다."))
                } else {
                    _eventFlow.emit(MainUiEvent.ShowSnackbar("AI 문장 즐겨찾기 해제 실패"))
                }
                return@launch
            }

            val newFavStatus = !card.isFavorite
            val result = repository.toggleFavorite(card.cardId, newFavStatus)

            if (result != null) {
                val updatedList = _words.value.map {
                    if (it.cardId == card.cardId) it.copy(isFavorite = result.isFavorite) else it
                }
                _words.value = updatedList

                val currentCategoryName = _categories.value.getOrNull(_selectedCategoryIndex.value)?.name?.replace(" ", "") ?: ""
                if (currentCategoryName == "즐겨찾기") {
                    selectCategory(_selectedCategoryIndex.value)
                }

                val msg = if (result.isFavorite) "즐겨찾기에 추가되었습니다." else "즐겨찾기에서 해제되었습니다."
                _eventFlow.emit(MainUiEvent.ShowSnackbar(msg))
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("즐겨찾기 변경 실패"))
            }
        }
    }

    fun deleteWord(wordItem: MainWordItem) {
        viewModelScope.launch {
            if (wordItem.partOfSpeech == "AI_SENTENCE") {
                val success = repository.deleteAiSentenceFavorite(wordItem.cardId)
                if (success) {
                    selectCategory(_selectedCategoryIndex.value)
                    _eventFlow.emit(MainUiEvent.ShowSnackbar("AI 문장 즐겨찾기가 삭제되었습니다."))
                }
                return@launch
            }

            val success = repository.deleteWord(wordItem.cardId)
            if (success) {
                selectCategory(_selectedCategoryIndex.value)
                _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말이 삭제되었습니다."))
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말 삭제 실패"))
            }
        }
    }

    private fun updateCardInList(oldId: String, newId: String, isFavorite: Boolean) {
        _words.value = _words.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
        _endingWords.value = _endingWords.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
        _selectedCards.value = _selectedCards.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
    }

    fun setWordPageIndex(index: Int) {
        _wordPageIndex.value = index
    }

    fun playSentence(context: Context) {
        val sentence = _selectedCards.value.joinToString(" ") { it.word }
        if (sentence.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("재생할 낱말이 없습니다.")) }
            return
        }
        playTts(context, sentence)
    }

    fun playSingleWord(context: Context, word: String) {
        if (word.isBlank()) return
        playTts(context, word)
    }

    private fun playTts(context: Context, text: String) {
        viewModelScope.launch {
            val audioBytes = repository.fetchTtsAudio(text, currentVoiceKey)
            if (audioBytes != null) {
                playAudioFromBytes(context, audioBytes)
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("TTS 재생 실패"))
            }
        }
    }

    private fun playAudioFromBytes(context: Context, audioData: ByteArray) {
        mediaPlayer?.release()
        mediaPlayer = null
        try {
            val tempFile = File.createTempFile("tts_audio", ".mp3", context.cacheDir).apply { deleteOnExit() }
            FileOutputStream(tempFile).use { it.write(audioData) }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    tempFile.delete()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("오디오 재생 오류")) }
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}