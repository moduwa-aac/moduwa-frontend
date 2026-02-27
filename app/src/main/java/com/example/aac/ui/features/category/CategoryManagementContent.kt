package com.example.aac.ui.features.category

import android.net.Uri
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aac.R
import com.example.aac.ui.components.CommonDeleteDialog
import com.example.aac.ui.features.category.components.*
import sh.calvin.reorderable.*

@Composable
fun CategoryManagementContent(
    categoryList: SnapshotStateList<CategoryEditData>,
    // Uri와 Bitmap을 포함하도록 콜백 타입 확장
    onAddCategory: (String, Int, Uri?, Bitmap?) -> Unit,        
    onEditCategory: (String, String, Int, Uri?, Bitmap?) -> Unit, 
    onDeleteCategory: (String) -> Unit           
) {

    LaunchedEffect(categoryList.toList()) {
        Log.d("DATA_CHECK", " 카테고리 리스트 아이템 수: ${categoryList.size}")
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<CategoryEditData?>(null) }

    val listState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val fromKey = from.key
        val toKey = to.key
        val fromIndex = categoryList.indexOfFirst { (it.id ?: it.hashCode()) == fromKey }
        val toIndex = categoryList.indexOfFirst { (it.id ?: it.hashCode()) == toKey }

        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
            categoryList.apply { add(toIndex, removeAt(fromIndex)) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TipBox() }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AddCategoryButton(onClick = { showAddDialog = true })
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(items = categoryList, key = { it.id ?: it.hashCode() }) { item ->
            ReorderableItem(state = reorderableState, key = item.id ?: item.hashCode()) { isDragging ->
                CategoryEditListItem(
                    data = item,
                    isDragging = isDragging,
                    dragModifier = Modifier.draggableHandle(),
                    onEditClick = {
                        selectedCategory = item
                        showEditDialog = true
                    },
                    onDeleteClick = {
                        selectedCategory = item
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    // 1. [카테고리 생성] 다이얼로그
    if (showAddDialog) {
        val newCategoryTemplate = CategoryEditData(id = null, title = "", iconRes = R.drawable.ic_default, count = 0)
        CategoryEditDialog(
            category = newCategoryTemplate,
            onDismissRequest = { showAddDialog = false },
            onSaveClick = { name, icon, uri, bitmap ->
                Log.d("CATEGORY_API", "생성 요청: $name")
                onAddCategory(name, icon, uri, bitmap) 
                showAddDialog = false
            }
        )
    }

    // 2. [카테고리 편집] 다이얼로그
    if (showEditDialog && selectedCategory != null) {
        CategoryEditDialog(
            category = selectedCategory!!,
            onDismissRequest = { showEditDialog = false },
            onSaveClick = { newName, newIcon, uri, bitmap ->
                val targetId = selectedCategory!!.id
                if (targetId != null) {
                    Log.d("CATEGORY_API", "수정 요청 ID: $targetId")
                    onEditCategory(targetId, newName, newIcon, uri, bitmap)

                    val index = categoryList.indexOfFirst { it.id == targetId }
                    if (index != -1) {
                        categoryList[index] = categoryList[index].copy(
                            title = newName,
                            iconRes = newIcon
                        )
                    }
                }
                showEditDialog = false
            }
        )
    }

    // 3. [카테고리 삭제] 다이얼로그
    if (showDeleteDialog && selectedCategory != null) {
        CommonDeleteDialog(
            message = "카테고리를 삭제하시겠어요?\n포함된 낱말은 모두 삭제돼요.",
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                val targetId = selectedCategory!!.id
                if (targetId != null) {
                    onDeleteCategory(targetId)
                }
                categoryList.remove(selectedCategory)
                showDeleteDialog = false
            }
        )
    }
}
