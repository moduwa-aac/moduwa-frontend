package com.example.aac.ui.features.category.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.data.mapper.IconMapper
import com.example.aac.domain.model.Category
import com.example.aac.domain.model.Word
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.features.category.CategoryEditData

@Composable
fun WordCardManagementContent(
    categories: List<Category>,
    wordList: List<Word>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit
) {
    // 🔍 [로그 1] 상태 변경 확인
    LaunchedEffect(selectedCategoryId) {
        val name = categories.find { it.id == selectedCategoryId }?.name ?: "전체"
        Log.d("CATEGORY_DEBUG", "🔄 [UI 필터 갱신] 선택된 ID: $selectedCategoryId ($name)")
    }

    // 🔥 [핵심 1] 화면에 보여줄 낱말 리스트 필터링 (Client-Side Filtering)
    // 서버가 전체를 주더라도, 여기서 내 카테고리꺼만 골라내서 보여줍니다.
    val filteredWords = remember(wordList, selectedCategoryId) {
        if (selectedCategoryId == null) {
            wordList // 전체 선택이면 다 보여줌
        } else {
            // 선택된 카테고리 ID와 일치하는 낱말만 남김
            wordList.filter { it.categoryId == selectedCategoryId }
        }
    }

    // 필터링된 리스트를 UI 상태로 변환
    val uiList = remember(filteredWords) { filteredWords.toMutableStateList() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf<Word?>(null) }

    val currentCategoryName = remember(selectedCategoryId, categories) {
        if (selectedCategoryId == null) "전체"
        else categories.find { it.id == selectedCategoryId }?.name ?: "전체"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .width(1116.dp)
                .fillMaxHeight()
                .padding(vertical = 12.dp)
        ) {
            TipBox(text = "팁 : 낱말 카드를 드래그하여 순서를 변경하실 수 있어요. 자주 사용하는 낱말을 쉽게 찾을 수 있는 위치에 배치해보세요!")

            Spacer(modifier = Modifier.height(10.dp))

            CategorySelectorBar(
                currentCategory = currentCategoryName,
                onClick = { showCategorySheet = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (currentCategoryName.trim() == "어미") {
                    EndingWordContent(onAddClick = { showAddDialog = true })
                } else {
                    // 🔥 필터링된 uiList를 전달
                    GeneralWordContent(
                        uiList = uiList,
                        onAddClick = { showAddDialog = true },
                        onWordClick = { item ->
                            selectedWord = item
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) showAddDialog = false

    if (showCategorySheet) {
        // 🔥 [핵심 2] 바텀시트 데이터 생성 시 ID 누락 방지
        val sheetData = remember(categories) {
            val allOption = CategoryEditData(id = null, title = "전체", iconRes = R.drawable.ic_default, count = 0)

            val mappedCategories = categories.map { category ->
                // 로그를 찍어서 매핑 시점에 ID가 있는지 확인
                if (category.id.isEmpty()) Log.e("CATEGORY_DEBUG", "⚠️ 경고: 카테고리 [${category.name}]의 ID가 비어있습니다!")

                CategoryEditData(
                    id = category.id, // 여기서 ID가 잘 들어가는지 확인
                    title = category.name,
                    iconRes = IconMapper.toLocalResource(category.iconKey),
                    count = 0
                )
            }
            listOf(allOption) + mappedCategories
        }

        CategorySelectionBottomSheet(
            categoryList = sheetData,
            onDismissRequest = { showCategorySheet = false },
            onCategorySelected = { selectedItem ->
                Log.d("CATEGORY_DEBUG", "👉 [사용자 선택] ${selectedItem.title} (ID: ${selectedItem.id})")

                // 뷰모델 호출
                onCategorySelect(selectedItem.id)
                showCategorySheet = false
            }
        )
    }

    if (showDeleteDialog && selectedWord != null) {
        CommonDeleteDialog(
            message = "낱말 카드를\n삭제 하시겠어요?",
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                uiList.remove(selectedWord)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun CategorySelectorBar(
    currentCategory: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "카테고리 선택",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(text = currentCategory, fontSize = 20.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}