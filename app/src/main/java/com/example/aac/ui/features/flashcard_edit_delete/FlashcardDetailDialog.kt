package com.example.aac.ui.features.flashcard_edit_delete

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aac.R
import com.example.aac.data.remote.dto.MainWordItem
import com.example.aac.ui.components.WordCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FlashcardDetailDialog(
    card: MainWordItem?,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
    onPlay: (MainWordItem) -> Unit = {},
    isAiSentence: Boolean = false, // 🟢 AI 문장 판별 변수
    onFavorite: (MainWordItem, Boolean) -> Unit = { _, _ -> },
    onEdit: (MainWordItem) -> Unit = {},
    onDelete: (MainWordItem) -> Unit = {}
) {
    if (card == null) return

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isFavorite by remember(card) { mutableStateOf(card.isFavorite) }
    val localSnackbarHostState = remember { SnackbarHostState() }
    val pointBlue = Color(0xFF0088FF)
    val defaultBorderColor = Color(0xFFD9D9D9)

    if (showDeleteConfirm) {
        FlashcardDeleteConfirmDialog(
            card = card,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
            onDismiss = { showDeleteConfirm = false },
            onConfirmDelete = {
                onDelete(it)
                showDeleteConfirm = false
                onDismiss()
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                modifier = Modifier.size(width = 530.dp, height = 603.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 51.dp, top = 32.dp, end = 51.dp, bottom = 48.dp)
                ) {
                    // [닫기 버튼]
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(width = 1.dp, color = Color(0xFF666666)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "닫기",
                                modifier = Modifier.size(15.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        // [카드 낱말]
                        WordCard(
                            text = card.word,
                            imageUrl = card.imageUrl,
                            partOfSpeech = card.partOfSpeech,
                            modifier = Modifier
                                .size(175.dp)
                                .border(1.dp, defaultBorderColor, RoundedCornerShape(16.dp)),
                            cornerRadius = 16.dp,
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // [메뉴 버튼들]
                        DetailMenuButton(
                            text = "재생",
                            icon = R.drawable.ic_play,
                            containerColor = pointBlue,
                            contentColor = Color.White,
                            useDefaultInteraction = false,
                            onClick = { onPlay(card) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailMenuButton(
                            text = "즐겨찾기",
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            iconTint = pointBlue,
                            onClick = {
                                isFavorite = !isFavorite
                                coroutineScope.launch {
                                    localSnackbarHostState.showSnackbar(
                                        if (isFavorite) "즐겨찾기에 추가했어요." else "즐겨찾기를 해제했어요."
                                    )
                                }
                                onFavorite(card, isFavorite)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 🟢 AI 문장이 아닐 때만 수정하기 버튼 렌더링
                        if (!isAiSentence) {
                            DetailMenuButton(
                                text = "수정하기",
                                icon = R.drawable.ic_edit_2,
                                iconTint = pointBlue,
                                onClick = { onEdit(card) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        DetailMenuButton(
                            text = "삭제하기",
                            icon = R.drawable.ic_delete_2,
                            contentColor = Color.Red,
                            onClick = { showDeleteConfirm = true }
                        )

                        Spacer(modifier = Modifier.weight(1.2f))
                    }
                }
            }

            // 내부 스낵바 호스트
            SnackbarHost(
                hostState = localSnackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
            ) { data ->
                val isFavMsg = data.visuals.message.contains("즐겨찾기")
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
                    modifier = Modifier.height(42.dp).then(if (isFavMsg) Modifier.width(232.dp) else Modifier.wrapContentWidth())
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(data.visuals.message, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMenuButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: Int? = null,
    imageVector: ImageVector? = null,
    containerColor: Color? = null,
    contentColor: Color = Color.Black,
    iconTint: Color? = null,
    useDefaultInteraction: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = if (useDefaultInteraction) {
        if (isPressed) Color(0xFFC4CAD4) else Color(0xFFE2E5EA)
    } else {
        containerColor ?: Color(0xFFE2E5EA)
    }
    val borderColor = Color(0xFFD9D9D9)

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.then(if (modifier == Modifier) Modifier.fillMaxWidth() else Modifier).height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) Icon(painterResource(icon), null, Modifier.size(24.dp), iconTint ?: contentColor)
            else if (imageVector != null) Icon(imageVector, null, Modifier.size(24.dp), iconTint ?: contentColor)
            Spacer(Modifier.width(10.dp))
            Text(text, color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}