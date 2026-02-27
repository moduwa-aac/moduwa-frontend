package com.example.aac.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex

class DragDropState(
    val onMove: (Int, Int) -> Unit,
    val onDragEnd: () -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    private val itemBounds = mutableMapOf<Int, Rect>()

    fun onPositioned(index: Int, bounds: Rect) {
        itemBounds[index] = bounds
    }

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        dragOffset = Offset.Zero
    }

    fun onDrag(dragAmount: Offset) {
        dragOffset += dragAmount

        val currentIndex = draggingItemIndex ?: return
        val currentBounds = itemBounds[currentIndex] ?: return
        val currentCenter = currentBounds.center + dragOffset

        for ((targetIndex, targetBounds) in itemBounds) {
            if (targetIndex != currentIndex && targetBounds.contains(currentCenter)) {
                onMove(currentIndex, targetIndex)

                // 위치 보정
                val positionDelta = targetBounds.topLeft - currentBounds.topLeft
                dragOffset -= positionDelta

                draggingItemIndex = targetIndex
                return
            }
        }
    }

    fun onDragCancel() {
        draggingItemIndex = null
        dragOffset = Offset.Zero
        onDragEnd()
    }
}

@Composable
fun rememberDragDropState(
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit = {}
): DragDropState {
    return remember { DragDropState(onMove, onDragEnd) }
}

@Composable
fun Modifier.dragAndDropItem(
    index: Int,
    state: DragDropState
): Modifier {
    val currentIndex by rememberUpdatedState(index)

    val isDragging = state.draggingItemIndex == currentIndex

    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (isDragging) 1.1f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isDragging) 0.9f else 1f, label = "alpha")

    return this
        .onGloballyPositioned { coordinates ->
            state.onPositioned(currentIndex, coordinates.boundsInWindow())
        }
        .graphicsLayer {
            translationX = if (isDragging) state.dragOffset.x else 0f
            translationY = if (isDragging) state.dragOffset.y else 0f
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            shadowElevation = if (isDragging) 10f else 0f
        }
        .zIndex(if (isDragging) 1f else 0f)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.onDragStart(currentIndex)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    state.onDrag(dragAmount)
                },
                onDragEnd = { state.onDragCancel() },
                onDragCancel = { state.onDragCancel() }
            )
        }
}