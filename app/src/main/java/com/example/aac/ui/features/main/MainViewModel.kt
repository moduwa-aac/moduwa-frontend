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
import kotlin.math.ceil

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    // ✅ [수정] 상수명 변경: 카테고리는 한 페이지에 8개
    private val CATEGORY_ITEMS_PER_PAGE = 8

    // UI 상태: 전체 카테고리 리스트 (서버에서 받은 원본)
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    // ✅ [수정] 변수명 변경: 낱말 카드 페이지와 겹치지 않게 'Category' 명시
    private val _categoryPageIndex = MutableStateFlow(0)
    val categoryPageIndex: StateFlow<Int> = _categoryPageIndex.asStateFlow()

    // ✅ [수정] 변수명 변경: 카테고리 총 페이지 수
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

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            // 1. 서버에서 카테고리 목록 가져오기
            val fetchedCategories = repository.getCategories()

            // 2. 서버 데이터를 UI 모델로 변환 (필터링 포함)
            val serverCategories = fetchedCategories
                .filter { it.name != "어미" } // ✅ "어미" 카테고리 제외
                .map { item ->
                    val icon = when (item.name) {
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

            // 3. 변환된 리스트 저장
            _categories.value = serverCategories

            // 4. ✅ 총 페이지 수 계산
            calculateCategoryPages(serverCategories.size)

            // 5. 데이터가 있다면 첫 번째 카테고리 자동 선택
            if (serverCategories.isNotEmpty()) {
                selectCategory(0)
            }
        }
    }

    // ✅ 페이지 수 계산 함수
    private fun calculateCategoryPages(totalItemCount: Int) {
        if (totalItemCount == 0) {
            _categoryTotalPageCount.value = 1
        } else {
            _categoryTotalPageCount.value = ceil(totalItemCount.toDouble() / CATEGORY_ITEMS_PER_PAGE).toInt()
        }
    }

    // ✅ 다음 카테고리 페이지로
    fun nextCategoryPage() {
        if (_categoryPageIndex.value < _categoryTotalPageCount.value - 1) {
            _categoryPageIndex.value += 1
        }
    }

    // ✅ 이전 카테고리 페이지로
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

    // ... (카드 추가/삭제/이동 로직은 기존과 동일) ...
    fun addCard(card: MainWordItem) {
        val newList = _selectedCards.value + card
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

    // ... (다이얼로그 로직) ...
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
}