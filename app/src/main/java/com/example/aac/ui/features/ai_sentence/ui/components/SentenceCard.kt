package com.example.aac.feature.ai_sentence.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aac.R

@Composable
fun SentenceCard(
    text: String,
    isFavorite: Boolean,
    onEdit: () -> Unit,
    onFavorite: () -> Unit,
    onPlay: () -> Unit
) {
    val border = Color(0xFFE0E0E0)
    val skyBlue = Color(0xFF66B2FF)
    val yellow = Color(0xFFFFD54F)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SvgButton(
                    iconRes = R.drawable.ic_edit,
                    label = "편집",
                    onClick = onEdit,
                    backgroundColor = skyBlue,
                    width = 72.dp,
                    height = 64.dp,
                    corner = 12.dp,
                    iconTint = Color.White
                )

                SvgButton(
                    iconRes = if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off,
                    label = "즐겨찾기",
                    onClick = onFavorite,
                    backgroundColor = yellow,
                    width = 72.dp,
                    height = 64.dp,
                    corner = 12.dp,
                    iconTint = Color.White,
                    contentColor = Color.White
                )

                SvgButton(
                    iconRes = R.drawable.ic_play,
                    label = "재생",
                    onClick = onPlay,
                    backgroundColor = skyBlue,
                    width = 72.dp,
                    height = 64.dp,
                    corner = 12.dp,
                    iconTint = Color.White
                )
            }
        }
    }
}
