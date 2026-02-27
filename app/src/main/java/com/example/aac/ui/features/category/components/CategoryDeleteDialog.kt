package com.example.aac.ui.features.category.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
fun CategoryDeleteDialog(
    onDismissRequest: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F7)),
            modifier = Modifier
                .width(451.dp)
                .height(317.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 40.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "카테고리를\n삭제 하시겠어요?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 46.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    // 취소 버튼
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E5EA)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD9D9D9)),
                        modifier = Modifier
                            .width(178.dp)
                            .height(60.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("취소", color = Color.Black, fontSize = 20.sp)
                    }

                    // 삭제하기 버튼
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E5EA)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD9D9D9)),
                        modifier = Modifier
                            .width(178.dp)
                            .height(60.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash2),
                            contentDescription = null,
                            tint = Color(0xFFCC3333),
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "삭제하기",
                            color = Color(0xFFCC3333),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}