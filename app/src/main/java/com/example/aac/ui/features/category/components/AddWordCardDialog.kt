package com.example.aac.ui.features.category.components

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.aac.R
import com.example.aac.ui.components.showCleanToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordCardDialog(
    onDismissRequest: () -> Unit,
    onSaveClick: (String, Uri?, Bitmap?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showPhotoSheet by remember { mutableStateOf(false) }

    // 선택된 이미지 상태 관리
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val photoSheetState = rememberModalBottomSheetState()

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedBitmap = null
        }
    }

    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedBitmap = it
            selectedImageUri = null
        }
    }

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
                .height(620.dp),
            shape = RoundedCornerShape(32.dp),
            color = lightBlueBg
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 40.dp,
                    bottom = 40.dp,
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
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
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
                        textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(Color.White)
                                    .border(1.dp, lightGrayBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (text.isEmpty()) {
                                    Text("낱말을 입력하세요", color = textGray, fontSize = 18.sp)
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // 3. 사진 업로드 영역
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "낱말 사진",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    val stroke = with(density) {
                        Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .clip(RoundedCornerShape(11.22.dp))
                            .background(Color(0xFFD7E6F9))
                            .drawBehind {
                                drawRoundRect(
                                    color = mainBlue,
                                    style = stroke,
                                    cornerRadius = CornerRadius(11.22.dp.toPx())
                                )
                            }
                            .clickable { showPhotoSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedBitmap != null -> {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Selected",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(11.22.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(11.22.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_upload),
                                    contentDescription = "Upload Icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "사진 업로드",
                        color = mainBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("취소", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (text.isBlank()) {
                                showCleanToast(context, "낱말을 입력해주세요.")
                            } else {
                                onSaveClick(text, selectedImageUri, selectedBitmap)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = mainBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("저장", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, sheetState = photoSheetState, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                Text("사진 업로드", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                PhotoOptionItem(Icons.Default.PhotoLibrary, "사진에서 불러오기") {
                    galleryLauncher.launch("image/*")
                    showPhotoSheet = false
                }
                PhotoOptionItem(Icons.Default.AddAPhoto, "카메라로 촬영하기") {
                    cameraLauncher.launch(null)
                    showPhotoSheet = false
                }
            }
        }
    }
}

@Composable
fun PhotoOptionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(28.dp), Color.Gray)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 18.sp, color = Color.Black)
    }
}