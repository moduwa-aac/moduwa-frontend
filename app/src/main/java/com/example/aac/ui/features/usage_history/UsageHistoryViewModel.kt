package com.example.aac.ui.features.usage_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.repository.UsageHistoryRepository
import com.example.aac.domain.model.UsageHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsageHistoryViewModel(
    private val repository: UsageHistoryRepository = UsageHistoryRepository()
) : ViewModel() {

    // 화면이 바라볼 진짜 데이터 리스트
    private val _histories = MutableStateFlow<List<UsageHistory>>(emptyList())
    val histories = _histories.asStateFlow()

    // 연도와 월이 바뀌면 호출되는 함수
    fun fetchHistories(year: Int, month: Int) {
        viewModelScope.launch {
            // Repository야, 데이터 가져와!
            val data = repository.getHistoryList(year, month)
            _histories.value = data
        }
    }
}