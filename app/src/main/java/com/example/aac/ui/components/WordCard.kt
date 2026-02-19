package com.example.aac.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import java.net.URLEncoder

fun getBackgroundColorByPartOfSpeech(partOfSpeech: String): Color {
    return when (partOfSpeech) {
        "NOUN", "명사" -> Color(0xFFFFE099)
        "VERB", "동사" -> Color(0xFFC2ECC9)
        "ADJECTIVE", "형용사" -> Color(0xFFCCE0FF)
        "ADVERB", "부사", "PREPOSITION", "전치사", "MODIFIER", "관형사" -> Color(0xFFF0C2FF)
        else -> Color(0xFFFBFBF8)
    }
}

fun getSafeUrl(url: String): String {
    return try {
        if (url.isBlank()) return url
        val fileName = url.substringAfterLast("/")
        val baseUrl = url.substringBeforeLast("/")
        val encodedName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        "$baseUrl/$encodedName"
    } catch (e: Exception) {
        url
    }
}

@Composable
fun WordCard(
    text: String,
    imageUrl: String?,
    partOfSpeech: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    fontSize: TextUnit = 20.sp,
    iconSize: Dp = 65.dp,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = getBackgroundColorByPartOfSpeech(partOfSpeech)

    val safeImageUrl = remember(imageUrl) {
        // 🔎 [확인 필수] 로그캣에 'WordCard'라고 검색해서 서버가 대체 무슨 주소를 주는지 확인하세요!
        android.util.Log.d("WordCard", "서버가 던져준 이미지 주소: $imageUrl")

        if (
            imageUrl.isNullOrBlank() ||
            imageUrl == "null" ||
            imageUrl == "undefined" ||
            imageUrl.contains("default", ignoreCase = true) // 🔥 서버의 기본 이미지 이름에 'default'가 들어가면 강제 차단!
        ) {
            ""
        } else {
            getSafeUrl(imageUrl)
        }
    }
    val hasImage = safeImageUrl.isNotBlank()

    // 🔥 [핵심] 이미지가 있으면 2줄, 없으면 5줄
    val maxLines = if (hasImage) 2 else 5

    // 이미지가 없을 때 글자가 꽉 차 보이도록 폰트를 살짝 키워줍니다 (보내주신 사진 느낌)
    val finalFontSize = if (hasImage) fontSize else fontSize * 1.1f

    val finalModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    val borderStroke = if (borderColor != null) {
        BorderStroke(2.dp, borderColor)
    } else {
        null
    }

    Card(
        modifier = finalModifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // 🔥 [핵심] 이미지가 빠지면 Text가 자동으로 정중앙에 위치하게 됨
            verticalArrangement = Arrangement.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(safeImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = text,
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 텍스트 영역
            Text(
                text = text,
                fontSize = finalFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,

                // 🔥 [핵심] 여기서 줄 수 제한(5줄) 및 말줄임표 처리
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,

                lineHeight = finalFontSize * 1.2f,

                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}