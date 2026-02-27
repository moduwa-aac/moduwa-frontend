package com.example.aac.ui.features.auto_sentence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.data.remote.dto.RoutineDto
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSentenceSettingScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (AutoSentenceItem) -> Unit,
    onSelectDeleteClick: () -> Unit,

    routineViewModel: AutoSentenceRoutineViewModel,

    routineToItem: (RoutineDto) -> AutoSentenceItem,

    // TTS 목소리 키(선택): AppNavGraph에서 voiceSettingId 넘겨주면 됨
    voiceKey: String? = null
) {

    val context = LocalContext.current

    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val uiState by routineViewModel.uiState.collectAsState()

    /* ---------- 화면 진입 시 루틴 조회 ---------- */
    LaunchedEffect(Unit) {
        if (uiState.routines.isEmpty()) {
            routineViewModel.fetchRoutines()
        }
    }

    /* ---------- 서버 루틴 → UI 모델 변환 ---------- */
    val autoSentenceList = remember(uiState.routines) {
        uiState.routines.map(routineToItem)
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F2),
        topBar = {
            CustomTopBar(
                title = "자동 출력 문장 설정",
                onBackClick = onBack,
                actionText = "더보기",
                actionColor = Color.Black,
                onActionClick = { showMoreMenu = !showMoreMenu }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                AutoSentenceAddButton(onClick = onAddClick)

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- 로딩 ---------- */
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                /* ---------- 에러 ---------- */
                else if (!uiState.errorMessage.isNullOrBlank()) {
                    Text(
                        text = uiState.errorMessage ?: "오류가 발생했습니다.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                /* ---------- 데이터 없음 ---------- */
                else if (autoSentenceList.isEmpty()) {
                    Text(
                        text = "등록된 문장이 없습니다.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                /* ---------- 데이터 표시 ---------- */
                else {
                    autoSentenceList.forEach { item ->
                        AutoSentenceItemCard(
                            item = item,

                            // 카드 내 "재생" 버튼 → 서버 MP3 TTS 재생
                            onSoundClick = { clicked ->
                                routineViewModel.playRoutineTts(
                                    context = context,
                                    text = clicked.sentence,
                                    voiceKey = voiceKey
                                )
                            },

                            // 기존: 카드 클릭(편집 이동)
                            onItemClick = { onEditClick(item) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            /* ---------- 더보기 메뉴 ---------- */
            if (showMoreMenu) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showMoreMenu = false }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 24.dp)
                        .width(137.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(12.dp))
                ) {

                    /* ---------- 선택 삭제 ---------- */
                    MoreMenuItem("선택 삭제") {
                        showMoreMenu = false
                        onSelectDeleteClick()
                    }

                    /* ---------- 전체 삭제 ---------- */
                    MoreMenuItem("전체 삭제") {
                        showMoreMenu = false
                        showDeleteAllDialog = true
                    }
                }
            }
        }
    }

    /* ---------- 전체 삭제 확인 모달 ---------- */
    if (showDeleteAllDialog) {
        CommonDeleteDialog(
            message = "자동 출력 문장을\n모두 삭제 하시겠어요?",
            onDismiss = { showDeleteAllDialog = false },
            onDelete = {
                routineViewModel.deleteAllRoutines(
                    onSuccess = {
                        // fetchRoutines() 내부에서 자동 갱신
                    }
                )
                showDeleteAllDialog = false
            }
        )
    }
}

@Composable
fun MoreMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
