package com.example.aac.ui.features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun CardsArea(modifier: Modifier = Modifier) {
    // Simple grid-like tile layout using rows of fixed count to act as placeholder cards.
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        val totalCards = 12
        val columns = 3
        var index = 0
        while (index < totalCards) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until columns) {
                    if (index >= totalCards) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Card ${index + 1}", fontSize = 14.sp, color = Color.Black)
                        }
                        index++
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun CardScrollBar() {
    Column(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Up control
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(Color(0xFFDDDDDD), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("˄", fontSize = 18.sp, color = Color.Black)
        }

        // Scroll indicator placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(56.dp)
                    .background(Color(0xFFBDBDBD), RoundedCornerShape(6.dp))
            )
        }

        // Down control
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(Color(0xFFDDDDDD), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("˅", fontSize = 18.sp, color = Color.Black)
        }
    }
}
