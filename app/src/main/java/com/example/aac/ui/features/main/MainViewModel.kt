package com.example.aac.ui.features.main

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.R
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

    // UI 상태: 전체 카테고리 리스트
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _categoryPageIndex = MutableStateFlow(0)
    val categoryPageIndex: StateFlow<Int> = _categoryPageIndex.asStateFlow()

    // 카테고리 총 페이지 수
    private val _categoryTotalPageCount = MutableStateFlow(0)
    val categoryTotalPageCount: StateFlow<Int> = _categoryTotalPageCount.asStateFlow()

    // UI 상태: 현재 보여줄 단어 리스트
    private val _words = MutableStateFlow<List<MainWordItem>>(emptyList())
    val words: StateFlow<List<MainWordItem>> = _words.asStateFlow()

    // UI 상태: 선택된 카테고리 인덱스
    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    // UI 상태: 상단에 선택된 카드들 (문장 만들기용)
    private val _selectedCards = MutableStateFlow<List<MainWordItem>>(emptyList())
    val selectedCards: StateFlow<List<MainWordItem>> = _selectedCards.asStateFlow()

    // UI 상태: 어미 단어 리스트 (우측 사이드바용)
    private val _endingWords = MutableStateFlow<List<MainWordItem>>(emptyList())
    val endingWords: StateFlow<List<MainWordItem>> = _endingWords.asStateFlow()

    // UI 상태: AI가 추천한 문장 리스트
    private val _predictedSentences = MutableStateFlow<List<String>>(emptyList())
    val predictedSentences: StateFlow<List<String>> = _predictedSentences.asStateFlow()

    // UI 상태: 반말/존댓말 모드 (기본: 존댓말 false)
    private val _isInformal = MutableStateFlow(false)
    val isInformal: StateFlow<Boolean> = _isInformal.asStateFlow()

    // UI 상태: 낱말 그리드 페이지 제어용
    private val _wordPageIndex = MutableStateFlow(0)
    val wordPageIndex: StateFlow<Int> = _wordPageIndex.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MainUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 내부 변수
    private var currentVoiceKey: String = "ADULT_FEMALE_DEFAULT"
    private var endingCategoryId: String? = null
    private var mediaPlayer: MediaPlayer? = null


    // 낱말 추가 다이얼로그 상태
    var showAddWordDialog by mutableStateOf(false)
        private set

    init {
        fetchInitialData()
    }

    // 1. 초기 데이터 로드 (카테고리 + 어미 분리)
    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                // 1. 토큰 저장 등 타이밍 이슈 방지용 대기
                kotlinx.coroutines.delay(500)

                // ✅ [핵심] 여기서 변수를 선언해야 아래에서 빨간 줄이 안 뜹니다!
                val fetchedCategories = repository.getCategories()

                // 로그로 확인
                Log.d("MainViewModel", "📜 가져온 카테고리: ${fetchedCategories.map { it.name }}")

                // -------------------------------------------------
                // 2. '어미' 카테고리 처리 (우측 사이드바용)
                // -------------------------------------------------
                val endingCategory = fetchedCategories.find { it.name.trim() == "어미" }
                endingCategoryId = endingCategory?.id

                if (endingCategory != null) {
                    val endings = repository.fetchWords(endingCategory.id)

                    // UI State에 값 넣기 (품사 'E'로 강제)
                    _endingWords.value = endings.map { it.copy(partOfSpeech = "E") }

                    Log.d("MainViewModel", "✅ 어미 데이터 로드 완료: ${endings.size}개")
                } else {
                    Log.e("MainViewModel", "⚠️ '어미' 카테고리 없음")
                }

                if (fetchedCategories.isEmpty()) return@launch

                // -------------------------------------------------
                // 3. 일반 카테고리 처리 (상단 탭용)
                // -------------------------------------------------
                val serverCategories = fetchedCategories
                    .filter { it.name.trim() != "어미" } // 어미는 탭에서 제외
                    .map { item ->
                        // 아이콘 매핑 (공백 제거 후 비교)
                        val icon = when (item.name.replace(" ", "")) {
                            "최근사용" -> R.drawable.ic_recent_use
                            "즐겨찾기" -> R.drawable.ic_favorite
                            "사람" -> R.drawable.ic_human
                            "행동" -> R.drawable.ic_act
                            "감정", "상태" -> R.drawable.ic_emotion
                            "음식" -> R.drawable.ic_food
                            "장소" -> R.drawable.ic_place
                            "신체" -> R.drawable.ic_human
                            else -> R.drawable.ic_default
                        }

                        CategoryItem(
                            name = item.name,
                            iconRes = icon,
                            isSelected = false,
                            serverId = item.id
                        )
                    }

                // UI 갱신
                _categories.value = serverCategories
                calculateCategoryPages(serverCategories.size)

                // 첫 번째 카테고리 자동 선택
                if (serverCategories.isNotEmpty()) {
                    selectCategory(0)
                }

            } catch (e: Exception) {
                e.printStackTrace()
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

    // 카테고리 선택
    fun selectCategory(index: Int) {
        val currentList = _categories.value
        if (index !in currentList.indices) return

        _selectedCategoryIndex.value = index

        // 탭 UI 갱신
        val updatedList = currentList.mapIndexed { i, item -> item.copy(isSelected = i == index) }
        _categories.value = updatedList

        val selectedItem = updatedList[index]

        viewModelScope.launch {
            // "즐겨찾기"라는 이름이거나 "즐겨찾기" 아이콘을 쓰는 경우
            if (selectedItem.name.replace(" ", "") == "즐겨찾기") {

                // 1. 서버에 즐겨찾기(onlyFavorite=true) 요청
                val favWords = repository.getWords(categoryId = null, onlyFavorite = true)

                // 🔥 [수정] 서버 믿지 말고 앱에서 한 번 더 필터링 (확실한 처리)
                _words.value = favWords.filter { it.isFavorite }.distinctBy { it.word }

            } else {
                // 일반 카테고리
                selectedItem.serverId?.let { serverId ->
                    val words = repository.getWords(categoryId = serverId)
                    _words.value = words.distinctBy { it.word }
                } ?: run {
                    _words.value = emptyList()
                }
            }
            // 페이지 초기화
            _wordPageIndex.value = 0
        }
    }

    // 2. AI 문장 예측 요청 (API-01 / API-05 분기 처리)
    private fun requestAiPrediction() {
        val currentList = _selectedCards.value
        if (currentList.isEmpty()) {
            _predictedSentences.value = emptyList()
            return
        }

        // 낱말(Content)과 어미(Ending) 분리
        val endingCards = currentList.filter {
            it.partOfSpeech == "E" || it.partOfSpeech == "ENDING" || it.categoryId == endingCategoryId
        }.map { it.word }

        val contentWords = currentList.filter {
            it.partOfSpeech != "E" && it.partOfSpeech != "ENDING" && it.categoryId != endingCategoryId
        }.map { it.word }

        // Tone 결정 (반말/존댓말)
        val tone = if (_isInformal.value) "INFORMAL" else "HONORIFIC"

        viewModelScope.launch {
            val sentences = if (endingCards.isNotEmpty()) {
                // 어미가 있으면 -> 스타일 변환 API (AI-05)
                repository.getAiStyles(contentWords, endingCards, tone)
            } else {
                // 어미가 없으면 -> 단순 예측 API (AI-01)
                repository.getAiPredictions(contentWords, tone)
            }
            _predictedSentences.value = sentences
        }
    }

    // 반말/존댓말 토글
    fun toggleTone() {
        _isInformal.value = !_isInformal.value
    }

    // 상단 카드 추가
    fun addCard(card: MainWordItem) {
        if (_selectedCards.value.size >= 20) {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말 카드는 최대 20개까지만 선택할 수 있어요.")) }
            return
        }
        val newList = _selectedCards.value + card
        _selectedCards.value = newList
        SentenceDataRepository.selectedWords = newList

        // 카드 변경 시 AI 요청
    }

    // 상단 카드 삭제
    fun removeCard(index: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedCards.value = currentList
            SentenceDataRepository.selectedWords = currentList
        }
        // 카드 변경 시 AI 요청
    }

    // 상단 카드 전체 삭제
    fun clearSelectedCards() {
        _selectedCards.value = emptyList()
        SentenceDataRepository.selectedWords = emptyList()
    }

    // 상단 카드 순서 이동
    fun moveCard(fromIndex: Int, toIndex: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            java.util.Collections.swap(currentList, fromIndex, toIndex)
            _selectedCards.value = currentList
            SentenceDataRepository.selectedWords = currentList
        }
    }

    fun syncWithRepository() {
        if (_selectedCards.value != SentenceDataRepository.selectedWords) {
            _selectedCards.value = SentenceDataRepository.selectedWords
            requestAiPrediction()
        }
    }

    fun openAddWordDialog() { showAddWordDialog = true }
    fun closeAddWordDialog() { showAddWordDialog = false }

    // 3. 새 낱말 생성 (자동 스크롤 기능 포함)
    fun createNewWord(word: String, imageUrl: String? = null) {
        val currentCatIndex = _selectedCategoryIndex.value
        val currentCategory = _categories.value.getOrNull(currentCatIndex)
        var targetCategoryId = currentCategory?.serverId

        // 즐겨찾기 탭 처리 로직 (기존 동일)
        val isFavoritesTab = currentCategory?.name?.replace(" ", "") == "즐겨찾기"

        if (isFavoritesTab) {
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

        // ❌ 품사(pos) 변수 선언 삭제 (필요 없음)

        viewModelScope.launch {
            // ✅ [수정] repository.createWord 인자가 3개로 줄어듦 (pos 삭제)
            val newId = repository.createWord(targetCategoryId!!, word, imageUrl)

            if (newId != null) {
                // 즐겨찾기 탭이면 자동 즐겨찾기
                if (isFavoritesTab) {
                    repository.toggleFavorite(newId, true)
                }

                closeAddWordDialog()
                _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말이 추가되었습니다."))

                // 목록 갱신
                if (isFavoritesTab) {
                    val favWords = repository.getWords(categoryId = null, onlyFavorite = true)
                    _words.value = favWords.filter { it.isFavorite }.distinctBy { it.word }
                } else {
                    val updatedList = repository.getWords(targetCategoryId).distinctBy { it.word }
                    _words.value = updatedList
                }

                _wordPageIndex.value = Int.MAX_VALUE
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("추가 실패: 서버 오류"))
            }
        }
    }

    // 낱말 수정
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
                // 현재 카테고리 새로고침
                selectCategory(_selectedCategoryIndex.value)
                _eventFlow.emit(MainUiEvent.ShowSnackbar("수정되었습니다."))

                // 만약 선택된 카드에 포함되어 있다면 거기도 업데이트
                updateCardInList(originalCard.cardId, updatedWordItem.cardId, updatedWordItem.isFavorite)
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("수정에 실패했습니다."))
            }
        }
    }

    // 낱말 삭제 (모달에서 호출)
    fun deleteWord(card: MainWordItem) {
        viewModelScope.launch {
            val isSuccess = repository.deleteWord(card.cardId)

            if (isSuccess) {
                // UI 갱신 (현재 카테고리 및 어미 목록)
                selectCategory(_selectedCategoryIndex.value)

                // 어미 목록도 갱신이 필요할 수 있으므로
                if (card.partOfSpeech == "E" || card.partOfSpeech == "ENDING" || card.categoryId == endingCategoryId) {
                    endingCategoryId?.let { id ->
                        _endingWords.value = repository.fetchWords(id).map { it.copy(partOfSpeech = "E") }
                    }
                }

                _eventFlow.emit(MainUiEvent.ShowSnackbar("삭제 완료"))
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("삭제 실패"))
            }
        }
    }

    // 4. 즐겨찾기 토글 (모달에서 호출)
    fun toggleFavorite(card: MainWordItem) {
        viewModelScope.launch {
            val newStatus = !card.isFavorite
            // ✅ Repository가 FavoriteResult를 반환함
            val result = repository.toggleFavorite(card.cardId, newStatus)

            if (result != null) {
                val msg = if (result.isFavorite) "즐겨찾기 추가됨" else "즐겨찾기 해제됨"
                _eventFlow.emit(MainUiEvent.ShowSnackbar(msg))

                // 리스트 갱신 (ID와 상태만 있으면 됨)
                updateCardInList(result.cardId, result.cardId, result.isFavorite) // ID 변경 없으면 그대로 사용

                // 즐겨찾기 탭이면 새로고침
                val currentName = _categories.value.getOrNull(_selectedCategoryIndex.value)?.name
                if (currentName == "즐겨찾기") {
                    selectCategory(_selectedCategoryIndex.value)
                }
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("즐겨찾기 변경 실패"))
            }
        }
    }

    // 내부 리스트 상태 동기화 헬퍼
    private fun updateCardInList(oldId: String, newId: String, isFavorite: Boolean) {
        _words.value = _words.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
        _endingWords.value = _endingWords.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
        _selectedCards.value = _selectedCards.value.map { if (it.cardId == oldId) it.copy(cardId = newId, isFavorite = isFavorite) else it }
    }

    // 페이지 인덱스 설정 (UI에서 호출)
    fun setWordPageIndex(index: Int) {
        _wordPageIndex.value = index
    }

    // 전체 문장 재생 (상단 재생 버튼)
    fun playSentence(context: Context) {
        val sentence = _selectedCards.value.joinToString(" ") { it.word }
        if (sentence.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("재생할 낱말이 없습니다.")) }
            return
        }
        playTts(context, sentence)
    }

    // 5. 단일 낱말 재생 (모달에서 호출)
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