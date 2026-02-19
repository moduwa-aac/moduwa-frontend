package com.example.aac.ui.features.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aac.R
import com.example.aac.ui.components.CategoryItem

val CustomBlue = Color(0xFF267FD6) // 배경색
val TextBlack = Color.Black        // 텍스트 색상

// 데이터 모델
data class CategoryItem(
    val name: String,
    val iconRes: Int, // R.drawable.xxx
    val isSelected: Boolean
)

@Composable
fun CategoryBar(
    categories: List<CategoryItem?>, // null이 섞인 8개짜리 리스트
    onCategoryClick: (Int) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(1137.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        NavigationBox(
            iconRes = R.drawable.btn_prev,
            description = "이전",
            onClick = onPrevClick
        )

        categories.forEachIndexed { index, item ->
            if (item != null) {
                // 1. 데이터가 있는 칸: 정상 작동
                CategoryTabItem(
                    item = item,
                    onClick = { onCategoryClick(index) },
                    modifier = Modifier.weight(1f)
                )

                // 구분선 로직 (다음 칸이 null이 아니고, 둘 다 선택 안 됐을 때만)
                val nextItem = categories.getOrNull(index + 1)
                val isNextSelected = nextItem?.isSelected ?: false

                if (index < categories.lastIndex && nextItem != null && !item.isSelected && !isNextSelected) {
                    VerticalDivider(
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                }
            } else {
                // 2. 데이터가 없는 칸: 클릭 안 되는 깡통 Box
                Box(modifier = Modifier.weight(1f))

                // 마지막 칸이 아니면 빈 칸 사이에도 구분선은 넣어줄지 결정 (보통은 안 넣는 게 깔끔함)
                if (index < categories.lastIndex && categories[index + 1] != null) {
                    // 다음 칸에 데이터가 있으면 구분선 하나 넣어줌
                    VerticalDivider(
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.fillMaxHeight().width(1.dp)
                    )
                }
            }
        }

        NavigationBox(
            iconRes = R.drawable.btn_next,
            description = "다음",
            onClick = onNextClick
        )
    }
}

@Composable
fun CategoryTabItem(
    item: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .background(if (item.isSelected) Color(0xFFE3F2FD) else Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // iconUrl이 있으면 서버 이미지를, 없으면 로컬 리소스를 표시
        if (!item.iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.iconUrl,
                contentDescription = item.name,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit,
                // 실패 시 기본 아이콘 표시
                error = painterResource(id = item.iconRes),
                placeholder = painterResource(id = item.iconRes)
            )
        } else {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.name,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.name,
            fontSize = 14.sp,
            color = if (item.isSelected) Color(0xFF0088FF) else Color.Black,
            fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun NavigationBox(
    iconRes: Int,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .background(Color(0xFF66B3FF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}