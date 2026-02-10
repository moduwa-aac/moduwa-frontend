package com.example.aac.ui.features.usage_history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.domain.model.UsageHistory // ✅ 여기가 중요! (새 모델 import)

@Composable
fun UsageHistoryItem(
    record: UsageHistory, // ✅ 타입 변경 (UsageRecord -> UsageHistory)
    isFirstItem: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val commonTextStyle = TextStyle(
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )

    // ✅ 첫 번째 아이템일 때만 상단 둥근 모서리(12dp), 나머지는 직각
    val itemShape = if (isFirstItem) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    } else {
        RectangleShape
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSelectionMode) { onSelectionClick() }
    ) {
        // ✅ 1. 문장 박스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 좌우 패딩 81.dp -> 전체 너비 1280에서 1118이 됨
                .padding(horizontal = 81.dp)
                // 👇 [수정] 높이 79dp 적용
                .height(79.dp)
                // 👇 [수정] 모양 자르기 (Rounded or Rect) -> 배경색 칠하기 전에 해야 함
                .clip(itemShape)
                .background(Color.White)
                // 👇 [수정] 내부 패딩 (상하 10dp, 좌우 19dp) 적용
                .padding(top = 10.dp, bottom = 10.dp, start = 19.dp, end = 19.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 텍스트 영역
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // ✅ 수정됨: record.text -> record.sentence
                Text(
                    text = record.sentence,
                    style = commonTextStyle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp,
                        color = Color.Black
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                // ✅ 수정됨: record.timestamp -> record.data
                Text(
                    text = record.date,
                    style = commonTextStyle.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        color = Color.Gray
                    )
                )
            }

            // 우측 재생 버튼
            if (!isSelectionMode) {
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(11.6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3199FF)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .width(58.dp)
                        .height(58.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "재생",
                            tint = Color.White,
                            modifier = Modifier
                                .width(22.dp)
                                .height(20.22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.32.dp))
                        Text(
                            text = "재생",
                            style = commonTextStyle.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 15.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }

        // ✅ 2. 체크박스
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    // 체크박스 위치 조절 (왼쪽 여백 내에서 배치)
                    .padding(start = 20.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF0088FF) else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                        shape = CircleShape
                    )
                    .clickable { onSelectionClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 구분선 (border-bottom-width: 1px)
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 81.dp), // 문장 박스 너비와 맞춤
            thickness = 1.dp,
            color = Color(0xFFEEEEEE)
        )
    }
}