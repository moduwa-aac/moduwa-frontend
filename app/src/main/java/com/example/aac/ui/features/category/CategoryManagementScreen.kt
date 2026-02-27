package com.example.aac.ui.features.category

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.data.mapper.IconMapper
import com.example.aac.ui.components.CommonSaveDialog
import com.example.aac.ui.components.CustomTopBar
import com.example.aac.ui.features.category.components.AddWordCardDialog
import com.example.aac.ui.features.category.components.ManagementTabRow
import com.example.aac.ui.features.category.components.WordCardManagementContent
import com.example.aac.ui.features.category.CategoryManagementContent
import com.example.aac.util.FileUtil

@Composable
fun CategoryManagementScreen(
    onBackClick: () -> Unit = {},
    viewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory())
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddWordDialog by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }
    // 🟢 1. 수동 액션(추가/수정/삭제) 감지용 변수
    var hasManualChanges by remember { mutableStateOf(false) }

    val serverCategories by viewModel.categories.collectAsState()
    val serverWords by viewModel.wordCards.collectAsState()
    val selectedWordCategoryId by viewModel.selectedWordCategoryId.collectAsState()

    val categoryList = remember { mutableStateListOf<CategoryEditData>() }

    // 🟢 2. 서버의 초기 카테고리 순서(ID 목록)를 기억해둡니다.
    val initialCategoryIds = remember(serverCategories) {
        serverCategories
            .filterNot { it.name == "최근사용" || it.name == "즐겨찾기" || it.name == "어미" }
            .map { it.id }
    }

    // 🟢 3. 현재 화면의 카테고리 순서(ID 목록)를 실시간으로 가져옵니다.
    val currentCategoryIds = categoryList.map { it.id }

    // 🟢 4. 순서가 달라졌는지 비교 (다르면 true)
    val isCategoryReordered = initialCategoryIds.isNotEmpty() && initialCategoryIds != currentCategoryIds

    // 🟢 5. 최종 변경사항 여부: 수동으로 뭔가 바꿨거나 OR 순서가 달라졌다면 변경된 것으로 간주!
    val hasChanges = hasManualChanges || isCategoryReordered

    LaunchedEffect(serverCategories) {
        categoryList.clear()
        categoryList.addAll(
            serverCategories
                .filterNot { it.name == "최근사용" || it.name == "즐겨찾기" || it.name == "어미" }
                .map { category ->
                    CategoryEditData(
                        id = category.id,
                        title = category.name,
                        count = category.wordCount,
                        iconRes = IconMapper.toLocalResource(category.iconKey),
                        iconUrl = category.iconUrl
                    )
                }
        )
        // 서버 데이터를 새로 불러왔을 때는 수동 변경사항 초기화
        hasManualChanges = false
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CategoryViewModel.UiEvent.SaveCompleted -> onBackClick()
                is CategoryViewModel.UiEvent.Error -> Log.e("CATEGORY_SCREEN", "에러 발생: ${event.message}")
            }
        }
    }

    BackHandler {
        if (hasChanges) showSaveDialog = true
        else onBackClick()
    }

    val processImage: (android.net.Uri?, android.graphics.Bitmap?) -> android.net.Uri? = { uri, bitmap ->
        when {
            bitmap != null -> FileUtil.saveBitmapToGallery(context, bitmap)
            uri != null -> {
                try {
                    val selectedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    FileUtil.saveBitmapToGallery(context, selectedBitmap)
                } catch (e: Exception) {
                    Log.e("CATEGORY_SCREEN", "이미지 변환 실패: ${e.message}")
                    null
                }
            }
            else -> null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
            CustomTopBar(
                title = if (selectedTabIndex == 0) "카테고리 관리" else "낱말 카드 관리",
                onBackClick = {
                    if (hasChanges) showSaveDialog = true
                    else onBackClick()
                },
                actionText = "저장하기",
                onActionClick = {
                    if (selectedTabIndex == 0) viewModel.saveCategoryList(categoryList)
                    else viewModel.saveWordCardChanges()
                }
            )

            ManagementTabRow(selectedTabIndex) { selectedTabIndex = it }

            if (selectedTabIndex == 0) {
                CategoryManagementContent(
                    categoryList = categoryList,
                    onAddCategory = { name, iconRes, uri, bitmap ->
                        val finalUri = processImage(uri, bitmap)
                        viewModel.createCategory(name, iconRes, finalUri?.toString())
                        hasManualChanges = true // 상태 업데이트
                    },
                    onEditCategory = { id, name, iconRes, uri, bitmap ->
                        val finalUriString = processImage(uri, bitmap)?.toString()
                        viewModel.updateCategory(id, name, iconRes, 0, finalUriString)
                        hasManualChanges = true // 상태 업데이트
                    },
                    onDeleteCategory = { id ->
                        categoryList.removeIf { it.id == id }
                        viewModel.deleteCategory(id)
                        hasManualChanges = true // 상태 업데이트
                    }
                )
            } else {
                WordCardManagementContent(
                    categories = serverCategories,
                    wordList = serverWords,
                    selectedCategoryId = selectedWordCategoryId,
                    onCategorySelect = { viewModel.fetchWords(it) },
                    viewModel = viewModel,
                    onAddClick = { showAddWordDialog = true }
                )
            }
        }

        if (showAddWordDialog) {
            AddWordCardDialog(
                onDismissRequest = { showAddWordDialog = false },
                onSaveClick = { word, uri, bitmap ->
                    val finalUri = processImage(uri, bitmap)
                    selectedWordCategoryId?.let { categoryId ->
                        viewModel.createWord(word, finalUri?.toString(), categoryId)
                        hasManualChanges = true // 낱말 추가 시에도 상태 업데이트
                    }
                    showAddWordDialog = false
                }
            )
        }

        if (showSaveDialog) {
            CommonSaveDialog(
                message = "변경사항을\n저장하시겠어요?",
                onDismiss = {
                    showSaveDialog = false
                    // onBackClick() // 저장 안하고 나갈 거면 여기 주석 해제!
                },
                onSave = {
                    showSaveDialog = false
                    if (selectedTabIndex == 0) {
                        viewModel.saveCategoryList(categoryList)
                    } else {
                        viewModel.saveWordCardChanges()
                    }
                }
            )
        }
    }
}