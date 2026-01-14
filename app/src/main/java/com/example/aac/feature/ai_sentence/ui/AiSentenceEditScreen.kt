package com.example.aac.feature.ai_sentence.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSentenceEditScreen(
    initialText: String,
    initialIsFavorite: Boolean = false,
    onBack: () -> Unit,
    onTextChanged: (String) -> Unit = {},
    onFavoriteChanged: (Boolean) -> Unit = {},
    onPlay: (String) -> Unit = {}
) {
    val originalText = remember(initialText) { initialText }

    var text by rememberSaveable { mutableStateOf(initialText) }
    var isFavorite by rememberSaveable { mutableStateOf(initialIsFavorite) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 스낵바 상태 관리
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val skyBlue = Color(0xFF66B2FF)

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
            CenterAlignedTopAppBar(
                title = { Text("AI 문장 편집") },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        IconButton(onClick = { goBack() }) {
                            Row(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(skyBlue),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back),
                                    contentDescription = "뒤로가기",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(text = "뒤로가기", color = Color(0xFF333333))
                    }
                }
            )
        },
        // 👇 커스텀 스낵바 Host 추가
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .height(42.dp)
                        .widthIn(min = 232.dp)
                        .background(
                            color = Color(0xFFEEEEEE), // 연한 회색 배경
                            shape = RoundedCornerShape(21.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.Black, // 검은 글씨
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .imePadding()
        ) {
            // 📐 컨테이너: 1128 x 118
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
                    // 1. 텍스트 입력 필드
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
                                onTextChanged(it)
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

                    // 2. 버튼 그룹
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 되돌리기 버튼
                        ActionSquareButton(
                            label = "되돌리기",
                            iconRes = R.drawable.ic_back, // 아이콘 이름 확인 필요 (undo 등)
                            iconTint = Color.White,
                            enabled = text != originalText,
                            backgroundColor = Color(0xFF505050),
                            contentColor = Color.White
                        ) {
                            text = originalText
                            onTextChanged(text)
                            // 스낵바 표시
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("문장을 되돌렸어요.", duration = SnackbarDuration.Short)
                            }
                        }

                        // 즐겨찾기 버튼
                        ActionSquareButton(
                            label = "즐겨찾기",
                            iconRes = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off,
                            iconTint = Color.White,
                            enabled = true,
                            backgroundColor = Color(0xFFFFD54F),
                            contentColor = Color.White
                        ) {
                            isFavorite = !isFavorite
                            onFavoriteChanged(isFavorite)
                            // 스낵바 표시 (상태에 따라 메시지 분기)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val msg = if (isFavorite) "즐겨찾기에 추가했어요." else "즐겨찾기를 해제했어요."
                                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                            }
                        }

                        // 재생 버튼
                        ActionSquareButton(
                            label = "재생",
                            iconRes = R.drawable.ic_play,
                            iconTint = Color.White,
                            enabled = true,
                            backgroundColor = Color(0xFF66B2FF),
                            contentColor = Color.White
                        ) {
                            onPlay(text)
                            // 스낵바 표시
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("문장을 재생했어요.", duration = SnackbarDuration.Short)
                            }
                        }
                    }
                }
            }

            // 글자수 표시
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

// 📐 버튼 컴포넌트 (86 x 86)
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