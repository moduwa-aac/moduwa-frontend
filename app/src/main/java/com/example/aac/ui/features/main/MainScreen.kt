package com.example.aac.ui.features.main

import android.R.attr.category
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.data.repository.SentenceDataRepository
import com.example.aac.ui.components.DashedAddCardItem
import com.example.aac.ui.components.WordCard
import com.example.aac.ui.features.category.components.AddWordCardDialog
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardDetailDialog
import com.example.aac.ui.features.flashcard_edit_delete.FlashcardEditDialog
import com.example.aac.ui.features.main.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    // ✅ [수정] 페이지 인덱스를 ViewModel에서 구독 (자동 스크롤 기능)
    val currentPage by viewModel.wordPageIndex.collectAsState()

    // 로컬 상태 관리
    var selectedDetailCard by remember { mutableStateOf<MainWordItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // 카테고리 페이지네이션 관련
    val categoryPageIndex by viewModel.categoryPageIndex.collectAsState()
    val visibleCategoriesWithPadding = remember(categoryList, categoryPageIndex) {
        val chunkSize = 8
        val pages = categoryList.chunked(chunkSize)
        val currentPageItems = pages.getOrNull(categoryPageIndex) ?: emptyList()

        // 부족한 개수만큼 null을 채워서 무조건 8개짜리 리스트 생성
        currentPageItems + List(chunkSize - currentPageItems.size) { null }
    }

    // 카테고리가 바뀌면 1페이지로 초기화 (뷰모델 함수 호출)
    LaunchedEffect(selectedCategoryIndex) {
        viewModel.setWordPageIndex(0)
    }

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
    // 낱말 추가 다이얼로그 (생성)
    if (viewModel.showAddWordDialog) {
        AddWordCardDialog(
            onDismissRequest = { viewModel.closeAddWordDialog() },
            // ✅ [수정] 파라미터에 bitmap 추가 및 uri를 String으로 변환하여 전달
            onSaveClick = { word, uri, bitmap ->
                // 만약 ViewModel의 createNewWord가 String?을 받는다면 toString() 처리
                viewModel.createNewWord(word, uri?.toString())
                // 참고: bitmap 처리가 필요하다면 뷰모델 함수를 확장해야 하지만,
                // 일단 에러 해결을 위해 uri 위주로 전달합니다.
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
                    // ✅ [기능 2] AI 요청 데이터는 ViewModel에서 이미 처리됨. 화면 이동만 수행
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
                    categories = visibleCategoriesWithPadding,
                    onCategoryClick = { localIndex ->
                        val globalIndex = (categoryPageIndex * 8) + localIndex
                        // 실제 데이터가 있는 경우만 클릭 처리
                        if (globalIndex < categoryList.size) {
                            viewModel.selectCategory(globalIndex)
                        }
                    },
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
                    val contentHeight = availableHeight - 32.dp
                    val availableWidth = maxWidth - 70.dp - commonSpacing
                    val cardSize = (availableWidth - (commonSpacing * (columnCount - 1))) / columnCount
                    val rowHeight = cardSize + commonSpacing
                    val maxRows = (contentHeight / rowHeight).toInt().coerceAtLeast(1)
                    val pageSize = maxRows * columnCount

                    // ✅ [1] 카테고리 확인 (공백 제거 후 비교)
                    val currentCategoryName = categoryList.getOrNull(selectedCategoryIndex)?.name?.replace(" ", "") ?: ""

                    // "최근사용", "즐겨찾기"인지 확인
                    val isSpecialCategory = currentCategoryName == "최근사용" || currentCategoryName == "즐겨찾기"

                    // 특수 카테고리면 추가 버튼 숨김
                    val hasAddButton = !isSpecialCategory

                    // 전체 아이템 개수 계산
                    val totalItemCount = wordList.size + (if (hasAddButton) 1 else 0)
                    val maxPage = if (totalItemCount == 0) 0 else (totalItemCount - 1) / pageSize

                    // 페이지 보정
                    val safeCurrentPage = currentPage.coerceIn(0, maxPage)
                    if (safeCurrentPage != currentPage && currentPage != Int.MAX_VALUE) {
                        viewModel.setWordPageIndex(safeCurrentPage)
                    }

                    val startIndex = safeCurrentPage * pageSize
                    val endIndex = minOf(startIndex + pageSize, totalItemCount)
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
                            if (totalItemCount == 0 && !hasAddButton) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("등록된 낱말 카드가 없습니다.", color = Color.Gray, fontSize = 20.sp)
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columnCount),
                                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                                    verticalArrangement = Arrangement.spacedBy(commonSpacing),
                                    modifier = Modifier.fillMaxSize(),
                                    userScrollEnabled = false
                                ) {
                                    items(currentItemCount) { index ->
                                        val absoluteIndex = startIndex + index

                                        if (hasAddButton) {
                                            // 🟢 [일반 카테고리] : 추가 버튼 있음 + 롱클릭 가능
                                            if (absoluteIndex == 0) {
                                                DashedAddCardItem(
                                                    modifier = Modifier.aspectRatio(1f),
                                                    onClick = { viewModel.openAddWordDialog() }
                                                )
                                            } else {
                                                val wordDataIndex = absoluteIndex - 1
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
                                                                // ✅ 일반 카테고리는 모달 띄우기
                                                                onLongClick = { selectedDetailCard = wordItem }
                                                            ),
                                                        cornerRadius = cardCornerRadius
                                                    )
                                                }
                                            }
                                        } else {
                                            // 🔴 [최근/즐겨찾기] : 추가 버튼 없음 + 롱클릭 비활성화
                                            val wordDataIndex = absoluteIndex
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
                                                            // ✅ 롱클릭 막음 (빈 함수) -> 모달 안 뜸
                                                            onLongClick = {
                                                                if (currentCategoryName != "최근사용")
                                                                    selectedDetailCard = wordItem
                                                            }
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
                            onUpClick = { viewModel.setWordPageIndex(safeCurrentPage - 1) },
                            onDownClick = { viewModel.setWordPageIndex(safeCurrentPage + 1) },
                            canScrollUp = safeCurrentPage > 0,
                            canScrollDown = safeCurrentPage < maxPage,
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "어미\n없음",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                IconButton(onClick = { viewModel.selectCategory(viewModel.selectedCategoryIndex.value) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_refresh),
                                        contentDescription = "새로고침",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
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
                                            // ✅ [기능 3] 어미 카드도 꾹 누르면 상세 모달 표시
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

    // 1. 상세(Detail) 다이얼로그 (꾹 눌렀을 때)
    if (selectedDetailCard != null && !showEditDialog) {
        // 🟢 AI 문장인지 판별하는 로직 (프로젝트 데이터 구조에 맞게 수정하세요)
        // 예: categoryId가 비어있거나 특정 ID인 경우 AI 문장으로 간주
        val isAiCard = selectedDetailCard!!.partOfSpeech == "AI_SENTENCE"

        FlashcardDetailDialog(
            card = selectedDetailCard!!,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
            isAiSentence = isAiCard, // 🟢 1. 이 파라미터를 추가해서 넘겨줍니다!
            onDismiss = { selectedDetailCard = null },
            onDelete = { wordItem ->
                viewModel.deleteWord(wordItem)
                selectedDetailCard = null
            },
            onFavorite = { card, _ ->
                viewModel.toggleFavorite(card)
            },
            onPlay = { card ->
                viewModel.playSingleWord(context, card.word)
            },
            onEdit = {
                showEditDialog = true
            }
        )
    }

    // 2. 수정(Edit) 다이얼로그
    if (selectedDetailCard != null && showEditDialog) {
        // "최근사용", "즐겨찾기" 등을 제외한 순수 카테고리 리스트 생성
        val realCategories = categoryList
            .filter { it.name !in listOf("최근 사용", "최근사용", "즐겨찾기") }
            .map {
                com.example.aac.domain.model.Category(
                    id = it.serverId ?: "",
                    name = it.name,
                    iconKey = it.iconKey ?: "",
                    iconUrl = it.iconUrl, // 🟢 [해결] 이 부분이 누락되면 에러가 납니다
                    displayOrder = 0,
                    wordCount = 0
                )
            }

        FlashcardEditDialog(
            card = selectedDetailCard!!,
            allCategories = realCategories,
            onDismiss = {
                showEditDialog = false
                selectedDetailCard = null
            },
            onSave = { newWord, newCategoryId, newUri, newBitmap ->
                // 🟢 뷰모델의 파라미터 순서와 맞춰서 호출
                viewModel.updateWord(
                    originalCard = selectedDetailCard!!,
                    newWord = newWord,
                    newCategoryId = newCategoryId, // 👈 여기서 ID를 바로 넘깁니다.
                    newImageUrl = newUri?.toString()
                )
                showEditDialog = false
                selectedDetailCard = null
            }
        )
    }

}