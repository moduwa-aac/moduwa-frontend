package com.example.aac.ui.features.category.components

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aac.R
import com.example.aac.ui.components.showCleanToast
import com.example.aac.ui.features.category.CategoryEditData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditDialog(
    category: CategoryEditData,
    onDismissRequest: () -> Unit,
    onSaveClick: (String, Int, Uri?, Bitmap?) -> Unit
) {
    LaunchedEffect(category.id) {
        Log.d("DIALOG_CHECK", "🆔 ID: ${category.id} | 🛠️ 모드: ${if (category.id != null) "편집" else "추가"}")
    }

    val isEditMode = remember(category.id) { category.id != null }
    var name by remember(category.id) { mutableStateOf(category.title) }
    var selectedIcon by remember(category.id) { mutableIntStateOf(category.iconRes) }
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val photoSheetState = rememberModalBottomSheetState()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            selectedUri = it
            selectedBitmap = null
            selectedIcon = 0 
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { 
            selectedBitmap = it
            selectedUri = null
            selectedIcon = 0
        }
    }

    // 🔥 [수정] 누락되었던 ic_song 아이콘을 다시 리스트에 추가했습니다.
    val icons = listOf(
        R.drawable.ic_human, R.drawable.ic_emotion, R.drawable.ic_act,
        R.drawable.ic_hand, R.drawable.ic_pill, R.drawable.ic_hospital, R.drawable.ic_school,
        R.drawable.ic_place, R.drawable.ic_food, R.drawable.ic_paint,
        R.drawable.ic_soccer, R.drawable.ic_book, R.drawable.ic_song
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F7)),
            modifier = Modifier.width(530.dp).height(540.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 51.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditMode) "카테고리 편집" else "카테고리 추가",
                    fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.width(426.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "카테고리 이름", fontSize = 18.sp, color = Color(0xFF494949), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("예: 병원, 학교, 식당...", color = Color.LightGray, fontSize = 18.sp) },
                        modifier = Modifier.fillMaxWidth().height(65.dp),
                        shape = RoundedCornerShape(5.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.width(426.dp)) {
                    Text(text = "아이콘 선택", fontSize = 18.sp, color = Color(0xFF494949), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(117.42.dp),
                        userScrollEnabled = false
                    ) {
                        items(icons) { iconRes ->
                            IconSelectionItem(
                                iconRes = iconRes,
                                isSelected = selectedIcon == iconRes,
                                onClick = { 
                                    selectedIcon = iconRes 
                                    selectedUri = null
                                    selectedBitmap = null
                                }
                            )
                        }
                        item {
                            UploadButtonItem(
                                isSelected = selectedUri != null || selectedBitmap != null,
                                onClick = { showPhotoSheet = true }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(modifier = Modifier.width(426.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E5EA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(68.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) { Text("취소", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                showCleanToast(context, "카테고리 이름을 입력해주세요.")
                            } else {
                                onSaveClick(name, selectedIcon, selectedUri, selectedBitmap)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(68.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) { Text("저장", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, sheetState = photoSheetState, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                Text("사진 업로드", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                PhotoOptionItemInDialog(Icons.Default.PhotoLibrary, "사진에서 불러오기") { galleryLauncher.launch("image/*"); showPhotoSheet = false }
                PhotoOptionItemInDialog(Icons.Default.AddAPhoto, "카메라로 촬영하기") { cameraLauncher.launch(null); showPhotoSheet = false }
            }
        }
    }
}

@Composable
fun IconSelectionItem(iconRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE3F2FD) else Color.White)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF0088FF) else Color(0xFFD9D9D9), shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun UploadButtonItem(isSelected: Boolean, onClick: () -> Unit) {
    val density = LocalDensity.current
    val stroke = with(density) {
        Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    }
    Box(
        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE3F2FD) else Color(0xFFE2E5EA))
            .drawBehind {
                drawRoundRect(
                    color = if (isSelected) Color(0xFF0088FF) else Color(0xFFADB5BD),
                    style = stroke, cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_upload), contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun PhotoOptionItemInDialog(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(28.dp), Color.Gray)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 18.sp, color = Color.Black)
    }
}
