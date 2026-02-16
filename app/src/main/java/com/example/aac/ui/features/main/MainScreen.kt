package com.example.aac.ui.features.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardEditDialog
import kotlinx.coroutines.flow.collectLatest

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
    val endingWords by viewModel.endingWords.collectAsState() // 어미 카드 리스트

    // 로컬 상태 관리
    var selectedDetailCard by remember { mutableStateOf<MainWordItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentPage by remember { mutableIntStateOf(0) }

    // 카테고리 페이지네이션 관련
    val categoryPageIndex by viewModel.categoryPageIndex.collectAsState()
    val visibleCategories = remember(categoryList, categoryPageIndex) {
        categoryList.chunked(8).getOrNull(categoryPageIndex) ?: emptyList()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // 카테고리가 바뀌면 1페이지로 초기화
    LaunchedEffect(selectedCategoryIndex) { currentPage = 0 }

    // 스낵바 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is MainUiEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // 화면 복귀 시 동기화
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.syncWithRepository()
    }

    // 낱말 추가 다이얼로그 (생성)
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F8F8))) {

        Column(modifier = Modifier.fillMaxSize()) {

            // 1. 상단 섹션 (선택된 카드 목록)
            TopSection(
                selectedCards = selectedCards,
                onRemoveCard = { index -> viewModel.removeCard(index) },
                onClearAll = { viewModel.clearSelectedCards() },
                onMoveCard = { from, to -> viewModel.moveCard(from, to) },
                onNavigateToAiSentence = {
                    SentenceDataRepository.selectedWords = selectedCards
                    onNavigateToAiSentence()
                },
                onPlaySentence = { viewModel.playSentence(context) }
            )

            Spacer(modifier = Modifier.height(commonSpacing))

            // 2. 카테고리 & 설정바
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
                    // 카테고리 바의 페이지 이동 버튼 연결 (필요 시)
                     onPrevClick = { viewModel.prevCategoryPage() },
                     onNextClick = { viewModel.nextCategoryPage() },
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

            // 3. 메인 낱말 리스트 영역 (좌측 리스트 + 우측 사이드바)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // [좌측] 낱말 카드 그리드 및 페이지네이션
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val availableHeight = maxHeight
                    val contentHeight = availableHeight - 32.dp // 패딩 제외

                    val availableWidth = maxWidth - 70.dp - commonSpacing // 컨트롤바, 간격 제외
                    val cardSize = (availableWidth - (commonSpacing * (columnCount - 1))) / columnCount
                    val rowHeight = cardSize + commonSpacing

                    // 한 페이지에 들어갈 수 있는 행(Row)의 개수
                    val maxRows = (contentHeight / rowHeight).toInt().coerceAtLeast(1)

                    // 한 페이지당 아이템 개수 (행 * 열)
                    val pageSize = maxRows * columnCount

                    // 0번(최근 사용)이 아니면 '추가 버튼'이 맨 앞에 하나 있다고 가정
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
                        // 낱말 그리드
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
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                                    verticalArrangement = Arrangement.spacedBy(commonSpacing),
                                    modifier = Modifier.fillMaxSize(),
                                    userScrollEnabled = false // 스크롤 막음 (페이지네이션 사용)
                                ) {
                                    items(currentItemCount) { index ->
                                        val absoluteIndex = startIndex + index

                                        if (hasAddButton && absoluteIndex == 0) {
                                            // 추가 버튼
                                            DashedAddCardItem(
                                                modifier = Modifier.aspectRatio(1f),
                                                onClick = { viewModel.openAddWordDialog() }
                                            )
                                        } else {
                                            // 낱말 카드
                                            val wordDataIndex = if (hasAddButton) absoluteIndex - 1 else absoluteIndex

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
                            onUpClick = { if (currentPage > 0) currentPage-- },
                            onDownClick = { if (currentPage < maxPage) currentPage++ },
                            canScrollUp = currentPage > 0,
                            canScrollDown = currentPage < maxPage,
                            modifier = Modifier
                                .width(70.dp)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(commonSpacing))

                // [우측] 사이드바 (어미 카드 리스트)
                Column(
                    modifier = Modifier
                        .width(rightColumnWidth)
                        .fillMaxHeight()
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (endingWords.isEmpty()) {
                        // ✅ 데이터가 없을 때 로딩 대신 '안내 문구' 표시
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "어미\n없음",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // 다시 시도 버튼 (선택 사항)
                                IconButton(onClick = { viewModel.selectCategory(viewModel.selectedCategoryIndex.value) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_refresh), // 아이콘 없으면 ic_default 사용
                                        contentDescription = "새로고침",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        // ✅ 데이터가 있으면 리스트 표시
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 6.dp)
                        ) {
                            items(endingWords.size) { index ->
                                val endingItem = endingWords[index]
                                WordCard(
                                    text = endingItem.word,
                                    imageUrl = endingItem.imageUrl,
                                    partOfSpeech = endingItem.partOfSpeech,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .aspectRatio(1f)
                                        .combinedClickable(
                                            onClick = { viewModel.addCard(endingItem) },
                                            onLongClick = { selectedDetailCard = endingItem }
                                        ),
                                    cornerRadius = 8.dp,
                                    fontSize = 14.sp,
                                    iconSize = 32.dp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(commonSpacing))
        }

        // 스낵바 호스트
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }

    // --- 다이얼로그 처리 ---

    // 1. 상세(Detail) 다이얼로그 (수정 모드가 아닐 때)
    if (selectedDetailCard != null && !showEditDialog) {
        FlashcardDetailDialog(
            card = selectedDetailCard!!,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
            onDismiss = { selectedDetailCard = null },
            onDelete = { wordItem ->
                // 삭제 API 호출
                viewModel.deleteWord(wordItem.cardId)
                selectedDetailCard = null
            },
            onEdit = {
                // 수정 모드로 전환
                showEditDialog = true
            },
            // TODO: 즐겨찾기, 재생 등 추가 구현
            onFavorite = { _, _ -> },
            onPlay = { }
        )
    }

    // 2. 수정(Edit) 다이얼로그 (수정 모드일 때)
    if (selectedDetailCard != null && showEditDialog) {
        // 실제 수정 가능한 카테고리만 필터링 (최근사용/즐겨찾기 제외)
        val realCategories = categoryList.filter {
            it.name != "최근 사용" && it.name != "최근사용" && it.name != "즐겨찾기"
        }

        FlashcardEditDialog(
            card = selectedDetailCard!!,
            categories = realCategories,
            onDismiss = {
                showEditDialog = false
                selectedDetailCard = null
            },
            onSave = { newWord, newCategory, newImage ->
                // 수정 API 호출
                viewModel.updateWord(selectedDetailCard!!, newWord, newCategory, newImage)
                showEditDialog = false
                selectedDetailCard = null
            }
        )
    }
}