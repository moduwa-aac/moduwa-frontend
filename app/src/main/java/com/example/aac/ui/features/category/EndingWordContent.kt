package com.example.aac.ui.features.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R

@Composable
fun EndingWordContent(
    onAddClick: () -> Unit
) {
    // 🔥 TipBox 제거함 (부모 파일로 이동)
    // 전체를 꽉 채우는 흰색 박스
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() // 부모에서 남은 공간을 다 채우도록
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 점선 추가 버튼
            DashedAddCardItem(onClick = onAddClick)

            // 2. 고정된 카드들
            ActionCardItem("하고싶어요", R.drawable.ic_human, Color(0xFFE8F5E9), Color(0xFF2E7D32))
            ActionCardItem("하기싫어요", R.drawable.ic_emotion, Color(0xFFE8F5E9), Color(0xFF2E7D32))
            ActionCardItem("질문", R.drawable.ic_book, Color(0xFFE3F2FD), Color(0xFF1565C0))
            ActionCardItem("해주세요", R.drawable.ic_hand, Color(0xFFFFF9C4), Color(0xFFF9A825))
        }
    }
}

// ==========================================
// 👇 하위 컴포넌트들
// ==========================================

@Composable
fun DashedAddCardItem(onClick: () -> Unit) {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
    )
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF66B3FF),
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF267FD6), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("낱말카드 추가", color = Color(0xFF267FD6), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionCardItem(text: String, iconRes: Int, backgroundColor: Color, iconTint: Color) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = Color.Black.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}