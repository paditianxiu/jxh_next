package me.padi.jxh.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import me.padi.jxh.Screen
import me.padi.jxh.core.model.ClassListViewModel
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.ProcessedClassInfo
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClassListPage(
    backStack: MutableList<NavKey>
) {
    val viewModel: ClassListViewModel = koinViewModel()
    val filteredClassListState by viewModel.filteredClassList.collectAsState()
    val searchText by viewModel.searchText.collectAsState()

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "班级列表", scrollBehavior = scrollBehavior, navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp), onClick = {
                        backStack.removeAt(backStack.lastIndex)
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                })
        },
    ) { innerPadding ->
        when (val state = filteredClassListState) {
            is NetworkState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            is NetworkState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = searchText,
                            onValueChange = { viewModel.updateSearchText(it) },
                            label = "筛选（班级/专业/学院）",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    state.data.let { list ->
                        if (list.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "没有找到匹配的班级",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                items(list) { item ->
                                    ClassInfoCard(item) {
                                        backStack.add(
                                            Screen.Course(
                                                item.params
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is NetworkState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "加载失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassInfoCard(
    classInfo: ProcessedClassInfo, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick
    ) {
        Text(
            classInfo.major,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = MiuixTheme.colorScheme.onBackgroundVariant,
            textAlign = TextAlign.Center,
            style = MiuixTheme.textStyles.subtitle
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClassInfoRow(
                label = "学院", value = classInfo.college
            )

            ClassInfoRow(
                label = "班级", value = classInfo.className
            )

            ClassInfoRow(
                label = "校区", value = classInfo.campus
            )

            ClassInfoRow(
                label = "学生人数", value = classInfo.studentCount, isImportant = true
            )

            Spacer(Modifier.height(16.dp))

        }
    }
}

@Composable
private fun ClassInfoRow(
    label: String, value: String, isImportant: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = value, style = if (isImportant) {
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.bodyMedium
            }, color = if (isImportant) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.onSurface
            }
        )
    }
}