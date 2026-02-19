package com.example.aac.ui.features.category.components

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.data.mapper.IconMapper
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.domain.model.Category
import com.example.aac.domain.model.Word
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.components.showCleanToast
import com.example.aac.ui.features.category.CategoryEditData
import com.example.aac.ui.features.category.CategoryViewModel
import com.example.aac.ui.features.category.CategoryViewModelFactory
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardEditDialog
import com.example.aac.util.FileUtil

@Composable
fun WordCardManagementContent(
    categories: List<Category>,
    wordList: List<Word>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit,
    viewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory()),
    onAddClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiList = remember { mutableStateListOf<Word>() }

    LaunchedEffect(wordList) {
        uiList.clear()
        uiList.addAll(wordList)
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            onCategorySelect(categories.first().id)
        }
    }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var targetWord by remember { mutableStateOf<Word?>(null) }

    val currentCategoryName = remember(selectedCategoryId, categories) {
        if (selectedCategoryId == null) {
            categories.firstOrNull()?.name ?: ""
        } else {
            categories.find { it.id == selectedCategoryId }?.name ?: ""
        }
    }

    val displayCategories = remember(categories) {
        categories.map { cat ->
            // iconUrl 필드 누락 매핑
            CategoryEditData(
                id = cat.id,
                title = cat.name,
                iconRes = IconMapper.toLocalResource(cat.iconKey),
                iconUrl = cat.iconUrl,
                count = cat.wordCount
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6F8)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.width(1116.dp).fillMaxHeight().padding(vertical = 12.dp)) {
            TipBox(text = "팁 : 낱말 카드를 드래그하여 순서를 변경하거나, 한 번 눌러 삭제, 길게 눌러 수정하실 수 있습니다.")
            Spacer(modifier = Modifier.height(10.dp))
            CategorySelectorBar(currentCategory = currentCategoryName, onClick = { showCategorySheet = true })
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (currentCategoryName.trim() == "어미") {
                    EndingWordContent(
                        wordList = uiList,
                        onAddClick = { 
                            if (uiList.size >= 5) {
                                showCleanToast(context, "어미는 최대 5개까지만 추가할 수 있어요.")
                            } else {
                                showAddDialog = true
                            }
                        }, 
                        onWordClick = { word ->
                            targetWord = word
                            showDeleteDialog = true
                        },
                        onWordLongClick = { word ->
                            targetWord = word
                            showEditDialog = true
                        }
                    )
                } else {
                    GeneralWordContent(
                        uiList = uiList,
                        onAddClick = { showAddDialog = true }, 
                        onWordClick = { word ->
                            targetWord = word
                            showDeleteDialog = true
                        },
                        onWordLongClick = { word ->
                            targetWord = word
                            showEditDialog = true
                        },
                        onReorder = { orderedIds ->
                            viewModel.markWordsForReorder(orderedIds)
                        }
                    )
                }
            }
        }
    }

    if (showCategorySheet) {
        CategorySelectionBottomSheet(
            categoryList = displayCategories,
            onDismissRequest = { showCategorySheet = false },
            onCategorySelected = { selectedItem ->
                onCategorySelect(selectedItem.id)
                showCategorySheet = false
            }
        )
    }

    if (showEditDialog && targetWord != null) {
        FlashcardEditDialog(
            card = MainWordItem(
                cardId = targetWord!!.cardId,
                categoryId = targetWord!!.categoryId,
                categoryName = targetWord!!.categoryName,
                partOfSpeech = targetWord!!.partOfSpeech,
                word = targetWord!!.word,
                imageUrl = targetWord!!.imageUrl,
                isDefault = targetWord!!.isDefault,
                isFavorite = targetWord!!.isFavorite,
                displayOrder = targetWord!!.displayOrder
            ),
            allCategories = categories,
            onDismiss = { showEditDialog = false },
            onSave = { newWordText, newCategoryId, newUri, newBitmap ->
                Log.e("WORD_UPDATE_DEBUG", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e("WORD_UPDATE_DEBUG", "1. UI 레이어 - 수정 프로세스 시작")

                val finalImageUrl = when {
                    newBitmap != null -> {
                        FileUtil.saveBitmapToGallery(context, newBitmap)?.toString()
                    }
                    newUri != null -> {
                        try {
                            val selectedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, newUri))
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(context.contentResolver, newUri)
                            }
                            FileUtil.saveBitmapToGallery(context, selectedBitmap)?.toString()
                        } catch (e: Exception) {
                            Log.e("WORD_DEBUG", "수정 시 갤러리 이미지 변환 실패: ${e.message}")
                            targetWord!!.imageUrl
                        }
                    }
                    else -> targetWord!!.imageUrl
                }

                viewModel.updateWord(
                    cardId = targetWord!!.cardId,
                    word = newWordText,
                    imageUrl = finalImageUrl,
                    categoryId = newCategoryId
                )
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && targetWord != null) {
        CommonDeleteDialog(
            message = "낱말 카드를\n삭제 하시겠어요?",
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                val cardId = targetWord!!.cardId
                uiList.removeIf { it.cardId == cardId }
                viewModel.markWordForDeletion(cardId)
                showDeleteDialog = false
                targetWord = null
            }
        )
    }

    if (showAddDialog) {
        AddWordCardDialog(
            onDismissRequest = { showAddDialog = false },
            onSaveClick = { text, uri, bitmap ->
                val targetCategoryId = selectedCategoryId ?: categories.firstOrNull()?.id
                if (targetCategoryId != null) {
                    Log.e("WORD_DEBUG", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.e("WORD_DEBUG", "1. UI 레이어 - 저장 시작")

                    val finalUri = when {
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
                                Log.e("WORD_DEBUG", "갤러리 이미지 변환 실패: ${e.message}")
                                null
                            }
                        }
                        else -> null
                    }

                    val imageUrlString = finalUri?.toString()
                    Log.e("WORD_DEBUG", "2. 최종 서버 전송 URI: $imageUrlString")

                    viewModel.createWord(
                        word = text,
                        imageUrl = imageUrlString,
                        categoryId = targetCategoryId
                    )
                }
                showAddDialog = false
            }
        )
    }
}
