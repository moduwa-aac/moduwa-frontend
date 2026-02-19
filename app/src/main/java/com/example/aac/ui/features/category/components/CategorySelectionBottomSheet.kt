package com.example.aac.ui.features.category.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aac.R
import com.example.aac.ui.features.category.CategoryEditData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    categoryList: List<CategoryEditData>,
    onDismissRequest: () -> Unit,
    onCategorySelected: (CategoryEditData) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<CategoryEditData?>(null) }

    // 최근사용 및 즐겨찾기 카테고리 제외 필터링
    val filteredList = remember(categoryList) {
        categoryList.filterNot { it.title == "최근사용" || it.title == "즐겨찾기" }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            ) {
                Text(
                    text = "카테고리를 선택하세요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.Black
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(13),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(11.22.dp),
                verticalArrangement = Arrangement.spacedBy(11.22.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filteredList) { item ->
                    CategoryItemCard(
                        category = item,
                        isSelected = selectedCategory?.id == item.id,
                        onClick = {
                            Log.d("SHEET_DEBUG", "[클릭] ${item.title} (ID: ${item.id})")
                            selectedCategory = item
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (selectedCategory != null) {
                        Log.d("SHEET_DEBUG", "[완료] 선택된 카테고리 반환: ${selectedCategory?.title} (ID: ${selectedCategory?.id})")
                        onCategorySelected(selectedCategory!!)
                        onDismissRequest()
                    } else {
                        onDismissRequest()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "완료",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CategoryItemCard(
    category: CategoryEditData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE0E0E0)
    val borderWidth = 1.87.dp

    Column(
        modifier = Modifier
            .size(width = 86.dp, height = 86.dp)
            .clip(RoundedCornerShape(11.22.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(11.22.dp))
            .padding(top = 5.61.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // iconUrl이 있으면 이미지 표시, 없으면 iconRes 표시
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!category.iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(category.iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val icon = if (category.iconRes != 0) category.iconRes else R.drawable.ic_default
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
