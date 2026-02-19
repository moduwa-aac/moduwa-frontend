package com.example.aac.ui.features.ai_sentence.ui

import com.example.aac.ui.features.ai_sentence.ui.AiSentenceViewModel.AiSentenceUiEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aac.R
import com.example.aac.ui.components.CustomTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSentenceEditScreen(
    initialText: String,
    initialIsFavorite: Boolean = false,
    onBack: () -> Unit,
    // 🟢 뷰모델을 화면 안으로 주입하여 직접 API를 호출하도록 설정
    vm: AiSentenceViewModel = viewModel()
) {
    val originalText = remember(initialText) { initialText }

    var text by rememberSaveable { mutableStateOf(initialText) }
    var isFavorite by rememberSaveable { mutableStateOf(initialIsFavorite) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 🟢 TTS 재생과 뷰모델 이벤트를 위해 Context 추가
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lightGrayBg = Color(0xFFF4F4F4)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun goBack() {
        focusManager.clearFocus()
        keyboardController?.hide()
        onBack()
    }

    BackHandler { goBack() }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "AI 문장 편집",
                onBackClick = { goBack() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .height(42.dp)
                        .widthIn(min = 232.dp)
                        .background(
                            color = Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(21.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        },
        containerColor = lightGrayBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .imePadding()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "문장을 입력하세요",
                                style = TextStyle(fontSize = 20.sp, color = Color.Gray)
                            )
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = {
                                text = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                color = Color.Black
                            ),
                            cursorBrush = SolidColor(Color.Black),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionSquareButton(
                            label = "되돌리기",
                            iconRes = R.drawable.ic_back,
                            iconTint = Color.White,
                            enabled = text != originalText,
                            backgroundColor = Color(0xFF505050),
                            contentColor = Color.White
                        ) {
                            text = originalText
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("문장을 되돌렸어요.", duration = SnackbarDuration.Short)
                            }
                        }

                        ActionSquareButton(
                            label = "즐겨찾기",
                            iconRes = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off,
                            iconTint = Color.White,
                            enabled = true,
                            backgroundColor = Color(0xFFFFD54F),
                            contentColor = Color.White
                        ) {
                            // 🟢 1. 수정한 텍스트를 바로 즐겨찾기 API로 전송
                            vm.onEvent(AiSentenceUiEvent.ClickFavorite(id = 0, text = text), context) { msg ->
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                                    // 서버 응답이 성공적이면 별 모양 켜기
                                    if (msg.contains("추가되었습니다")) {
                                        isFavorite = true
                                    }
                                }
                            }
                        }

                        ActionSquareButton(
                            label = "재생",
                            iconRes = R.drawable.ic_play,
                            iconTint = Color.White,
                            enabled = true,
                            backgroundColor = Color(0xFF66B2FF),
                            contentColor = Color.White
                        ) {
                            // 🟢 2. 수정한 텍스트를 바로 TTS API로 전송하여 재생
                            vm.onEvent(AiSentenceUiEvent.ClickPlaySentence(id = 0, text = text), context) { msg ->
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    // 에러가 났을 때만 스낵바를 띄우도록 처리
                                    if (msg.isNotEmpty()) {
                                        snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.size(8.dp))
            Text(
                text = "${text.length}자",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ActionSquareButton(
    label: String,
    @DrawableRes iconRes: Int,
    iconTint: Color? = Color.White,
    enabled: Boolean,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val bg = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.35f)
    val fg = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)

    Surface(
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(86.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = iconTint ?: Color.Unspecified
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = fg
            )
        }
    }
}