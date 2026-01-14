package com.example.aac.feature.ai_sentence.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.feature.ai_sentence.ui.components.SentenceCard
import com.example.aac.ui.theme.AacTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSentenceScreen(
    onBack: () -> Unit,
    onEditNavigate: (String) -> Unit,
    vm: AiSentenceViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    // 색상 정의
    val skyBlue = Color(0xFF66B2FF)
    val lightGrayBg = Color(0xFFF5F5F5)
    val grayButton = Color(0xFF666666)

    // 상태 관리
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isBanmalMode by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI 문장 완성", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Row(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(skyBlue),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back),
                                    contentDescription = "뒤로가기",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(text = "뒤로가기", color = Color(0xFF333333))
                    }
                }
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 상단 라벨 영역 (선택한 낱말 + 반말 토글)
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
                        onCheckedChange = {
                            isBanmalMode = it
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val msg = if (it) "반말 모드로 변경했어요." else "존댓말 모드로 변경했어요."
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

            // 2. 상단 컨테이너 (낱말 리스트 + 새로고침/재생 버튼)
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
                    // 2-1. 낱말 리스트 (왼쪽)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 낱말 카드 (이제 버튼과 같은 86x86 크기)
                        MockWordItem("밥", Color(0xFFFFE082))
                        MockWordItem("먹다", Color(0xFFA5D6A7))
                        MockWordItem("긍정", Color(0xFF666666), isDark = true)
                    }

                    // 2-2. 버튼 그룹 (오른쪽: 새로고침 + 재생)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 새로고침 버튼 (86x86)
                        TopSquareButton(
                            text = "새로고침",
                            iconRes = R.drawable.ic_refresh,
                            backgroundColor = grayButton
                        ) {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("새로고침 했어요.", duration = SnackbarDuration.Short)
                            }
                            // vm.onEvent(AiSentenceUiEvent.ClickRefresh)
                        }

                        // 상단 재생 버튼 (86x86)
                        TopSquareButton(
                            text = "재생",
                            iconRes = R.drawable.ic_play,
                            backgroundColor = skyBlue
                        ) {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("낱말을 재생했어요.", duration = SnackbarDuration.Short)
                            }
                            vm.onEvent(AiSentenceUiEvent.ClickPlayTop)
                        }
                    }
                }
            }

            // 3. 문장 리스트
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.sentences, key = { it.id }) { item ->
                    SentenceCard(
                        text = item.text,
                        isFavorite = item.isFavorite,
                        onEdit = {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("편집화면으로 넘어갈게요.", duration = SnackbarDuration.Short)
                            }
                            onEditNavigate(item.text)
                        },
                        onFavorite = {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("즐겨찾기에 추가했어요.", duration = SnackbarDuration.Short)
                            }
                            vm.onEvent(AiSentenceUiEvent.ClickFavorite(item.id))
                        },
                        onPlay = {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("문장을 재생했어요.", duration = SnackbarDuration.Short)
                            }
                            vm.onEvent(AiSentenceUiEvent.ClickPlaySentence(item.id))
                        }
                    )
                }
            }
        }
    }
}

// 🟩 상단 낱말 카드 (크기 수정됨: 86x86, 둥글기 12dp)
@Composable
fun MockWordItem(label: String, color: Color, isDark: Boolean = false) {
    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp), // ⭐ 수정됨: 버튼과 동일한 12dp 둥글기
        modifier = Modifier.size(86.dp)    // ⭐ 수정됨: 버튼과 동일한 86x86 크기
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 카드 크기가 커졌으므로 내부 아이콘 영역도 약간 키움 (32dp -> 40dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Spacer(Modifier.height(4.dp)) // 간격 조정
            Text(
                text = label,
                fontSize = 14.sp, // 글자 크기도 약간 키움 (12 -> 14)
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}

// 🟦 상단 네모 버튼 (86x86)
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

@Preview(
    name = "Figma Design Size",
    device = "spec:width=1280dp,height=720dp,dpi=320,orientation=landscape",
    showBackground = true
)
@Composable
fun AiSentencesScreenPreview() {
    AacTheme {
        AiSentenceScreen(
            onBack = {},
            onEditNavigate = {}
        )
    }
}