package com.example.aac.ui.features.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.ui.components.WordCard
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardDetailDialog
import com.example.aac.ui.features.main.components.*
import kotlinx.coroutines.launch
import com.example.aac.data.repository.SentenceDataRepository
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToAiSentence: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // ViewModel 상태 구독
    val categoryList by viewModel.categories.collectAsState()
    val wordList by viewModel.words.collectAsState()
    val selectedCategoryIndex by viewModel.selectedCategoryIndex.collectAsState()
    val selectedCards by viewModel.selectedCards.collectAsState()

    // 로컬 상태 관리
    var selectedDetailCard by remember { mutableStateOf<MainWordItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentPage by remember { mutableIntStateOf(0) }

    // 카테고리가 바뀌면 1페이지로 초기화
    LaunchedEffect(selectedCategoryIndex) { currentPage = 0 }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.syncWithRepository()
    }

    // 디자인 상수 정의 (간격 및 크기)
    val commonSpacing = 17.dp       // 모든 요소 간의 간격
    val rightColumnWidth = 92.dp    // 설정 버튼 및 우측 사이드바 너비
    val cardCornerRadius = 12.dp    // 카드 둥글기

    // 전체 화면 배경
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {

        Column(modifier = Modifier.fillMaxSize()) {

            TopSection(
                selectedCards = selectedCards,
                onRemoveCard = { index -> viewModel.removeCard(index) },
                onClearAll = { viewModel.clearSelectedCards() },
                onMoveCard = { from, to -> viewModel.moveCard(from, to) },
                onNavigateToAiSentence = {
                    SentenceDataRepository.selectedWords = selectedCards
                    onNavigateToAiSentence()
                }
            )

            Spacer(modifier = Modifier.height(commonSpacing))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBar(
                    categories = categoryList,
                    onCategoryClick = { index -> viewModel.selectCategory(index) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(commonSpacing))

                Surface(
                    onClick = { onNavigateToSettings() },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEEEEEE),
                    border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                    modifier = Modifier.size(width = rightColumnWidth, height = 68.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_setting),
                            contentDescription = "설정",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(text = "설정", fontSize = 18.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(commonSpacing))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val availableHeight = maxHeight

                    val controlBarWidth = 70.dp
                    val cardAreaWidth = maxWidth - controlBarWidth
                    val spacingTotal = commonSpacing * 6

                    val cardSize = (cardAreaWidth - spacingTotal) / 7
                    val rowHeight = cardSize + commonSpacing

                    val maxRows = (availableHeight / rowHeight).toInt().coerceAtLeast(1)

                    val dynamicPageSize = maxRows * 7

                    val maxPage = if (wordList.isEmpty()) 0 else (wordList.size - 1) / dynamicPageSize

                    if (currentPage > maxPage) currentPage = maxPage

                    val currentDisplayCards = wordList
                        .drop(currentPage * dynamicPageSize)
                        .take(dynamicPageSize)
                    // -------------------------

                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            if (wordList.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("등록된 낱말 카드가 없습니다.", color = Color.Gray, fontSize = 20.sp)
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(7),
                                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                                    verticalArrangement = Arrangement.spacedBy(commonSpacing),
                                    modifier = Modifier.fillMaxSize(),
                                    userScrollEnabled = false
                                ) {
                                    items(currentDisplayCards) { wordItem ->
                                        WordCard(
                                            text = wordItem.word,
                                            imageUrl = wordItem.imageUrl,
                                            partOfSpeech = wordItem.partOfSpeech,
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .combinedClickable(
                                                    onClick = { viewModel.addCard(wordItem) }, // 클릭 시 상단바 추가
                                                    onLongClick = { selectedDetailCard = wordItem } // 롱클릭 시 상세/수정/삭제
                                                ),
                                            cornerRadius = cardCornerRadius
                                        )
                                    }
                                }
                            }
                        }

                        CardControlBar(
                            onUpClick = { if (currentPage > 0) currentPage-- },
                            onDownClick = { if (currentPage < maxPage) currentPage++ },
                            modifier = Modifier
                                .width(controlBarWidth)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(commonSpacing))

                Column(
                    modifier = Modifier
                        .width(rightColumnWidth)
                        .fillMaxHeight()
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val buttonModifier = Modifier.weight(1f)
                    SmallReactionButton(R.drawable.ic_positive, "긍정", buttonModifier)
                    SmallReactionButton(R.drawable.ic_negative, "부정", buttonModifier)
                    SmallReactionButton(R.drawable.ic_question, "질문", buttonModifier)
                    SmallReactionButton(R.drawable.ic_request, "부탁", buttonModifier)
                    SmallReactionButton(R.drawable.ic_suggestion, "청유", buttonModifier)
                }
            }

            Spacer(modifier = Modifier.height(commonSpacing))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }

    if (selectedDetailCard != null) {
        FlashcardDetailDialog(
            card = selectedDetailCard,
            onDismiss = { selectedDetailCard = null },
            onEdit = { wordItem ->
                selectedDetailCard = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("'${wordItem.word}'")
                }
            },
            onDelete = { wordItem ->
                val deletedText = wordItem.word
                selectedDetailCard = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("'$deletedText' 카드가 삭제되었습니다.")
                }
            },
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope
        )
    }
}