package com.example.aac.ui.features.ai_sentence.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.feature.ai_sentence.ui.components.SentenceCard
import com.example.aac.ui.components.CustomTopBar
import com.example.aac.ui.components.WordCard
// ✅ 드래그 유틸리티 import (패키지명 확인 필요)
import com.example.aac.ui.util.dragAndDropItem
import com.example.aac.ui.util.rememberDragDropState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSentenceScreen(
    initialWords: List<MainWordItem>,
    onBack: () -> Unit,
    onEditNavigate: (String) -> Unit,
    vm: AiSentenceViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    val dragDropState = rememberDragDropState(onMove = { from, to ->
        vm.moveWord(from, to)
    })

    LaunchedEffect(Unit) {
        if (state.selectedWords.isEmpty() && initialWords.isNotEmpty()) {
            vm.setInitialWords(initialWords)
        }
    }

    var deleteTargetIndex by remember { mutableIntStateOf(-1) }

    val skyBlue = Color(0xFF66B2FF)
    val lightGrayBg = Color(0xFFF4F4F4)
    val grayButton = Color(0xFF666666)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isBanmalMode by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "AI 문장 완성",
                onBackClick = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .height(42.dp)
                        .widthIn(min = 232.dp)
                        .background(
                            color = Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(21.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        },
        containerColor = lightGrayBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 상단 라벨 및 스위치
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "선택한 낱말",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "반말",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isBanmalMode,
                        onCheckedChange = { isChecked ->
                            isBanmalMode = isChecked
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val msg = if (isChecked) "반말 모드로 변경했어요." else "존댓말 모드로 변경했어요."
                                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = skyBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // 2. 단어 리스트 컨테이너
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 2-1. 단어들 (드래그 적용)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.selectedWords.forEachIndexed { index, item ->
                            val isDeleteMode = (deleteTargetIndex == index)

                            key(item.cardId) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .dragAndDropItem(index, dragDropState)
                                ) {
                                    WordCard(
                                        text = item.word,
                                        imageUrl = item.imageUrl,
                                        partOfSpeech = item.partOfSpeech,
                                        modifier = Modifier.size(86.dp),
                                        cornerRadius = 12.dp,
                                        fontSize = 14.sp,
                                        iconSize = 40.dp,
                                        borderColor = if (isDeleteMode) Color.Red else null,
                                        onClick = {
                                            if (dragDropState.draggingItemIndex == null) {
                                                if (isDeleteMode) {
                                                    vm.removeWord(index)
                                                    deleteTargetIndex = -1
                                                } else {
                                                    deleteTargetIndex = index
                                                }
                                            }
                                        }
                                    )

                                    if (isDeleteMode) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.ic_delete),
                                            contentDescription = "삭제 대기",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 2-2. 버튼 그룹
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TopSquareButton(
                            text = "새로고침",
                            iconRes = R.drawable.ic_refresh,
                            backgroundColor = grayButton
                        ) {
                            scope.launch {
                                snackbarHostState.showSnackbar("새로고침 중...", duration = SnackbarDuration.Short)
                            }
                            vm.fetchAiSentences(state.selectedWords.map { it.word }, isRefresh = true)
                        }

                        TopSquareButton(
                            text = "재생",
                            iconRes = R.drawable.ic_play,
                            backgroundColor = skyBlue
                        ) {
                            vm.onEvent(AiSentenceUiEvent.ClickPlayTop)
                        }
                    }
                }
            }

            // 3. 문장 리스트
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = skyBlue)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.sentences, key = { it.id }) { item ->
                        SentenceCard(
                            text = item.text,
                            isFavorite = item.isFavorite,
                            onEdit = { onEditNavigate(item.text) },
                            onFavorite = { vm.onEvent(AiSentenceUiEvent.ClickFavorite(item.id)) },
                            onPlay = { vm.onEvent(AiSentenceUiEvent.ClickPlaySentence(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopSquareButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(86.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}