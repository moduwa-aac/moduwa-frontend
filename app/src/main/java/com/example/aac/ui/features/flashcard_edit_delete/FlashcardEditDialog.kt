package com.example.aac.ui.features.flashcard_edit_delete

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.ui.components.CategoryItem // ViewModel이나 DTO에 있는 CategoryItem 필요
import com.example.aac.ui.components.getBackgroundColorByPartOfSpeech
import com.example.aac.ui.components.getSafeUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardEditDialog(
    card: MainWordItem?,
    categories: List<CategoryItem>, // ✅ 카테고리 리스트를 받아옵니다
    onDismiss: () -> Unit,
    // ✅ 저장 콜백: (단어, 카테고리명, 이미지URI)
    onSave: (String, String, String?) -> Unit
) {
    if (card == null) return

    var wordText by remember { mutableStateOf(card.word) }
    // 초기 카테고리 설정 (ID로 이름 찾기, 없으면 기본값)
    var selectedCategoryName by remember {
        mutableStateOf(categories.find { it.serverId == card.categoryId }?.name ?: "기본")
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // 새로 선택한 이미지
    var isEditingWord by remember { mutableStateOf(false) }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoSheetState = rememberModalBottomSheetState()
    val categorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 갤러리 런처
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
            showPhotoSheet = false // 선택 후 시트 닫기
        }
    )

    val pointBlue = Color(0xFF0088FF)
    val lightGrayBorder = Color(0xFFDDDDDD)
    val buttonBorderColor = Color(0xFFD9D9D9)

    val cardBackgroundColor = getBackgroundColorByPartOfSpeech(card.partOfSpeech)

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
                modifier = Modifier
                    .width(520.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 51.dp, top = 48.dp, end = 51.dp, bottom = 48.dp)
                        .fillMaxWidth(),
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
                        Text(text = "카테고리", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth().height(51.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = lightGrayBorder,
                                unfocusedBorderColor = lightGrayBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = { ChangeButton(onClick = { showCategorySheet = true }) }
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
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth().height(51.dp).focusRequester(focusRequester),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isEditingWord) pointBlue else lightGrayBorder,
                                unfocusedBorderColor = lightGrayBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (isEditingWord) {
                                    IconButton(onClick = { wordText = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "초기화")
                                    }
                                } else {
                                    ChangeButton(onClick = { isEditingWord = true })
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                isEditingWord = false
                                keyboardController?.hide()
                            })
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. 낱말 사진 수정 영역
                    Column(
                        modifier = Modifier.width(426.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "낱말 사진",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBackgroundColor)
                                .drawBehind {
                                    drawRoundRect(
                                        color = pointBlue,
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                    )
                                }
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            // 이미지가 변경되었으면 새 URI, 아니면 기존 URL 표시
                            val imageModel = selectedImageUri ?: getSafeUrl(card.imageUrl)

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                error = painterResource(R.drawable.ic_launcher_foreground)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "사진 변경하기",
                            fontSize = 18.sp,
                            color = pointBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showPhotoSheet = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        modifier = Modifier.width(426.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, buttonBorderColor)
                        ) {
                            Text(text = "취소", color = Color.Black, fontSize = 18.sp)
                        }

                        Button(
                            onClick = {
                                if (wordText.trim().isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("낱말을 입력해주세요.") }
                                } else {
                                    // 저장 로직 호출 (이미지는 변경된 경우 URI 문자열, 아니면 null 전달)
                                    onSave(wordText, selectedCategoryName, selectedImageUri?.toString())
                                }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = pointBlue),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, buttonBorderColor)
                        ) {
                            Text(text = "저장", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            ) { data ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEBEBEB)),
                    modifier = Modifier.height(42.dp).width(214.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = data.visuals.message, color = Color.Black, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }

    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, sheetState = photoSheetState, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                Text(text = "사진 업로드", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                PhotoOptionItem(Icons.Default.PhotoLibrary, "사진에서 불러오기") {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    showPhotoSheet = false
                }
                // 카메라는 별도 권한 및 로직 필요하므로 일단 토스트나 로그 처리
                PhotoOptionItem(Icons.Default.AddAPhoto, "카메라로 촬영하기") {
                    // TODO: 카메라 기능 구현
                    showPhotoSheet = false
                }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = categorySheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxWidth()
        ) {
            CategorySelectionContent(
                currentCategory = selectedCategoryName,
                categories = categories, // ✅ 실제 카테고리 리스트 전달
                onCategorySelected = { selectedCategoryName = it },
                onComplete = { showCategorySheet = false }
            )
        }
    }
}

// ✅ 카테고리 리스트를 동적으로 받도록 수정
@Composable
fun CategorySelectionContent(
    currentCategory: String,
    categories: List<CategoryItem>,
    onCategorySelected: (String) -> Unit,
    onComplete: () -> Unit
) {
    val pointBlue = Color(0xFF0088FF)
    val scrollState = rememberScrollState()
    val paddingLeft = 94.dp

    Column(
        modifier = Modifier
            .width(1280.dp)
            .height(380.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "카테고리를 선택하세요", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(start = paddingLeft),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { item ->
                val iconRes = R.drawable.ic_default
                CategoryButton(
                    name = item.name,
                    icon = iconRes,
                    isSelected = item.name == currentCategory,
                    pointBlue = pointBlue,
                    size = 86.dp,
                    onClick = { onCategorySelected(item.name) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onComplete, modifier = Modifier.padding(start = paddingLeft, bottom = 40.dp).width(1092.dp).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = pointBlue), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFD9D9D9))) { Text("완료", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun CategoryButton(name: String, icon: Int, isSelected: Boolean, pointBlue: Color, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Column(modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent).border(1.dp, if (isSelected) pointBlue else Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(painterResource(id = icon), name, Modifier.size(36.dp), Color.Unspecified)
        Spacer(Modifier.height(4.dp))
        Text(name, fontSize = 12.sp, color = if (isSelected) pointBlue else Color.Black, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable fun ChangeButton(onClick: () -> Unit) { val grayColor = Color(0xFF494949); Row(modifier = Modifier.padding(end = 20.dp).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(id = R.drawable.ic_edit1), null, Modifier.size(18.dp), grayColor); Spacer(Modifier.width(4.dp)); Text("변경", fontSize = 14.sp, color = grayColor) } }

@Composable fun PhotoOptionItem(icon: ImageVector, text: String, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(28.dp), Color.Gray); Spacer(Modifier.width(16.dp)); Text(text, fontSize = 18.sp) } }