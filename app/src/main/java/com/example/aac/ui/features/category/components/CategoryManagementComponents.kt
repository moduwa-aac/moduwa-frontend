package com.example.aac.ui.features.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import com.example.aac.ui.features.category.CategoryEditData
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// 1. 상단 탭
@Composable
fun ManagementTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFFEDEDED),
        contentColor = Color(0xFF267FD6),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = Color(0xFF0088FF)
            )
        }
    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.background(
                if (selectedTabIndex == 0) Color.White else Color(0xFFEDEDED)
            ),
            text = {
                Text(
                    text = "카테고리 관리",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = if (selectedTabIndex == 0) Color(0xFF0088FF) else Color(0xFF636363)
                )
            }
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.background(
                if (selectedTabIndex == 1) Color.White else Color(0xFFEDEDED)
            ),
            text = {
                Text(
                    text = "낱말 카드 관리",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = if (selectedTabIndex == 1) Color(0xFF0088FF) else Color(0xFF636363)
                )
            }
        )
    }
}

// 2. 팁 박스
@Composable
fun TipBox(
    text: String = "팁 : 카테고리를 드래그하여 순서를 변경하실 수 있어요! 자주 사용하는 카테고리를 상단에 배치해보세요."
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(1116.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE9F3FF))
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "✨",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

// 3. 카테고리 추가 버튼
@Composable
fun AddCategoryButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(1116.dp)
                .height(86.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3199FF)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "카테고리 추가",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// 4. 리스트 아이템
@Composable
fun CategoryEditListItem(
    data: CategoryEditData,
    isDragging: Boolean,
    dragModifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val backgroundColor = Color.White
    val shadowElevation = if (isDragging) 8.dp else 0.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(1116.dp)
                .height(86.dp)
                .shadow(shadowElevation, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(12.dp))
                .padding(start = 13.dp, end = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dot),
                contentDescription = "순서 변경",
                tint = Color.Unspecified,
                modifier = dragModifier
                    .size(44.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 아이콘 표시 영역
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFD7E6F9), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                // iconUrl이 있으면 이미지 표시, 없으면 iconRes 표시
                if (!data.iconUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 아이콘 리소스가 0인 경우 방어 로직 포함
                    val icon = if (data.iconRes != 0) data.iconRes else R.drawable.ic_default
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = data.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C),
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 10.sp
                    )
                )
                Text(
                    text = "${data.count}개의 낱말",
                    fontSize = 18.sp,
                    color = Color(0xFF494949),
                    fontWeight = FontWeight.SemiBold,
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 10.sp
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditOptionButton(
                    text = "편집",
                    icon = painterResource(id = R.drawable.ic_edit1),
                    color = Color.White,
                    backgroundColor = Color(0xFF66B3FF),
                    onClick = onEditClick
                )

                EditOptionButton(
                    text = "삭제",
                    icon = painterResource(id = R.drawable.ic_trash2),
                    color = Color.White,
                    backgroundColor = Color(0xE5F84D4D),
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun EditOptionButton(
    text: String,
    icon: Painter,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            fontSize = 21.sp,
            color = color,
            fontWeight = FontWeight.Normal
        )
    }
}

// 5. 카테고리 선택 바 (WordCardManagementContent에서 사용)
@Composable
fun CategorySelectorBar(
    currentCategory: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_category),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "카테고리 선택",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(text = currentCategory, fontSize = 20.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
