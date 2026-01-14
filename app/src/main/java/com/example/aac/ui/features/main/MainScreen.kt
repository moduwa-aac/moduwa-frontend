package com.example.aac.ui.features.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R

val BluePrimary = Color(0xFF4A90E2)
val TopBarGray = Color(0xFFF2F2F2)
val SideBarGray = Color(0xFF666666)
val CategorySelectedGray = Color(0xFFCCCCCC)
val CategoryUnselectedGray = Color(0xFFE0E0E0)

@Composable
fun MainScreen() {
    Row(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            TopSection()

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryRow(modifier = Modifier.weight(1f))
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    Image(painter = painterResource(R.drawable.ic_setting), contentDescription = "설정")
                }
            }

            // Main content row: CardsArea | CardScrollBar (up/down) | Reaction buttons (긍정..청유)
            Row(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                CardsArea(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                CardScrollBar()

                // Reaction buttons column placed in the same horizontal row (previously RightSideBar)
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFEEEEEE))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Keep buttons grouped vertically; positioned inside same Row horizontally
                    Spacer(modifier = Modifier.weight(1f))

                    SmallReactionButton(iconRes = R.drawable.ic_positive, text = "긍정")
                    SmallReactionButton(iconRes = R.drawable.ic_negative, text = "부정")
                    SmallReactionButton(iconRes = R.drawable.ic_question, text = "질문")
                    SmallReactionButton(iconRes = R.drawable.ic_request, text = "부탁")
                    SmallReactionButton(iconRes = R.drawable.ic_suggestion, text = "청유")
                }
            }
        }

        // Removed separate RightSideBar() call — reactions moved into the main row
    }
}

@Composable
fun TopSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(TopBarGray)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selected cards area (fills remaining space)
        CardsSelectedArea(modifier = Modifier.weight(1f))

        // 문장완성 버튼 (RoundedIconButton moved to ButtonComponents.kt)
        RoundedIconButton(iconRes = R.drawable.btn_ai, text = "문장완성", containerColor = BluePrimary, contentColor = Color.White)

        // 재생 버튼
        RoundedIconButton(iconRes = R.drawable.btn_play, text = "재생", containerColor = BluePrimary, contentColor = Color.White)
    }
}

// New small composable to show chosen/selected cards at the top (horizontal list placeholder)
@Composable
fun CardsSelectedArea(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxHeight()
            .horizontalScroll(scrollState)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // simple selected-card placeholders
        for (i in 1..5) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 72.dp)
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "선택 카드 $i", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CategoryRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(68.dp)
            .background(BluePrimary)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 왼쪽 화살표
        IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
            Image(painter = painterResource(R.drawable.btn_prev), contentDescription = "이전")
        }

        val categories = listOf(
            Triple(R.drawable.ic_recent_use, "최근사용", true),
            Triple(R.drawable.ic_favorite, "즐겨찾기", false),
            Triple(R.drawable.ic_default, "기본", false),
            Triple(R.drawable.ic_human, "사람", false),
            Triple(R.drawable.ic_act, "행동", false),
            Triple(R.drawable.ic_emotion, "감정", false),
            Triple(R.drawable.ic_food, "음식", false),
            Triple(R.drawable.ic_place, "장소", false),
        )

        categories.forEach { (icon, name, isSelected) ->
            CategoryTab(iconRes = icon, text = name, isSelected = isSelected, modifier = Modifier.weight(1f))
        }


        IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
            Image(painter = painterResource(R.drawable.btn_next), contentDescription = "다음")
        }
    }
}
