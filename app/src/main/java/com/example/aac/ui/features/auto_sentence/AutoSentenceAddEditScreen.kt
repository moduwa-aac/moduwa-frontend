package com.example.aac.ui.features.auto_sentence

import AutoSentenceInputField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.components.CustomTopBar
import com.example.aac.ui.features.auto_sentence.repeat.*
import com.example.aac.ui.features.auto_sentence.time.TimePickerBottomSheet
import com.example.aac.ui.features.auto_sentence.time.TimeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSentenceAddEditScreen(
    mode: AutoSentenceMode,
    initialItem: AutoSentenceItem? = null,
    onBack: () -> Unit,
    onSave: (AutoSentenceItem) -> Unit,
    onDelete: (() -> Unit)? = null,
    routineViewModel: AutoSentenceRoutineViewModel,
    voiceKey: String? = null
) {
    /* ---------- 초기 상태 ---------- */
    val context = LocalContext.current

    var sentence by rememberSaveable { mutableStateOf(initialItem?.sentence ?: "") }

    var repeatSetting by remember {
        mutableStateOf(
            initialItem?.repeatSetting ?: RepeatSetting(
                type = RepeatType.WEEKLY,
                days = setOf(Weekday.TUE, Weekday.WED, Weekday.THU)
            )
        )
    }

    var timeState by remember {
        mutableStateOf(
            initialItem?.timeState ?: TimeState(
                isAm = true,
                hour = 9,
                minute = 0
            )
        )
    }

    var isRepeatSelected by remember { mutableStateOf(initialItem != null) }
    var isTimeSelected by remember { mutableStateOf(initialItem != null) }

    /* ---------- BottomSheet / Dialog 상태 ---------- */
    var showRepeatSheet by remember { mutableStateOf(false) }
    var showTimeSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    /* ---------- 반복 주기 텍스트 ---------- */
    val repeatText = remember(repeatSetting, isRepeatSelected) {
        if (!isRepeatSelected) "선택" else repeatSetting.toKoreanText()
    }

    /* ---------- 시간 텍스트 ---------- */
    val timeText = remember(timeState, isTimeSelected) {
        if (!isTimeSelected) {
            "선택"
        } else {
            val amPm = if (timeState.isAm) "오전" else "오후"
            val minuteText = timeState.minute.toString().padStart(2, '0')
            "$amPm ${timeState.hour}:$minuteText"
        }
    }

    /* ---------- 타이틀 ---------- */
    val titleText = if (mode == AutoSentenceMode.ADD) "문장 추가" else "문장 편집"

    val isFormValid = sentence.isNotBlank() && isRepeatSelected && isTimeSelected
    val saveButtonColor = if (isFormValid) Color(0xFF1C63A8) else Color(0xFFB0B0B0)

    /* ---------- UI ---------- */
    Scaffold(
        containerColor = Color(0xFFF2F2F2),
        topBar = {
            CustomTopBar(
                title = titleText,
                onBackClick = onBack,
                actionText = "저장하기",
                actionColor = saveButtonColor,
                onActionClick = {
                    if (isFormValid) {
                        val item = AutoSentenceItem(
                            id = initialItem?.id ?: System.currentTimeMillis(),
                            serverId = initialItem?.serverId ?: "",   // ADD면 아직 서버 id 없으니 빈 값
                            sentence = sentence,
                            repeatSetting = repeatSetting,
                            timeState = timeState
                        )
                        onSave(item)
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AutoSentenceInputField(
                value = sentence,
                onValueChange = { sentence = it }
            )

            // ----------------------------------------------------
            // (임시 테스트용) 미리듣기 버튼: 현재 입력한 sentence를 서버 TTS로 재생
            // ----------------------------------------------------
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF0088FF), RoundedCornerShape(8.dp))
                    .clickable {
                        routineViewModel.playRoutineTts(
                            context = context,
                            text = sentence,
                            voiceKey = voiceKey
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "미리듣기",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AutoSentenceOptionCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_recycle,
                    title = "반복 주기",
                    value = repeatText,
                    onClick = { showRepeatSheet = true }
                )

                AutoSentenceOptionCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_time,
                    title = "시간",
                    value = timeText,
                    onClick = { showTimeSheet = true }
                )
            }

            /* ---------- 삭제 버튼 (EDIT 모드에서만) ---------- */
            if (mode == AutoSentenceMode.EDIT && onDelete != null) {
                Spacer(modifier = Modifier.height(32.dp))
                DeleteButton(onClick = { showDeleteConfirmDialog = true })
            }
        }

        /* ---------- Repeat BottomSheet ---------- */
        // 여기 수정: 바깥 터치 dismiss 시에도 안전하게 닫히도록
        if (showRepeatSheet) {
            RepeatCycleBottomSheet(
                onDismiss = { showRepeatSheet = false },
                onComplete = { newSetting ->
                    repeatSetting = newSetting
                    isRepeatSelected = true
                    showRepeatSheet = false
                }
            )
        }

        /* ---------- Time BottomSheet ---------- */
        if (showTimeSheet) {
            TimePickerBottomSheet(
                onDismiss = { showTimeSheet = false },
                onConfirm = { newTime ->
                    timeState = newTime
                    isTimeSelected = true
                    showTimeSheet = false
                }
            )
        }

        /* ---------- 삭제 확인 Dialog ---------- */
        if (showDeleteConfirmDialog) {
            CommonDeleteDialog(
                message = "문장을\n삭제 하시겠어요?",
                onDismiss = { showDeleteConfirmDialog = false },
                onDelete = {
                    showDeleteConfirmDialog = false
                    onDelete?.invoke()
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun AutoSentenceAddEditScreenPreview() {
    // ⚠️ Preview에서는 ViewModel/실제 Context 기반 네트워크/MediaPlayer 동작이 불가하니
    //    임시 더미 ViewModel을 만들어 호출해야 함.
    //    (현재 프로젝트 구조상 Preview는 생략하거나, 파라미터 없는 별도 Preview용 Composable을 만드는 걸 추천)
}
