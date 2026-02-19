package com.example.aac.ui.features.flashcard_edit_delete

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import com.example.aac.data.mapper.IconMapper
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.domain.model.Category
import com.example.aac.ui.components.getBackgroundColorByPartOfSpeech
import com.example.aac.ui.components.getSafeUrl
import com.example.aac.ui.features.category.CategoryEditData
import com.example.aac.ui.features.category.components.CategorySelectionBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardEditDialog(
    card: MainWordItem?,
    allCategories: List<Category>, // ✅ 동료의 도메인 모델 적용
    onDismiss: () -> Unit,
    onSave: (word: String, categoryId: String, newUri: Uri?, newBitmap: Bitmap?) -> Unit // ✅ 카메라(Bitmap) 지원 API
) {
    if (card == null) return

    var wordText by remember { mutableStateOf(card.word) }
    var selectedCategory by remember { mutableStateOf(allCategories.find { it.id == card.categoryId }) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isEditingWord by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val photoSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // ✅ 갤러리 및 카메라 런처 (동료 코드 적용)
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedBitmap = null
            Log.d("PHOTO_DEBUG", "수정 모달 - 갤러리 선택: $it")
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedBitmap = it
            selectedUri = null
            Log.d("PHOTO_DEBUG", "수정 모달 - 카메라 촬영")
        }
    }

    val pointBlue = Color(0xFF0088FF)
    val lightGrayBorder = Color(0xFFDDDDDD)
    val buttonBorderColor = Color(0xFFD9D9D9)

    LaunchedEffect(isEditingWord) {
        if (isEditingWord) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.width(520.dp).wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 51.dp, vertical = 48.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "낱말 카드 수정",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
                    )

                    // 1. 카테고리 선택 영역
                    Column(modifier = Modifier.width(426.dp)) {
                        Text("카테고리", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "카테고리 선택",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().height(51.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = lightGrayBorder,
                                unfocusedBorderColor = lightGrayBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = { ChangeButton { showCategorySheet = true } }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. 낱말 이름 수정 영역
                    Column(modifier = Modifier.width(426.dp)) {
                        Text(text = "낱말", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = wordText,
                            onValueChange = { if (isEditingWord) wordText = it },
                            readOnly = !isEditingWord,
                            modifier = Modifier.fillMaxWidth().height(51.dp).focusRequester(focusRequester),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isEditingWord) pointBlue else lightGrayBorder,
                                unfocusedBorderColor = lightGrayBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (isEditingWord) {
                                    IconButton({ wordText = "" }) { Icon(Icons.Default.Clear, null) }
                                } else {
                                    ChangeButton { isEditingWord = true }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { isEditingWord = false; keyboardController?.hide() })
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. 낱말 사진 수정 영역 (동료의 Bitmap/Uri 통합 렌더링 적용)
                    Column(modifier = Modifier.width(426.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "낱말 사진", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier.size(175.dp).clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(getBackgroundColorByPartOfSpeech(card.partOfSpeech)),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    selectedBitmap != null -> {
                                        Image(
                                            bitmap = selectedBitmap!!.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(1f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    selectedUri != null -> {
                                        AsyncImage(
                                            model = selectedUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(1f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(getSafeUrl(card.imageUrl))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = card.word,
                                            modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(1f),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }

                            val density = LocalDensity.current
                            val stroke = with(density) {
                                Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = pointBlue,
                                            style = stroke,
                                            cornerRadius = CornerRadius(16.dp.toPx())
                                        )
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "사진 변경하기", color = pointBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.clickable { showPhotoSheet = true })
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(modifier = Modifier.width(426.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, buttonBorderColor)
                        ) { Text("취소", color = Color.Black, fontSize = 18.sp) }

                        Button(
                            onClick = {
                                // ✅ 동료의 파라미터 구조에 맞춰서 저장 (ID 전달)
                                onSave(wordText, selectedCategory?.id ?: card.categoryId, selectedUri, selectedBitmap)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = pointBlue),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, buttonBorderColor)
                        ) { Text("저장", color = Color.White, fontSize = 18.sp) }
                    }
                }
            }
        }
    }

    // ✅ 사진 선택 바텀 시트
    if (showPhotoSheet) {
        ModalBottomSheet({ showPhotoSheet = false }, sheetState = photoSheetState, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                Text("사진 업로드", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                PhotoOptionItem(Icons.Default.PhotoLibrary, "사진에서 불러오기") { galleryLauncher.launch("image/*"); showPhotoSheet = false }
                PhotoOptionItem(Icons.Default.AddAPhoto, "카메라로 촬영하기") { cameraLauncher.launch(null); showPhotoSheet = false }
            }
        }
    }

    // ✅ 공용 카테고리 선택 바텀 시트 (동료 코드 적용)
    if (showCategorySheet) {
        val displayCategories = allCategories.map { cat ->
            CategoryEditData(
                id = cat.id,
                title = cat.name,
                iconRes = IconMapper.toLocalResource(cat.iconKey),
                iconUrl = cat.iconUrl,
                count = cat.wordCount
            )
        }
        CategorySelectionBottomSheet(
            categoryList = displayCategories,
            onDismissRequest = { showCategorySheet = false },
            onCategorySelected = { selectedData ->
                selectedCategory = allCategories.find { it.id == selectedData.id }
                showCategorySheet = false
            }
        )
    }
}

@Composable
fun ChangeButton(onClick: () -> Unit) {
    val grayColor = Color(0xFF494949)
    Row(
        modifier = Modifier.padding(end = 20.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(id = R.drawable.ic_edit1), null, modifier = Modifier.size(18.dp), tint = grayColor)
        Spacer(Modifier.width(4.dp))
        Text("변경", fontSize = 14.sp, color = grayColor)
    }
}

@Composable
fun PhotoOptionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(28.dp), Color.Gray)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 18.sp, color = Color.Black)
    }
}