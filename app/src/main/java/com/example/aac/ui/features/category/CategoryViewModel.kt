package com.example.aac.ui.features.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aac.data.mapper.IconMapper
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.repository.CategoryRepositoryImpl
import com.example.aac.domain.model.Category
import com.example.aac.domain.model.Word
import com.example.aac.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    private var allWords: List<Word> = emptyList()

    private val _wordCards = MutableStateFlow<List<Word>>(emptyList())
    val wordCards = _wordCards.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedWordCategoryId = MutableStateFlow<String?>(null)
    val selectedWordCategoryId = _selectedWordCategoryId.asStateFlow()

    private val _pendingDeleteCardIds = MutableStateFlow<Set<String>>(emptySet())
    private var pendingOrderedCardIds: List<String>? = null

    sealed class UiEvent {
        object SaveCompleted : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        refreshAllData()
    }

    private fun findFirstValidCategoryId(list: List<Category>): String? {
        return list.firstOrNull { it.name != "최근사용" && it.name != "즐겨찾기" }?.id
    }

    fun refreshAllData() {
        viewModelScope.launch {
            repository.getCategories().onSuccess { categoryList ->
                _categories.value = categoryList
                
                if (_selectedWordCategoryId.value == null) {
                    _selectedWordCategoryId.value = findFirstValidCategoryId(categoryList)
                }
                
                repository.getWords(null).onSuccess { list ->
                    allWords = list.distinctBy { it.cardId }
                    updateDisplayList()
                }.onFailure {
                    Log.e("CategoryViewModel", "낱말 조회 실패: ${it.message}")
                }
            }.onFailure {
                Log.e("CategoryViewModel", "카테고리 조회 실패: ${it.message}")
            }
        }
    }

    private suspend fun fetchCategoriesInternal() {
        repository.getCategories().onSuccess { categoryList ->
            _categories.value = categoryList
            if (_selectedWordCategoryId.value == null) {
                _selectedWordCategoryId.value = findFirstValidCategoryId(categoryList)
            }
        }.onFailure {
            Log.e("CategoryViewModel", "카테고리 조회 실패: ${it.message}")
        }
    }

    private suspend fun fetchWordsInternal() {
        repository.getWords(null).onSuccess { list ->
            allWords = list.distinctBy { it.cardId }
            updateDisplayList()
        }.onFailure {
            Log.e("CategoryViewModel", "낱말 조회 실패: ${it.message}")
        }
    }

    fun fetchWords(categoryId: String?) {
        _selectedWordCategoryId.value = categoryId
        updateDisplayList()
    }

    private fun updateDisplayList() {
        val categoryId = _selectedWordCategoryId.value
        _wordCards.value = if (categoryId == null) {
            allWords
        } else {
            allWords.filter { it.categoryId == categoryId }
        }
    }

    fun createWord(word: String, imageUrl: String?, categoryId: String) {
        viewModelScope.launch {
            repository.createWord(categoryId, word, imageUrl).onSuccess {
                refreshAllData()
            }.onFailure {
                Log.e("CategoryViewModel", "낱말 생성 실패: ${it.message}")
                _eventFlow.emit(UiEvent.Error("낱말 생성에 실패했습니다."))
            }
        }
    }

    fun updateWord(cardId: String, word: String, imageUrl: String?, categoryId: String) {
        viewModelScope.launch {
            repository.updateWord(cardId, word, imageUrl, categoryId).onSuccess {
                refreshAllData()
            }
        }
    }

    fun saveWordCardChanges() {
        val currentCategoryId = _selectedWordCategoryId.value
        val deletes = _pendingDeleteCardIds.value.toList()
        val reorderIds = pendingOrderedCardIds

        viewModelScope.launch {
            try {
                for (id in deletes) repository.deleteWord(id)
                if (currentCategoryId != null && reorderIds != null) {
                    val finalReorderList = reorderIds.filterNot { _pendingDeleteCardIds.value.contains(it) }
                    repository.reorderWords(currentCategoryId, finalReorderList)
                }
                refreshAllData()
                _eventFlow.emit(UiEvent.SaveCompleted)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.Error("저장 실패"))
            }
        }
    }

    fun markWordForDeletion(cardId: String) { _pendingDeleteCardIds.value += cardId }
    fun markWordsForReorder(orderedIds: List<String>) { pendingOrderedCardIds = orderedIds }

    fun createCategory(name: String, iconRes: Int, iconUrl: String? = null) {
        viewModelScope.launch {
            val iconKey = if (!iconUrl.isNullOrBlank()) null else IconMapper.toRemoteKey(iconRes)
            repository.createCategory(name, iconKey, iconUrl).onSuccess {
                fetchCategoriesInternal()
            }.onFailure {
                Log.e("CategoryViewModel", "생성 실패: ${it.message}")
                _eventFlow.emit(UiEvent.Error("카테고리 생성에 실패했습니다."))
            }
        }
    }

    fun updateCategory(id: String, name: String, iconRes: Int, order: Int, iconUrl: String? = null) {
        viewModelScope.launch {
            val iconKey = if (!iconUrl.isNullOrBlank()) null else IconMapper.toRemoteKey(iconRes)
            repository.updateCategory(id, name, iconKey, order, iconUrl).onSuccess {
                fetchCategoriesInternal()
            }.onFailure {
                Log.e("CategoryViewModel", "카테고리 수정 실패: ${it.message}")
                _eventFlow.emit(UiEvent.Error("카테고리 수정에 실패했습니다."))
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id).onSuccess {
                fetchCategoriesInternal()
            }
        }
    }

    /**
     * 순서 저장 시 화면에서 보이지 않는 고정 카테고리를 포함하여 전송
     */
    fun saveCategoryList(editedList: List<CategoryEditData>) {
        viewModelScope.launch {
            // 1. 서버에 있는 전체 카테고리 중, 화면에서 숨겨진 고정 카테고리들만 추출
            val fixedCategories = categories.value.filter { 
                it.name == "최근사용" || it.name == "즐겨찾기" || it.name == "어미" 
            }

            // 2. 고정 카테고리들을 맨 앞에 두고, 그 뒤에 사용자가 수정한 리스트를 합침
            val fullOrderedIds = (fixedCategories.map { it.id } + editedList.mapNotNull { it.id }).distinct()

            // 3. 전체 리스트에 대한 displayOrder 맵 생성
            val finalOrderMap = fullOrderedIds.mapIndexed { index, id ->
                id to index
            }.toMap()
            
            if (finalOrderMap.isNotEmpty()) {
                Log.d("CategoryViewModel", "전체 순서 저장 요청 (총 ${finalOrderMap.size}개)")
                repository.updateCategoryOrders(finalOrderMap).onSuccess {
                    _eventFlow.emit(UiEvent.SaveCompleted)
                }.onFailure {
                    Log.e("CategoryViewModel", "순서 저장 API 실패: ${it.message}")
                    _eventFlow.emit(UiEvent.Error("순서 저장 실패"))
                }
            } else {
                _eventFlow.emit(UiEvent.SaveCompleted)
            }
        }
    }
}

class CategoryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            return CategoryViewModel(CategoryRepositoryImpl(RetrofitInstance.api)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
