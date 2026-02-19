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

    val serverCategories by viewModel.categories.collectAsState()
    val serverWords by viewModel.wordCards.collectAsState()
    val selectedWordCategoryId by viewModel.selectedWordCategoryId.collectAsState()

    val categoryList = remember { mutableStateListOf<CategoryEditData>() }

    LaunchedEffect(serverCategories) {
        categoryList.clear()
        categoryList.addAll(
            serverCategories.map { category ->
                CategoryEditData(
                    id = category.id,
                    title = category.name,
                    // 🔥 [수정] 직접 계산하지 않고 서버에서 온 wordCount 값을 그대로 사용
                    count = category.wordCount, 
                    iconRes = IconMapper.toLocalResource(category.iconKey),
                    iconUrl = category.iconUrl
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CategoryViewModel.UiEvent.SaveCompleted -> onBackClick()
                is CategoryViewModel.UiEvent.Error -> Log.e("CATEGORY_SCREEN", "에러 발생: ${event.message}")
            }
        }
    }

    BackHandler { onBackClick() }

    // 이미지 처리를 위한 공통 함수
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
                onBackClick = { onBackClick() },
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
                    },
                    onEditCategory = { id, name, iconRes, uri, bitmap ->
                        val finalUriString = processImage(uri, bitmap)?.toString()
                        viewModel.updateCategory(id, name, iconRes, 0, finalUriString)
                    },
                    onDeleteCategory = { id ->
                        categoryList.removeIf { it.id == id }
                        viewModel.deleteCategory(id)
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
                    }
                    showAddWordDialog = false
                }
            )
        }
    }
}
