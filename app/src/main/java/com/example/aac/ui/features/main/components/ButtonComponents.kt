package com.example.aac.ui.features.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// RoundedIconButton: TopSection 버튼
@Composable
fun RoundedIconButton(
    iconRes: Int,
    text: String,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(painter = painterResource(iconRes), contentDescription = text, modifier = Modifier.size(32.dp))
        Text(text, fontSize = 12.sp, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

// CategoryTab: 카테고리 탭
@Composable
fun CategoryTab(
    iconRes: Int,
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) BluePrimary else Color.White

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(iconRes), contentDescription = text, modifier = Modifier.size(32.dp))
        Text(text, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
    }
}

// SmallRoundedIconButton: 사이드 설정 버튼 등
@Composable
fun SmallRoundedIconButton(
    iconRes: Int,
    text: String,
    containerColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(containerColor, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Image(painter = painterResource(iconRes), contentDescription = text, modifier = Modifier.size(24.dp))
        Text(text, fontSize = 9.sp, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SmallReactionButton(iconRes: Int, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SideBarGray,
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Image(painter = painterResource(iconRes), contentDescription = text, modifier = Modifier.size(28.dp))
        Text(text, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

