package com.example.aac.ui.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.MainRepository
import com.example.aac.ui.components.CategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.aac.data.repository.SentenceDataRepository
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.ceil
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileOutputStream

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

    private val _eventFlow = MutableSharedFlow<MainUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentVoiceKey: String = "ADULT_FEMALE_DEFAULT"

    init {
        fetchInitialData()
    }


    private val _endingWords = MutableStateFlow<List<MainWordItem>>(emptyList())
    val endingWords: StateFlow<List<MainWordItem>> = _endingWords.asStateFlow()

    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                // 1. 0.5초 대기 (토큰 저장 타이밍 이슈 방지)
                kotlinx.coroutines.delay(500)

                // 2. 카테고리 목록 가져오기
                val fetchedCategories = repository.getCategories()
                Log.d("MainViewModel", "📜 서버 카테고리 목록: ${fetchedCategories.map { it.name }}")

                // 4. 어미 카테고리 찾기
                // 혹시 서버에 "어미"가 아니라 "Ending"이나 "조사"로 되어 있는지 확인 필요
                val endingCategory = fetchedCategories.find { it.name == "어미" }

                if (endingCategory != null) {
                    Log.d("MainViewModel", "✅ '어미' 카테고리 찾음! ID: ${endingCategory.id}")
                    val endings = repository.fetchWords(endingCategory.id)
                    Log.d("MainViewModel", "📦 가져온 어미 단어 개수: ${endings.size}")
                    _endingWords.value = endings
                } else {
                    Log.e("MainViewModel", "⚠️ '어미'라는 이름의 카테고리가 서버에 없습니다!")
                }
                if (fetchedCategories.isEmpty()) {
                    android.util.Log.e("MainViewModel", "카테고리가 비어있습니다!")
                    return@launch
                }

                // 3. 서버 데이터를 UI 모델로 변환
                val serverCategories = fetchedCategories
                    .filter { it.name != "어미" } // 어미는 상단 탭에서 제외
                    .map { item ->
                        val icon = when (item.name) {
                            "최근사용" -> R.drawable.ic_recent_use
                            "즐겨찾기" -> R.drawable.ic_favorite
                            "사람" -> R.drawable.ic_human
                            "행동" -> R.drawable.ic_act
                            "감정" -> R.drawable.ic_emotion
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

                // 5. 변환된 리스트 저장 및 UI 갱신
                _categories.value = serverCategories
                calculateCategoryPages(serverCategories.size)

                if (serverCategories.isNotEmpty()) {
                    selectCategory(0) // 첫 번째 카테고리(최근 사용 등) 자동 선택
                }
            } catch (e: Exception) {
                // 에러 발생 시 로그 출력 (앱이 멈추지 않게 함)
                e.printStackTrace()
                android.util.Log.e("MainViewModel", "초기 데이터 로드 실패: ${e.message}")
            }
        }
    }

    // 페이지 수 계산 함수
    private fun calculateCategoryPages(totalItemCount: Int) {
        if (totalItemCount == 0) {
            _categoryTotalPageCount.value = 1
        } else {
            _categoryTotalPageCount.value = ceil(totalItemCount.toDouble() / CATEGORY_ITEMS_PER_PAGE).toInt()
        }
    }

    // 다음 카테고리 페이지로
    fun nextCategoryPage() {
        if (_categoryPageIndex.value < _categoryTotalPageCount.value - 1) {
            _categoryPageIndex.value += 1
        }
    }

    // 이전 카테고리 페이지로
    fun prevCategoryPage() {
        if (_categoryPageIndex.value > 0) {
            _categoryPageIndex.value -= 1
        }
    }

    // 카테고리 선택 로직
    fun selectCategory(index: Int) {
        val currentList = _categories.value
        if (index !in currentList.indices) return

        _selectedCategoryIndex.value = index

        // 1. 선택 상태 갱신
        val updatedList = currentList.mapIndexed { i, item ->
            item.copy(isSelected = i == index)
        }
        _categories.value = updatedList

        // 2. 서버에 해당 카테고리 단어 요청
        val selectedItem = updatedList[index]
        viewModelScope.launch {
            val catId = selectedItem.serverId
            if (catId != null) {
                _words.value = repository.getWords(categoryId = catId)
            } else {
                _words.value = emptyList()
            }
        }
    }

    fun addCard(card: MainWordItem) {
        val currentList = _selectedCards.value
        if (currentList.size >= 20) {
            viewModelScope.launch {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("낱말 카드는 최대 20개까지만 선택할 수 있어요."))
            }
            return
        }

        val newList = currentList + card
        _selectedCards.value = newList
        SentenceDataRepository.selectedWords = newList
    }

    fun removeCard(index: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedCards.value = currentList
        }
        SentenceDataRepository.selectedWords = currentList
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

    fun syncWithRepository() {
        if (_selectedCards.value != SentenceDataRepository.selectedWords) {
            _selectedCards.value = SentenceDataRepository.selectedWords
        }
    }

    var showAddWordDialog by mutableStateOf(false)
        private set

    fun openAddWordDialog() { showAddWordDialog = true }
    fun closeAddWordDialog() { showAddWordDialog = false }

    fun createNewWord(word: String, imageUrl: String? = null) {
        val currentCatIndex = _selectedCategoryIndex.value
        val currentCategory = _categories.value.getOrNull(currentCatIndex)
        val categoryId = currentCategory?.serverId ?: return

        viewModelScope.launch {
            if (repository.createWord(categoryId, word, imageUrl)) {
                closeAddWordDialog()
                selectCategory(currentCatIndex)
            }
        }
    }

    fun updateWord(originalCard: MainWordItem, newWord: String, newCategoryName: String, newImageUrl: String?) {
        viewModelScope.launch {
            // 1. 카테고리 이름으로 ID 찾기
            val targetCategory = _categories.value.find { it.name == newCategoryName }
            val categoryId = targetCategory?.serverId

            // 2. 변경된 값 보냄
            val updatedWordItem = repository.updateWord(
                cardId = originalCard.cardId,
                categoryId = categoryId, // 카테고리 변경 시
                word = newWord,
                imageUrl = newImageUrl
            )

            if (updatedWordItem != null) {
                // 3. 성공 시: 현재 리스트 새로고침
                selectCategory(_selectedCategoryIndex.value)

                // 스낵바 알림
                _eventFlow.emit(MainUiEvent.ShowSnackbar("'${updatedWordItem.word}' (으)로 수정되었습니다."))
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("수정에 실패했습니다."))
            }
        }
    }

    fun deleteWord(cardId: String) {
        viewModelScope.launch {
            val isSuccess = repository.deleteWord(cardId)

            if (isSuccess) {
                // 1. UI 갱신 (현재 카테고리 다시 불러오기)
                selectCategory(_selectedCategoryIndex.value)
                // 2. 스낵바 알림
                _eventFlow.emit(MainUiEvent.ShowSnackbar("카드가 삭제되었습니다."))
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("삭제에 실패했습니다."))
            }
        }
    }

    // ✅ TTS 재생 함수 (MainScreen의 재생 버튼과 연결)
    fun playSentence(context: android.content.Context) { // Context 필요!
        // 1. 선택된 카드들을 공백으로 연결 (예: "나 밥 먹다")
        val sentence = _selectedCards.value.joinToString(" ") { it.word }

        if (sentence.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("재생할 낱말이 없습니다.")) }
            return
        }

        viewModelScope.launch {
            // 2. 서버에서 오디오 데이터(byte[]) 가져오기
            val audioBytes = repository.fetchTtsAudio(sentence, currentVoiceKey)

            if (audioBytes != null) {
                // 3. 오디오 재생 (Helper 함수 사용)
                playAudioFromBytes(context, audioBytes)
            } else {
                _eventFlow.emit(MainUiEvent.ShowSnackbar("TTS 재생 실패: 데이터를 불러오지 못했습니다."))
            }
        }
    }

    // 🎵 바이트 배열을 재생하는 헬퍼 함수
    private fun playAudioFromBytes(context: android.content.Context, audioData: ByteArray) {
        try {
            // 1. 임시 파일 생성 (cacheDir 사용)
            val tempFile = File.createTempFile("tts_audio", ".mp3", context.cacheDir)
            tempFile.deleteOnExit() // 앱 종료 시 삭제

            // 2. 파일에 바이트 쓰기
            val fos = FileOutputStream(tempFile)
            fos.write(audioData)
            fos.close()

            // 3. MediaPlayer로 재생
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            // 재생 끝나면 리소스 해제
            mediaPlayer.setOnCompletionListener {
                it.release()
                tempFile.delete() // 파일 삭제
            }

        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch { _eventFlow.emit(MainUiEvent.ShowSnackbar("오디오 재생 중 오류가 발생했습니다.")) }
        }
    }
}