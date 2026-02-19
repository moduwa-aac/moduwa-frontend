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
    val verticalPadding = 16.dp // Box의 상하 패딩 합계 (8dp + 8dp)
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth - 70.dp // 우측 버튼 영역 제외
        
        // 1. 카드 크기 계산 (가로 7개 기준)
        val cardSize = (availableWidth - (commonSpacing * 6) - 32.dp) / 7
        val rowHeight = cardSize + tightVerticalSpacing
        
        // 2. 카드가 잘리지 않도록 온전한 행의 개수만 계산
        val maxRows = ((availableHeight - verticalPadding + tightVerticalSpacing) / rowHeight).toInt().coerceAtLeast(1)
        val dynamicPageSize = maxRows * 7

        // 전체 데이터 리스트 (추가 버튼 포함하여 전체 렌더링)
        val fullList = remember(uiList.toList()) {
            listOf("ADD_BUTTON") + uiList
        }

        val totalItemsCount = fullList.size
        val maxPage = if (totalItemsCount <= 0) 0 else (totalItemsCount - 1) / dynamicPageSize
        
        // 현재 페이지가 범위를 벗어나지 않도록 보정
        if (currentPage > maxPage) currentPage = maxPage

        // 페이지 번호 변경 시 해당 위치로 부드럽게 스크롤
        LaunchedEffect(currentPage) {
            gridState.animateScrollToItem(currentPage * dynamicPageSize)
        }

        // 드래그 앤 드롭 상태 관리
        val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
            if (from.key == "ADD_BUTTON" || to.key == "ADD_BUTTON") return@rememberReorderableLazyGridState
            
            val fromIndex = uiList.indexOfFirst { it.cardId == from.key }
            val toIndex = uiList.indexOfFirst { it.cardId == to.key }
            
            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                uiList.add(toIndex, uiList.removeAt(fromIndex))
                onReorder(uiList.map { it.cardId })
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    // 3. 행이 잘리지 않도록 높이를 계산된 행 수만큼 정확히 고정
                    .height(rowHeight * maxRows - tightVerticalSpacing + verticalPadding)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(commonSpacing),
                    verticalArrangement = Arrangement.spacedBy(tightVerticalSpacing),
                    // 수동 스크롤은 막되, 드래그 시의 자동 스크롤은 허용됨
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fullList, key = { if (it is String) it else (it as Word).cardId }) { item ->
                        if (item == "ADD_BUTTON") {
                            DashedAddCardItem(onClick = onAddClick)
                        } else {
                            val word = item as Word
                            ReorderableItem(state = reorderableState, key = word.cardId) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                                
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
                ScrollButton(R.drawable.btn_up, "위로", Modifier.weight(1f)) { 
                    if (currentPage > 0) currentPage-- 
                }
                Spacer(modifier = Modifier.height(9.dp))
                ScrollButton(R.drawable.btn_down, "아래로", Modifier.weight(1f)) { 
                    if (currentPage < maxPage) currentPage++ 
                }
            }
        }
    }
}

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
