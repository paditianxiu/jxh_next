package me.padi.jxh.core.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.flow.distinctUntilChanged
import me.padi.jxh.R
import me.padi.jxh.Screen
import me.padi.jxh.core.model.NewsViewModel
import me.padi.jxh.data.repository.NewsData
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun NewsPage(backStack: MutableList<NavKey>) {
    val viewModel: NewsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()


    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = uiState.newsList.size
                if (lastVisibleIndex != null && lastVisibleIndex >= totalItems - 3 && uiState.hasMorePages && !uiState.isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.newsList.isEmpty() && !uiState.isLoading && uiState.error == null) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无新闻", color = MiuixTheme.colorScheme.onSurface
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .overScrollVertical(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.newsList,
                ) { news ->
                    NewsItem(
                        news
                    ) {
                        backStack.add(Screen.NewsDetail(news.url))
                    }
                }

                if (uiState.isLoading && uiState.newsList.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            InfiniteProgressIndicator(
                                modifier = Modifier.width(24.dp),
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading && uiState.newsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                InfiniteProgressIndicator(
                    modifier = Modifier.width(24.dp),
                )
            }
        }

        if (uiState.error != null && uiState.newsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载失败: ${uiState.error}", color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NewsItem(
    news: NewsData,
    imageLoadEnabled: Boolean = true,
    maxLines: Int = 2,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = news.time, color = MiuixTheme.colorScheme.onSurface, fontSize = 14.sp
                )
            },
            trailingContent = {
                if (imageLoadEnabled && !news.img.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(news.img)
                            .crossfade(true).build(),
                        contentDescription = "新闻图片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(90.dp)
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(8.dp)),
                        placeholder = painterResource(id = R.drawable.ic_loading_placeholder_horizontal),
                        error = painterResource(id = R.drawable.ic_loading_placeholder_horizontal)
                    )
                }
            })
    }
}