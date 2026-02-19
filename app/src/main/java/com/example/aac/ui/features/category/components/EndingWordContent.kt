package com.example.aac.ui.features.category.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import com.example.aac.domain.model.Word
import com.example.aac.ui.components.getBackgroundColorByPartOfSpeech
import com.example.aac.ui.components.getSafeUrl

@Composable
fun EndingWordContent(
    wordList: List<Word>,
    onAddClick: () -> Unit,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit
) {
    val scrollState = rememberScrollState()
    // 🔥 5개 제한 체크
    val isEnabled = wordList.size < 5

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DashedAddCardItem(
                isEnabled = isEnabled,
                onClick = onAddClick
            )

            wordList.forEach { word ->
                EndingWordCardItem(
                    word = word,
                    onClick = { onWordClick(word) },
                    onLongClick = { onWordLongClick(word) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EndingWordCardItem(
    word: Word,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = getBackgroundColorByPartOfSpeech(word.partOfSpeech)

    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getSafeUrl(word.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = word.word,
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit,
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = word.word,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashedAddCardItem(
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    
    // 🔥 요청하신 디자인 컬러 적용 (#0088FF)
    val brandBlue = Color(0xFF0088FF)
    val disabledColor = Color(0xFFB2B2B2)
    
    val contentColor = if (isEnabled) brandBlue else disabledColor
    val borderColor = if (isEnabled) brandBlue else disabledColor
    
    // 🔥 점선 스타일 (2px 두께, 5:5 간격)
    val stroke = with(density) {
        Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f),
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(130.dp) // 130x130
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = if (isEnabled) LocalIndication.current else null,
                onClick = { if (isEnabled) onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add, 
                contentDescription = null, 
                tint = contentColor, 
                modifier = Modifier.size(40.83.dp) // 40.83x40.83
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "낱말카드 추가", 
                color = contentColor, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}
