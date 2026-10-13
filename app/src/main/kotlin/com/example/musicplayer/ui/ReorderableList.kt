package com.example.musicplayer.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

/**
 * A list whose rows are reordered by dragging a handle. [row] receives the index, the item and a modifier to put on the
 * handle. The move is committed on drop, so the dragged row keeps its place in the list (and its gesture) until then.
 */
@Composable
fun <T> ReorderableList(
    items: List<T>,
    key: (index: Int, item: T) -> Any,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    row: @Composable (index: Int, item: T, handle: Modifier) -> Unit,
) {
    val listState = rememberLazyListState()
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    // A change from elsewhere can drop the dragged row mid-gesture without a cancel callback.
    LaunchedEffect(items) {
        draggingIndex = null
        dragOffset = 0f
    }
    val lastIndex by rememberUpdatedState(items.lastIndex)
    val move by rememberUpdatedState(onMove)
    LazyColumn(modifier.fillMaxWidth(), state = listState) {
        itemsIndexed(items, key = key) { index, item ->
            val isDragging = draggingIndex == index
            val currentIndex by rememberUpdatedState(index)
            val handle = Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        draggingIndex = currentIndex
                        dragOffset = 0f
                    },
                    onDrag = { change, delta ->
                        change.consume()
                        dragOffset += delta.y
                    },
                    onDragEnd = {
                        val from = draggingIndex
                        val rowHeight = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == from }?.size
                        if (from != null && rowHeight != null && rowHeight > 0) {
                            move(from, dragTargetIndex(from, dragOffset, rowHeight, lastIndex))
                        }
                        draggingIndex = null
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        draggingIndex = null
                        dragOffset = 0f
                    },
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffset else 0f },
            ) { row(index, item, handle) }
        }
    }
}
