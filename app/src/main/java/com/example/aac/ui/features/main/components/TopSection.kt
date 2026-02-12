package com.example.aac.ui.features.main.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.ui.components.WordCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopSection(
    selectedCards: List<MainWordItem>,
    onRemoveCard: (Int) -> Unit,
    onClearAll: () -> Unit,
    onNavigateToAiSentence: () -> Unit = {}
) {
    var isMultiLine by remember { mutableStateOf(false) }

    var deleteTargetIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(selectedCards) {
        deleteTargetIndex = -1
    }

    val density = LocalDensity.current
    val singleLineHeightThreshold = 100.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xFFD7E6F9))
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 112.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "낱말 카드를 선택하세요.",
                        fontSize = 20.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            val contentHeight = with(density) { coordinates.size.height.toDp() }
                            isMultiLine = contentHeight > singleLineHeightThreshold
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedCards.forEachIndexed { index, card ->
                        val isDeleteMode = (deleteTargetIndex == index)

                        Box(contentAlignment = Alignment.Center) {
                            WordCard(
                                text = card.word,
                                imageUrl = card.imageUrl,
                                partOfSpeech = card.partOfSpeech,
                                modifier = Modifier.size(86.dp),
                                cornerRadius = 8.dp,
                                fontSize = 14.sp,
                                iconSize = 40.dp,
                                borderColor = if (isDeleteMode) Color.Red else null,
                                onClick = {
                                    if (isDeleteMode) {
                                        onRemoveCard(index)
                                        deleteTargetIndex = -1 // 상태 초기화
                                    } else {
                                        deleteTargetIndex = index
                                    }
                                }
                            )

                            if (isDeleteMode) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "삭제 대기",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCards.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        onClearAll()
                        deleteTargetIndex = -1
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mainx),
                        contentDescription = "전체 삭제",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        val aiButtonEnabled = !isMultiLine && selectedCards.isNotEmpty()

        Surface(
            modifier = Modifier.size(92.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp),
            onClick = { if (aiButtonEnabled) onNavigateToAiSentence() },
            shadowElevation = if (aiButtonEnabled) 2.dp else 0.dp,
            enabled = aiButtonEnabled
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (aiButtonEnabled) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0080FF), Color(0xFFE8C2EC)),
                                start = Offset(0f, Float.POSITIVE_INFINITY),
                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                            )
                        } else {
                            androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))
                        }
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_ai),
                    contentDescription = "AI문장완성",
                    modifier = Modifier.size(36.dp),
                    alpha = if (aiButtonEnabled) 1f else 0.4f
                )
                Text(
                    text = "문장완성",
                    fontSize = 16.sp,
                    color = if (aiButtonEnabled) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            modifier = Modifier.size(92.dp),
            color = Color(0xFF5E9FFF),
            shape = RoundedCornerShape(12.dp),
            onClick = { /* 재생 로직 */ },
            shadowElevation = 2.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = "재생",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Text("재생", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}