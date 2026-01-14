package com.example.aac.feature.ai_sentence.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aac.R

@Composable
fun SelectedWordRow(
    words: List<String>,
    onPlayTop: () -> Unit
) {
    val border = Color(0xFFE0E0E0)
    val skyBlue = Color(0xFF66B2FF)
    val chipBg = Color(0xFFFFF1B8)

    // ✅ 현재 "삭제 표시"가 켜진 칩 인덱스 (없으면 -1)
    var markedIndex by rememberSaveable { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "선택한 낱말",
            style = MaterialTheme.typography.titleMedium
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(words) { index, _ ->
                        WordChip(
                            label = "나",
                            background = chipBg,
                            iconRes = R.drawable.ic_pictogram, // ✅ 픽토그램은 PNG
                            isMarked = (markedIndex == index),
                            onClick = {
                                markedIndex = if (markedIndex == index) -1 else index
                            }
                        )
                    }
                }

                SvgButton(
                    iconRes = R.drawable.ic_play,
                    label = "재생",
                    onClick = onPlayTop,
                    backgroundColor = skyBlue,
                    width = 76.dp,
                    height = 76.dp,
                    corner = 12.dp,
                    iconSize = 30.dp,
                    iconTint = Color.White
                )
            }
        }
    }
}

@Composable
private fun WordChip(
    label: String,
    background: Color,
    iconRes: Int,
    isMarked: Boolean,
    onClick: () -> Unit
) {
    val red = Color(0xFFE53935)
    val chipShape = RoundedCornerShape(10.dp)

    Surface(
        color = background,
        shape = chipShape,
        border = if (isMarked) BorderStroke(3.dp, red) else null,
        modifier = Modifier
            .width(64.dp)
            .height(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "픽토그램",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isMarked) {
                // ✅ 살짝 어둡게
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
                // ✅ 중앙 X 표시
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "삭제 표시",
                    tint = red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                )
            }
        }
    }
}
