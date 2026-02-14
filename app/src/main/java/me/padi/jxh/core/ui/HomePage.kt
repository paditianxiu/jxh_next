package me.padi.jxh.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NordicWalking
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Token
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.padi.jxh.Screen
import me.padi.jxh.core.model.LoginViewModel
import me.padi.jxh.data.repository.ClassParams
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun HomePage(backStack: MutableList<NavKey>) {
    val scrollBehavior = MiuixScrollBehavior()

    val items: List<NavigationItem> = listOf(
        NavigationItem("江小航", MiuixIcons.VerticalSplit),
        NavigationItem("设置", MiuixIcons.Settings)
    )
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("江小航") }

    val pagerState = rememberPagerState(
        initialPage = selectedIndex, pageCount = { items.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        selectedIndex = pagerState.currentPage
        title = items[pagerState.currentPage].label
    }

    val viewModel: LoginViewModel = koinViewModel()

    Scaffold(topBar = {
        TopAppBar(scrollBehavior = scrollBehavior, title = title, actions = {
            ListPopup {
                viewModel.logout()
                backStack.clear()
                backStack.add(Screen.Login)
            }
            Spacer(Modifier.width(4.dp))
        })
    }, bottomBar = {
        NavigationBar(
            items = items, selected = selectedIndex, onClick = {
                selectedIndex = it
                title = items[it].label
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            }
        )
    }) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> HomeMainPage(backStack)
                1 -> SettingsPage(backStack)
            }

        }
    }
}

@Composable
fun HomeMainPage(backStack: MutableList<NavKey>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .padding(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            SmallTitle(
                text = "美丽江航", insideMargin = PaddingValues(8.dp, 0.dp)
            )
            Spacer(Modifier.height(8.dp))
            BannerCarouselWidget(
                modifier = Modifier.fillMaxWidth(), banners = listOf(
                    BannerModel(
                        "https://jxh.karpov.cn/public/banner/3280e756451e9c168992ed4305ac549.jpg",
                        "江航"
                    ), BannerModel(
                        "https://jxh.karpov.cn/public/banner/8440ab0e003cc31e7838cb675266ddb.jpg",
                        "江航"
                    ), BannerModel(
                        "https://jxh.karpov.cn/public/banner/293f398ef20e91c491bfa4ff9ad7fef6.jpg",
                        "江航"
                    )
                )
            )
            Spacer(Modifier.height(8.dp))
            SmallTitle(
                text = "学习功能", insideMargin = PaddingValues(8.dp, 0.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StudyCard(
                    Icons.Default.Scoreboard,
                    "考试成绩",
                    "这学期成绩考的怎么样啊，嘻嘻",
                    Modifier.weight(1f)
                ) {
                    backStack.add(Screen.Score)
                }
                Spacer(Modifier.width(16.dp))
                StudyCard(
                    Icons.Default.Token, "个人课表", "今天上什么课，我知道", Modifier.weight(1f)
                ) {
                    backStack.add(Screen.Course(ClassParams()))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StudyCard(
                    Icons.Default.Category,
                    "班级课表",
                    "用来查询其他班级的课程安排",
                    Modifier.weight(1f)
                ) {
                    backStack.add(Screen.ClassList)
                }
            }
            Spacer(Modifier.height(8.dp))
            SmallTitle(
                text = "校园工具", insideMargin = PaddingValues(8.dp, 0.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StudyCard(
                    Icons.Default.Support, "校园生活", "校园生活工具便捷跳转", Modifier.weight(1f)
                ) {
                    backStack.add(Screen.CampusLife)
                }

                Spacer(Modifier.width(16.dp))
                StudyCard(
                    Icons.Default.Map, "江航地图", "学校地图概况，一览众山小", Modifier.weight(1f)
                ) {
                    backStack.add(Screen.Map)
                }
            }

        }
    }
}

@Composable
fun SettingsPage(backStack: MutableList<NavKey>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .padding(16.dp)
    ) {
        item {
            Card {
                SuperArrow(startAction = {
                    Icon(Icons.Default.CalendarMonth, null)
                }, title = "课程表设置", onClick = {
                    backStack.add(Screen.CourseSetting)
                })
            }

            Spacer(Modifier.height(8.dp))

            Card {
                SuperArrow(startAction = {
                    Icon(Icons.Default.Egg, null)
                }, title = "彩蛋", onClick = {
                    backStack.add(Screen.Egg)
                })
                SuperArrow(startAction = {
                    Icon(Icons.Default.NordicWalking, null)
                }, title = "关于", onClick = {
                    backStack.add(Screen.About)
                })
            }
        }
    }
}

data class BannerModel(
    val imageUrl: String, val contentDescription: String
)

@Composable
fun BannerCarouselWidget(
    banners: List<BannerModel>, modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = {
        banners.size
    })
    Card(
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter, modifier = modifier
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 8.dp,
                verticalAlignment = Alignment.Top,
            ) { page ->
                BannerWidget(
                    imageUrl = banners[page].imageUrl,
                    contentDescription = banners[page].contentDescription
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) MiuixTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BannerWidget(
    imageUrl: String, contentDescription: String, modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}


@Composable
private fun ListPopup(
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.TopEnd,
    onClick: (String) -> Unit
) {
    val showTopPopup = remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = { showTopPopup.value = true },
        holdDownState = showTopPopup.value
    ) {
        Icon(
            imageVector = MiuixIcons.More,
            contentDescription = "更多",
            tint = MiuixTheme.colorScheme.onBackground
        )
    }

    WindowListPopup(
        show = showTopPopup,
        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
        alignment = alignment,
        onDismissRequest = { showTopPopup.value = false }) {
        ListPopupColumn {
            val homeOptions = remember {
                val options = mutableListOf(
                    "退出登录"
                )
                options
            }

            homeOptions.forEachIndexed { idx, reason ->
                MyDropdownItem(
                    title = reason,
                    showTopPopup = showTopPopup,
                    optionSize = homeOptions.size,
                    index = idx,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun MyDropdownItem(
    title: String = "",
    showTopPopup: MutableState<Boolean>,
    optionSize: Int,
    index: Int,
    onClick: (String) -> Unit
) {
    DropdownItem(
        text = title, optionSize = optionSize, index = index, onSelectedIndexChange = {
            onClick(title)
            showTopPopup.value = false
        })
}

@Composable
fun DropdownItem(
    text: String,
    optionSize: Int,
    index: Int,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onSelectedIndexChange: (Int) -> Unit
) {
    val currentOnSelectedIndexChange = rememberUpdatedState(onSelectedIndexChange)
    val additionalTopPadding = if (index == 0) 20f.dp else 12f.dp
    val additionalBottomPadding = if (index == optionSize - 1) 20f.dp else 12f.dp

    Row(
        modifier = Modifier
            .clickable { currentOnSelectedIndexChange.value(index) }
            .background(dropdownColors.containerColor)
            .padding(horizontal = 20.dp)
            .padding(
                top = additionalTopPadding, bottom = additionalBottomPadding
            ), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            fontSize = MiuixTheme.textStyles.body1.fontSize,
            fontWeight = FontWeight.Medium,
            color = dropdownColors.contentColor,
        )
    }
}


@Composable
fun StudyCard(
    icon: ImageVector,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.then(modifier),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = onClick,
        insideMargin = PaddingValues(16.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MiuixTheme.colorScheme.primary
        )

        Text(
            color = MiuixTheme.colorScheme.onSurface,
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            text = summary,
            style = MiuixTheme.textStyles.footnote2,
            minLines = 2,
            maxLines = 2
        )
    }
}
