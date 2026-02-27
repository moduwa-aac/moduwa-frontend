package com.example.aac.ui.features.category.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R
import com.example.aac.domain.model.Word
import com.example.aac.ui.components.WordCard
import sh.calvin.reorderable.*

val FigmaTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp,
    textAlign = TextAlign.Center,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

@Composable
fun GeneralWordContent(
    uiList: MutableList<Word>,
    onAddClick: () -> Unit,
    onWordClick: (Word) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxHeight = maxHeight

        // 📏 공간 확보를 위한 수치 조정
        val cardHeight = 130.dp
        val verticalSpacing = 10.dp // 20 -> 10으로 축소
        val topPadding = 12.dp      // 24 -> 12로 축소
        val bottomPadding = 12.dp   // 24 -> 12로 축소

        val availableHeight = maxHeight - topPadding - bottomPadding
        val rowHeight = cardHeight + verticalSpacing

        // 3줄이 나오도록 계산 (버림 대신 아주 살짝 넘는 것도 허용하게 처리)
        val maxRows = (availableHeight / rowHeight).toInt().coerceAtLeast(1)
        val itemsPerPage = maxRows * 7

        val totalItemsCount = uiList.size + 1
        val maxPage = if (totalItemsCount == 0) 0 else (totalItemsCount - 1) / itemsPerPage

        if (currentPage > maxPage) currentPage = maxPage

        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, totalItemsCount)

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = topPadding), // 상하 패딩 줄임
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                val gridState = rememberLazyGridState()
                val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
                    if (from.key == "ADD_BUTTON" || to.key == "ADD_BUTTON") return@rememberReorderableLazyGridState
                    val fromIndex = uiList.indexOfFirst { it.cardId == (from.key as? String) }
                    val toIndex = uiList.indexOfFirst { it.cardId == (to.key as? String) }
                    if (fromIndex != -1 && toIndex != -1) {
                        uiList.apply { add(toIndex, removeAt(fromIndex)) }
                    }
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing), // 간격 줄임
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    for (i in startIndex until endIndex) {
                        if (i == 0) {
                            item(key = "ADD_BUTTON") {
                                AddWordCardItem(onClick = onAddClick)
                            }
                        } else {
                            val dataIndex = i - 1
                            if (dataIndex < uiList.size) {
                                val item = uiList[dataIndex]
                                item(key = item.cardId) {
                                    ReorderableItem(state = reorderableState, key = item.cardId) { isDragging ->
                                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                                        WordCard(
                                            text = item.word,
                                            imageUrl = item.imageUrl,
                                            partOfSpeech = item.partOfSpeech,
                                            modifier = Modifier
                                                .size(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .shadow(elevation, RoundedCornerShape(12.dp))
                                                .draggableHandle(),
                                            onClick = { onWordClick(item) }
                                        )
                                    }
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
                ScrollButton(
                    imageRes = R.drawable.btn_up,
                    label = "위로",
                    modifier = Modifier.weight(1f),
                    onClick = { if (currentPage > 0) currentPage-- }
                )
                Spacer(modifier = Modifier.height(9.dp))
                ScrollButton(
                    imageRes = R.drawable.btn_down,
                    label = "아래로",
                    modifier = Modifier.weight(1f),
                    onClick = { if (currentPage < maxPage) currentPage++ }
                )
            }
        }
    }
}

// AddWordCardItem, ScrollButton은 기존과 동일하므로 생략하지 않고 유지하세요.
@Composable
fun AddWordCardItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F9FA))
            .border(BorderStroke(2.dp, Color(0xFFE0E0E0)), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF267FD6), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "낱말 추가", color = Color(0xFF267FD6), style = FigmaTextStyle)
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
        Box(modifier = Modifier.size(25.dp), contentAlignment = Alignment.Center) {
            Icon(painterResource(id = imageRes), label, tint = Color.Unspecified, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(text = label, color = Color.White, style = FigmaTextStyle)
    }
}