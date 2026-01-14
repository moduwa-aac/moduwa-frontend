package com.example.aac.feature.ai_sentence.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SvgButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    width: Dp = 72.dp,
    height: Dp = 64.dp,
    corner: Dp = 12.dp,
    iconSize: Dp = 26.dp,

    // ✅ 아이콘이 단색이면 contentColor로 tint
    // ✅ 멀티컬러 벡터면 iconTint = null 로 넣으면 원본 색 유지
    iconTint: Color? = contentColor,
) {
    val a = if (enabled) 1f else 0.45f

    Surface(
        onClick = onClick,
        enabled = enabled,
        color = backgroundColor,
        shape = RoundedCornerShape(corner),
        modifier = modifier
            .size(width, height)
            .alpha(a)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(iconSize),
                tint = iconTint ?: Color.Unspecified
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}
