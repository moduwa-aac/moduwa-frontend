package com.example.aac.ui.features.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.R
import com.example.aac.data.mapper.IconMapper
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.MainRepository
import com.example.aac.ui.components.CategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _words = MutableStateFlow<List<MainWordItem>>(emptyList())
    val words: StateFlow<List<MainWordItem>> = _words.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    private val _selectedCards = MutableStateFlow<List<MainWordItem>>(emptyList())
    val selectedCards: StateFlow<List<MainWordItem>> = _selectedCards.asStateFlow()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                val fixedCategories = listOf(
                    CategoryItem(name = "전체", iconRes = R.drawable.ic_category, isSelected = true, serverId = null),
                    CategoryItem(name = "즐겨찾기", iconRes = R.drawable.ic_favorite, isSelected = false, serverId = null)
                )

                val fetchedCategories = repository.getCategories()

                val serverCategories = fetchedCategories.map { item ->
                    CategoryItem(
                        name = item.name,
                        // [수정] 이름 기반 매핑 대신 서버의 iconKey를 사용하여 정확한 아이콘 표시
                        iconRes = IconMapper.toLocalResource(item.iconKey),
                        isSelected = false,
                        serverId = item.id
                    )
                }

                _categories.value = fixedCategories + serverCategories
                selectCategory(0)
            } catch (e: Exception) {
                Log.e("MainViewModel", "초기 데이터 로딩 실패: ${e.message}")
            }
        }
    }

    fun selectCategory(index: Int) {
        val currentList = _categories.value
        if (index !in currentList.indices) return

        _selectedCategoryIndex.value = index

        val updatedList = currentList.mapIndexed { i, item -> item.copy(isSelected = i == index) }
        _categories.value = updatedList

        val selectedItem = updatedList[index]

        viewModelScope.launch {
            val fetchedWords = when {
                index == 0 -> repository.getWords()
                index == 1 -> repository.getWords(onlyFavorite = true)
                else -> selectedItem.serverId?.let { repository.getWords(categoryId = it) } ?: emptyList()
            }
            _words.value = fetchedWords.distinctBy { it.cardId } // 중복 제거
        }
    }

    fun addCard(card: MainWordItem) {
        _selectedCards.value = _selectedCards.value + card
    }

    fun removeCard(index: Int) {
        val currentList = _selectedCards.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _selectedCards.value = currentList
        }
    }

    fun clearSelectedCards() {
        _selectedCards.value = emptyList()
    }
}
