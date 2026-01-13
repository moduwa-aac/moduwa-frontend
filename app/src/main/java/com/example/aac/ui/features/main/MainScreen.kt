package com.example.aac.ui.features.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    Row(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TopSection()
            CategoryRow()
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFEEEEEE))) {
                Text("카드 영역", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            }
        }
        RightSideBar()
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
        // 2번: 키보드 버튼
        RoundedIconButton(iconRes = R.drawable.ic_keyboard, text = "키보드")

        // 입력창
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("낱말 카드를 선택하거나 키보드로 입력하세요.", color = Color.Gray, fontSize = 14.sp)
        }

        // 5번: 문장완성 버튼
        RoundedIconButton(iconRes = R.drawable.btn_ai, text = "문장완성", containerColor = BluePrimary, contentColor = Color.White)

        // 6번: 재생 버튼
        RoundedIconButton(iconRes = R.drawable.btn_play, text = "재생", containerColor = BluePrimary, contentColor = Color.White)
    }
}

@Composable
fun CategoryRow() {
    Row(
        modifier = Modifier
            .width(1137.dp)
            .height(68.dp)
            .background(Color(0xFF4A90E2))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 3번: 왼쪽 화살표
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

@Composable
fun RightSideBar() {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(Color(0xFF4A90E2))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SmallRoundedIconButton(iconRes = R.drawable.ic_setting, text = "설정", containerColor = Color.White)

        SmallReactionButton(iconRes = R.drawable.ic_good, text = "좋아요")
        SmallReactionButton(iconRes = R.drawable.ic_bad, text = "싫어요")
        SmallReactionButton(iconRes = R.drawable.ic_question, text = "질문")
        SmallReactionButton(iconRes = R.drawable.ic_help, text = "해주세요")
        SmallReactionButton(iconRes = R.drawable.ic_do, text = "합시다")
    }
}


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
            .background(Color(0xFF666666), RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Image(painter = painterResource(iconRes), contentDescription = text, modifier = Modifier.size(28.dp))
        Text(text, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}