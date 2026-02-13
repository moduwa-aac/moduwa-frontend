package com.example.aac.ui.features.category.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R

@Composable
fun AddWordCardDialog(
    onDismissRequest: () -> Unit,
    onSaveClick: (String, String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val mainBlue = Color(0xFF0088FF)
    val lightBlueBg = Color(0xFFF3F4F7)
    val lightGrayBorder = Color(0xFFE0E0E0)
    val buttonGray = Color(0xFFE2E5EA)
    val textGray = Color(0xFF888888)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(530.dp)
                .height(613.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF3F4F7)
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 48.dp,
                    bottom = 48.dp,
                    start = 51.dp,
                    end = 51.dp
                ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 1. 타이틀
                Text(
                    text = "낱말 카드 추가",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // 2. 낱말 입력 영역
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "낱말",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .background(Color.White)
                                    .border(1.dp, lightGrayBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (text.isEmpty()) {
                                    Text("낱말을 입력하세요", color = textGray, fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // 3. 사진 업로드 영역
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "낱말 사진",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .clip(RoundedCornerShape(11.22.dp))
                                .background(Color(0xFFD7E6F9))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(5.dp.toPx(), 5.dp.toPx()),
                                        0f
                                    )
                                )
                                drawRoundRect(
                                    color = mainBlue,
                                    style = stroke,
                                    cornerRadius = CornerRadius(11.22.dp.toPx())
                                )
                            }

                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(selectedImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_upload),
                                    contentDescription = "Upload Icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = "사진 업로드",
                            color = Color(0xFF1C63A8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 4. 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text("취소", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSaveClick(text, selectedImageUri?.toString()) },
                        colors = ButtonDefaults.buttonColors(containerColor = mainBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("저장", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}