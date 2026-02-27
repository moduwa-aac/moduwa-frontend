package com.example.aac.ui.features.auto_sentence

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R

@Composable
fun CommonTopBar(
    title: String,
    rightText: String,
    onBackClick: () -> Unit,
    onRightClick: () -> Unit,
    rightTextColor: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {

        // 가운데 타이틀
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C2C2C),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // 뒤로가기
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable(onClick = onBackClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_circle),
                contentDescription = "Back",
                modifier = Modifier.size(45.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "뒤로가기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF373737)
            )
        }

        // 우측 액션
        Text(
            text = rightText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = rightTextColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(onClick = onRightClick)
        )
    }
}
