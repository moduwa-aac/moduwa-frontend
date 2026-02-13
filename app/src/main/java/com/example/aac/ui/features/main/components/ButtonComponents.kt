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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    categories: List<CategoryItem>,
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
            CategoryTabItem(
                item = item,
                onClick = { onCategoryClick(index) }, // 0~7 사이의 인덱스를 넘김
                modifier = Modifier.weight(1f)
            )

            val isNextSelected = (index + 1 < categories.size) && categories[index + 1].isSelected
            if (index < categories.lastIndex && !item.isSelected && !isNextSelected) {
                VerticalDivider(
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
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
private fun CategoryTabItem(
    item: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (item.isSelected) CustomBlue else Color.White
    val textColor = if(item.isSelected) Color.White else TextBlack

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.name,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
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