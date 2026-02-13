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

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    // UI 상태: 카테고리 리스트
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

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
            // 1. 고정 카테고리 (전체, 즐겨찾기)
            val fixedCategories = listOf(
                CategoryItem(name = "전체", iconRes = R.drawable.ic_default, isSelected = true, serverId = null),
                CategoryItem(name = "즐겨찾기", iconRes = R.drawable.ic_favorite, isSelected = false, serverId = null)
            )

            // 2. 서버에서 카테고리 목록 가져오기
            val fetchedCategories = repository.getCategories()

            // 3. 서버 데이터를 UI 모델(CategoryItem)로 변환
            val serverCategories = fetchedCategories.map { item ->
                // 아이콘 매핑 로직 (이름 기반)
                val icon = when (item.name) {
                    "사람" -> R.drawable.ic_human
                    "행동" -> R.drawable.ic_act
                    "감정" -> R.drawable.ic_emotion
                    "음식" -> R.drawable.ic_food
                    "장소" -> R.drawable.ic_place
                    "신체" -> R.drawable.ic_human
                    "어미" -> R.drawable.ic_question
                    else -> R.drawable.ic_default
                }

                CategoryItem(
                    name = item.name,
                    iconRes = icon,
                    isSelected = false,
                    serverId = item.id
                )
            }

            // 4. 고정 카테고리 + 서버 카테고리 합치기
            val allCategories = fixedCategories + serverCategories
            _categories.value = allCategories

            // 5. 초기 선택 (0번: 전체)
            selectCategory(0)
        }
    }

    // 카테고리 선택 시 호출
    fun selectCategory(index: Int) {
        val currentList = _categories.value

        // 인덱스 범위 체크 (안전장치)
        if (index !in currentList.indices) return

        _selectedCategoryIndex.value = index

        // 1. UI 선택 상태(테두리 등) 갱신
        val updatedList = currentList.mapIndexed { i, item ->
            item.copy(isSelected = i == index)
        }
        _categories.value = updatedList

        // 2. 선택된 카테고리에 맞는 단어 목록 서버 요청
        val selectedItem = updatedList[index]

        viewModelScope.launch {
            val fetchedWords = when {
                index == 0 -> repository.getWords()

                index == 1 -> repository.getWords(onlyFavorite = true)

                // 그 외 서버 카테고리
                else -> {
                    val catId = selectedItem.serverId
                    if (catId != null) {
                        repository.getWords(categoryId = catId)
                    } else {
                        emptyList()
                    }
                }
            }
            _words.value = fetchedWords
        }
    }

    // 상단 카드 추가
    fun addCard(card: MainWordItem) {
        _selectedCards.value = _selectedCards.value + card
    }

    // 상단 카드 삭제
    fun removeCard(index: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedCards.value = currentList
        }
    }

    // 상단 카드 전체 삭제
    fun clearSelectedCards() {
        _selectedCards.value = emptyList()
    }

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            java.util.Collections.swap(currentList, fromIndex, toIndex)
            _selectedCards.value = currentList
        }
    }
}