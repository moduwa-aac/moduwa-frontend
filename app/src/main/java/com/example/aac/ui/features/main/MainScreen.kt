package com.example.aac.ui.features.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.SentenceDataRepository
import com.example.aac.ui.components.DashedAddCardItem
import com.example.aac.ui.components.WordCard
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardDetailDialog
import com.example.aac.ui.features.main.components.*
import kotlinx.coroutines.launch
import com.example.aac.ui.features.category.components.AddWordCardDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToAiSentence: () -> Unit = {},
    onNavigateToAddWord: () -> Unit = {},
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

    val categories by viewModel.categories.collectAsState()
    val categoryPageIndex by viewModel.categoryPageIndex.collectAsState()

    val visibleCategories = remember(categories, categoryPageIndex) {
        categories.chunked(8).getOrNull(categoryPageIndex) ?: emptyList()
    }

    // 카테고리가 바뀌면 1페이지로 초기화
    LaunchedEffect(selectedCategoryIndex) { currentPage = 0 }

    // 화면 복귀 시 동기화
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.syncWithRepository()
    }

    if (viewModel.showAddWordDialog) {
        AddWordCardDialog(
            onDismissRequest = { viewModel.closeAddWordDialog() },
            onSaveClick = { word, imageUri ->
                viewModel.createNewWord(word, imageUri)
            }
        )
    }

    // 디자인 상수
    val commonSpacing = 17.dp
    val rightColumnWidth = 92.dp
    val cardCornerRadius = 12.dp
    val columnCount = 7 // 가로 칸 개수 고정

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 상단바
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

            // 카테고리 & 설정바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBar(
                    categories = visibleCategories,
                    onCategoryClick = { localIndex ->
                        val globalIndex = (categoryPageIndex * 8) + localIndex
                        viewModel.selectCategory(globalIndex)
                    },
                    onPrevClick = {
                        viewModel.prevCategoryPage()
                    },
                    onNextClick = {
                        viewModel.nextCategoryPage()
                    },
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

            // 메인 낱말 리스트 영역
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // 화면 높이를 측정해서 페이지당 행(Row) 개수 계산
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val availableHeight = maxHeight

                    // 1. 레이아웃 계산
                    val contentHeight = availableHeight - 32.dp

                    val availableWidth = maxWidth - 70.dp - commonSpacing // 컨트롤바, 간격 제외
                    val cardSize = (availableWidth - (commonSpacing * (columnCount - 1))) / columnCount

                    val rowHeight = cardSize + commonSpacing

                    // 한 페이지에 들어갈 수 있는 행(Row)의 개수
                    val maxRows = (contentHeight / rowHeight).toInt().coerceAtLeast(1)

                    // 한 페이지당 아이템 개수 (행 * 열)
                    val pageSize = maxRows * columnCount

                    // 2. 페이지네이션 데이터 계산
                    val hasAddButton = selectedCategoryIndex != 0

                    // 전체 아이템 개수 (버튼 포함)
                    val totalItemCount = wordList.size + (if (hasAddButton) 1 else 0)

                    // 최대 페이지 (0부터 시작)
                    val maxPage = if (totalItemCount == 0) 0 else (totalItemCount - 1) / pageSize

                    // 페이지 범위 안전장치
                    if (currentPage > maxPage) currentPage = maxPage

                    // 현재 페이지의 시작/끝 인덱스
                    val startIndex = currentPage * pageSize
                    val endIndex = minOf(startIndex + pageSize, totalItemCount)

                    // 현재 화면에 그릴 아이템 개수
                    val currentItemCount = if (endIndex > startIndex) endIndex - startIndex else 0

                    Row(modifier = Modifier.fillMaxSize()) {
                        // 낱말 그리드 배경
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            if (totalItemCount == 0) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("등록된 낱말 카드가 없습니다.", color = Color.Gray, fontSize = 20.sp)
                                }
                            } else {
                                // 스크롤 금지된 그리드
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                                    verticalArrangement = Arrangement.spacedBy(commonSpacing),
                                    modifier = Modifier.fillMaxSize(),
                                    userScrollEnabled = false
                                ) {
                                    items(currentItemCount) { index ->
                                        // 현재 칸의 절대 인덱스 (전체 리스트 기준)
                                        val absoluteIndex = startIndex + index

                                        if (hasAddButton && absoluteIndex == 0) {
                                            // 0번 칸이고 버튼이 필요한 경우 -> 추가 버튼 렌더링
                                            DashedAddCardItem(
                                                modifier = Modifier.aspectRatio(1f),
                                                onClick = { viewModel.openAddWordDialog() }
                                            )
                                        } else {
                                            // 그 외 -> 낱말 카드 렌더링
                                            val wordDataIndex = if (hasAddButton) absoluteIndex - 1 else absoluteIndex

                                            // 인덱스 범위 체크 (혹시 모를 오류 방지)
                                            if (wordDataIndex in wordList.indices) {
                                                val wordItem = wordList[wordDataIndex]
                                                WordCard(
                                                    text = wordItem.word,
                                                    imageUrl = wordItem.imageUrl,
                                                    partOfSpeech = wordItem.partOfSpeech,
                                                    modifier = Modifier
                                                        .aspectRatio(1f)
                                                        .combinedClickable(
                                                            onClick = { viewModel.addCard(wordItem) },
                                                            onLongClick = { selectedDetailCard = wordItem }
                                                        ),
                                                    cornerRadius = cardCornerRadius
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(commonSpacing))

                        // 페이지 이동 컨트롤러
                        CardControlBar(
                            onUpClick = { if (currentPage > 0) currentPage-- }, // 이전 페이지
                            onDownClick = { if (currentPage < maxPage) currentPage++ }, // 다음 페이지
                            canScrollUp = currentPage > 0,
                            canScrollDown = currentPage < maxPage,
                            modifier = Modifier
                                .width(70.dp)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(commonSpacing))

                // 우측 반응 버튼
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
            card = selectedDetailCard!!,
            onDismiss = { selectedDetailCard = null },
            onEdit = { wordItem ->
                selectedDetailCard = null
                coroutineScope.launch { snackbarHostState.showSnackbar("'${wordItem.word}'") }
            },
            onDelete = { wordItem ->
                selectedDetailCard = null
                coroutineScope.launch { snackbarHostState.showSnackbar("'${wordItem.word}' 카드가 삭제되었습니다.") }
            },
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope
        )
    }
}