package com.example.aac.ui.features.auto_sentence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aac.R

@Composable
fun AutoSentenceDeleteConfirmDialog(
    message: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {

        Column(
            modifier = Modifier
                .width(451.dp)
                .background(
                    color = Color(0xFFF3F4F7),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(
                    start = 51.dp,
                    end = 51.dp,
                    top = 64.dp,
                    bottom = 48.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- 메시지 ---------- */
            Text(
                text = message,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(61.dp))

            /* ---------- 버튼 영역 ---------- */
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 취소
                Box(
                    modifier = Modifier
                        .width(178.dp)
                        .height(60.dp)
                        .background(
                            color = Color(0xFFE2E5EA),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFFD9D9D9),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }

                // 삭제하기
                Box(
                    modifier = Modifier
                        .width(178.dp)
                        .height(60.dp)
                        .background(
                            color = Color(0xFFE2E5EA),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            Color(0xFFD9D9D9),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash2),
                            contentDescription = null,
                            tint = Color(0xFFCC3333),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "삭제하기",
                            fontSize = 20.sp,
                            color = Color(0xFFCC3333)
                        )
                    }
                }
            }
        }
    }
}

