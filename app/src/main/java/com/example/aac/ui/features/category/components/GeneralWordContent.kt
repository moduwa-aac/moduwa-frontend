package com.example.aac.ui.features.category.components

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.domain.model.Word
import com.example.aac.ui.components.WordCard
import sh.calvin.reorderable.*

@Composable
fun GeneralWordContent(
    uiList: MutableList<Word>,
    onAddClick: () -> Unit,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit,
    onReorder: (List<String>) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()

    val commonSpacing = 17.dp
    val tightVerticalSpacing = 8.dp
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth - 70.dp
        val cardSize = (availableWidth - (commonSpacing * 6) - 32.dp) / 7
        val rowHeight = cardSize + tightVerticalSpacing
        val maxRows = (availableHeight / rowHeight).toInt().coerceAtLeast(1)
        val dynamicPageSize = maxRows * 7

        val totalItemsCount = uiList.size + 1
        val maxPage = if (totalItemsCount <= 0) 0 else (totalItemsCount - 1) / dynamicPageSize
        if (currentPage > maxPage) currentPage = maxPage

        val currentDisplayItems = remember(uiList.toList(), currentPage, dynamicPageSize) {
            val fullList = mutableListOf<Any>().apply {
                if (currentPage == 0) add("ADD_BUTTON")
                addAll(uiList)
            }
            val start = currentPage * dynamicPageSize
            val end = minOf(start + dynamicPageSize, fullList.size)
            if (start < fullList.size) fullList.subList(start, end).toList() else emptyList()
        }

        val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
            if (from.key == "ADD_BUTTON" || to.key == "ADD_BUTTON") return@rememberReorderableLazyGridState
            
            val fromIndex = uiList.indexOfFirst { it.cardId == from.key }
            val toIndex = uiList.indexOfFirst { it.cardId == to.key }
            
            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                uiList.add(toIndex, uiList.removeAt(fromIndex))
                val orderedIds = uiList.map { it.cardId }
                onReorder(orderedIds)
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                    verticalArrangement = Arrangement.spacedBy(tightVerticalSpacing),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentDisplayItems, key = { if (it is String) it else (it as Word).cardId }) { item ->
                        if (item == "ADD_BUTTON") {
                            // 🔥 [통합] 어미 카테고리와 동일한 점선 스타일 버튼 적용
                            DashedAddCardItem(onClick = onAddClick)
                        } else {
                            val word = item as Word
                            ReorderableItem(state = reorderableState, key = word.cardId) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                                
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .shadow(elevation, RoundedCornerShape(12.dp))
                                        .pointerInput(word.cardId) {
                                            detectTapGestures(
                                                onTap = { onWordClick(word) },
                                                onLongPress = { onWordLongClick(word) }
                                            )
                                        }
                                        .draggableHandle()
                                ) {
                                    WordCard(
                                        text = word.word,
                                        imageUrl = word.imageUrl,
                                        partOfSpeech = word.partOfSpeech,
                                        modifier = Modifier.fillMaxSize(),
                                        onClick = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.width(51.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                ScrollButton(R.drawable.btn_up, "위로", Modifier.weight(1f)) { if (currentPage > 0) currentPage-- }
                Spacer(modifier = Modifier.height(9.dp))
                ScrollButton(R.drawable.btn_down, "아래로", Modifier.weight(1f)) { if (currentPage < maxPage) currentPage++ }
            }
        }
    }
}

/**
 * 🔥 [통합 컴포넌트] 어미 카테고리 추가 버튼과 동일한 디자인
 */
@Composable
fun DashedAddCardItem(onClick: () -> Unit) {
    val density = LocalDensity.current
    val brandBlue = Color(0xFF0088FF)
    
    val stroke = with(density) {
        Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f),
        )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .drawBehind {
                drawRoundRect(
                    color = brandBlue,
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add, 
                contentDescription = null, 
                tint = brandBlue, 
                modifier = Modifier.size(40.83.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "낱말카드 추가", 
                color = brandBlue, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScrollButton(imageRes: Int, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF66B3FF))
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(painterResource(id = imageRes), label, tint = Color.Unspecified, modifier = Modifier.size(25.dp))
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
