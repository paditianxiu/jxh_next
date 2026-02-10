package me.padi.jxh.core.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ListNumberPicker(
    data: List<T>,
    selectIndex: Int,
    visibleCount: Int,
    modifier: Modifier = Modifier,
    onSelect: (index: Int, item: T) -> Unit,
    content: @Composable (item: T) -> Unit,
) {
    require(visibleCount % 2 == 1)

    BoxWithConstraints(modifier = modifier) {
        val itemHeight = maxHeight / visibleCount
        val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
        val half = visibleCount / 2

        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = selectIndex
        )
        val scope = rememberCoroutineScope()

        // 修复 1: 当外部 selectIndex 改变（如天数从31修正为30）时，强制列表滚动同步
        LaunchedEffect(selectIndex) {
            if (listState.firstVisibleItemIndex != selectIndex && !listState.isScrollInProgress) {
                listState.scrollToItem(selectIndex)
            }
        }

        // 修复 2: 绑定 data 作为 key。当月份改变导致 data.size 变动时，重新计算派生状态
        val selectedIndex by remember(data) {
            derivedStateOf {
                val firstIndex = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset

                val index = if (offset > itemHeightPx / 2) firstIndex + 1 else firstIndex
                index.coerceIn(0, data.lastIndex)
            }
        }

        LaunchedEffect(selectedIndex) {
            // 修复 3: 确保在索引合法的情况下回调，避免数据切换瞬间越界
            if (selectedIndex in data.indices) {
                onSelect(selectedIndex, data[selectedIndex])
            }
        }

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(half) {
                Spacer(Modifier.height(itemHeight))
            }

            // 修复 4: 为 items 增加 key。这让 LazyColumn 在数据量变动（28->31）时能更精确地处理布局逻辑
            items(
                count = data.size,
                key = { index -> "${data[index]}_$index" }
            ) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(8.dp)) // 稍微减小圆角，避免点击范围边缘重叠
                        .clickable {
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    content(data[index])
                }
            }

            items(half) {
                Spacer(Modifier.height(itemHeight))
            }
        }
    }
}