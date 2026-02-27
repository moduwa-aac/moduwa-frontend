package com.example.aac.ui.features.auto_sentence.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.data.remote.dto.RoutineDto

@Composable
fun RoutineModal(
    routine: RoutineDto,
    onSnoozeClick: () -> Unit,
    onDismissClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xC7191919)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(530.dp)
                .height(457.dp)
                .background(
                    color = Color(0xFFF3F4F7),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 51.dp, vertical = 40.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 벨 아이콘 원
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color(0xFF99C1FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(70.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = routine.message,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formatToKoreanAmPm(routine.scheduledTime),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 재생 버튼
                Box(
                    modifier = Modifier
                        .width(428.dp)
                        .height(60.dp)
                        .background(Color(0xFF0088FF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                        .clickable { onPlayClick() }, // TTS 추가
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sound),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "재생",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // 🔘 하단 두 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    // 5분 뒤 다시 알림
                    Box(
                        modifier = Modifier
                            .width(206.5.dp)
                            .height(60.dp)
                            .background(Color(0xFFE2E5EA), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                            .clickable { onSnoozeClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "5분 뒤 다시 알림",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    // 끄기
                    Box(
                        modifier = Modifier
                            .width(206.5.dp)
                            .height(60.dp)
                            .background(Color(0xFFE2E5EA), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                            .clickable { onDismissClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "끄기",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB80000)
                        )
                    }
                }
            }
        }
    }
}

private fun formatToKoreanAmPm(time: String): String {
    return try {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1]

        val amPm = if (hour < 12) "오전" else "오후"

        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        "$amPm $displayHour:$minute"
    } catch (e: Exception) {
        time
    }
}

@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun RoutineModalPreview() {
    RoutineModal(
        routine = RoutineDto(
            id = "1",
            message = "물을 끓여와주세요.",
            repeatType = "WEEKLY",
            daysOfWeek = listOf(2, 4, 6),
            daysOfMonth = listOf(),
            isMonthEnd = false,
            scheduledTime = "08:30",
            isActive = true,
            snoozedUntil = null,
            dismissedUntil = null,
            createdAt = "",
            updatedAt = ""
        ),
        onSnoozeClick = {},
        onDismissClick = {},
        onPlayClick = {}
    )
}

