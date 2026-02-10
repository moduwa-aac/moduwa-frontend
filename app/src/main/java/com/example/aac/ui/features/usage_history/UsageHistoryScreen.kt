package com.example.aac.ui.features.usage_history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel 추가
import com.example.aac.R
import com.example.aac.domain.model.UsageHistory
import com.example.aac.ui.components.CustomTopBar
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.features.usage_history.components.UsageHistoryDatePicker
import com.example.aac.ui.features.usage_history.components.UsageHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistoryScreen(
    onBackClick: () -> Unit = {},
    // ViewModel 주입 (기본값 설정)
    viewModel: UsageHistoryViewModel = viewModel()
) {
    var selectedYear by remember { mutableIntStateOf(2025) }
    var selectedMonth by remember { mutableIntStateOf(12) }
    var showDatePicker by remember { mutableStateOf(false) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeleteAllMode by remember { mutableStateOf(false) }

    // 1. ViewModel의 데이터를 구독 (서버에서 온 데이터가 여기로 들어옴)
    val historyList by viewModel.histories.collectAsState()

    // 2. 연도나 월이 바뀌면 서버에 데이터 요청 (자동 실행)
    LaunchedEffect(selectedYear, selectedMonth) {
        viewModel.fetchHistories(selectedYear, selectedMonth)
    }

    // 3. ID 타입 변경: 서버 ID는 String(UUID)이므로 Long -> String으로 변경
    val selectedIds: SnapshotStateList<String> = remember { mutableStateListOf() }

    val menuTextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        color = Color.Black
    )

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {

                CustomTopBar(
                    title = "사용 기록 조회",
                    actionText = if (isSelectionMode) "삭제하기" else "더보기",
                    actionColor = if (isSelectionMode) Color(0xFFFF4B4B) else Color.Black,
                    onBackClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedIds.clear()
                        } else {
                            onBackClick()
                        }
                    },
                    onActionClick = {
                        if (isSelectionMode) {
                            if (selectedIds.isNotEmpty()) {
                                isDeleteAllMode = false
                                showDeleteConfirmDialog = true
                            }
                        } else {
                            showMoreMenu = true
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp, top = 72.dp)
                        .size(1.dp)
                ) {
                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            offset = DpOffset(x = 0.dp, y = 0.dp),
                            shape = RoundedCornerShape(12.dp),
                            containerColor = Color.White,
                            tonalElevation = 4.dp,
                            modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.width(137.dp).height(106.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f).clickable {
                                        showMoreMenu = false
                                        isSelectionMode = true
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "선택 삭제", style = menuTextStyle)
                                }
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f).clickable {
                                        showMoreMenu = false
                                        isDeleteAllMode = true
                                        showDeleteConfirmDialog = true
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "전체 삭제", style = menuTextStyle)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 81.dp, top = 20.dp, bottom = 10.dp)
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedYear}년 ${selectedMonth}월",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        )
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_toggle),
                    contentDescription = "날짜 선택",
                    tint = Color(0xFF99C1FF),
                    modifier = Modifier.size(19.dp).offset(y = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(historyList) { index, record ->
                        UsageHistoryItem(
                            record = record, // UsageHistory 객체 전달
                            isFirstItem = (index == 0),
                            isSelectionMode = isSelectionMode,
                            isSelected = record.id in selectedIds,
                            onSelectionClick = {
                                if (record.id in selectedIds) {
                                    selectedIds.remove(record.id)
                                } else {
                                    selectedIds.add(record.id)
                                }
                            },
                            onPlayClick = { /* 재생 로직 */ }
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            UsageHistoryDatePicker(
                currentYear = selectedYear,
                currentMonth = selectedMonth,
                onDismiss = { showDatePicker = false },
                onConfirm = { year, month ->
                    selectedYear = year
                    selectedMonth = month
                    showDatePicker = false
                }
            )
        }

        if (showDeleteConfirmDialog) {
            CommonDeleteDialog(
                message = if (isDeleteAllMode) {
                    "${selectedMonth}월 사용 기록을\n모두 삭제 하시겠어요?"
                } else {
                    "선택한 사용 기록을\n삭제 하시겠어요?"
                },
                onDismiss = { showDeleteConfirmDialog = false },
                onDelete = {
                    if (isDeleteAllMode) {
                        // TODO: ViewModel의 전체 삭제 API 함수 호출
                        // viewModel.deleteAll(selectedYear, selectedMonth)
                    } else {
                        // TODO: ViewModel의 선택 삭제 API 함수 호출
                        // viewModel.deleteItems(selectedIds.toList())
                    }

                    // historyList.clear() -> ⚠️ 불가능: 서버 데이터는 UI에서 직접 지울 수 없음
                    // 일단 선택 상태만 초기화합니다.
                    selectedIds.clear()
                    isSelectionMode = false
                    showDeleteConfirmDialog = false

                    // (임시) 삭제 후 목록 갱신을 위해 다시 불러오기
                    // viewModel.fetchHistories(selectedYear, selectedMonth)
                }
            )
        }
    }
}

@Preview(
    device = "spec:width=1280dp,height=720dp,dpi=160,orientation=landscape",
    showBackground = true
)
@Composable
fun UsageHistoryScreenPreview() {
    // 미리보기에서는 ViewModel 주입이 어려우므로 빈 화면이 나올 수 있습니다.
    // Preview용 Mock 데이터를 만들거나 별도의 PreviewWrapper가 필요합니다.
}