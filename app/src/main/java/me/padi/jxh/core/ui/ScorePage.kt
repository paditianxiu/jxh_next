package me.padi.jxh.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import me.padi.jxh.core.model.ScoreViewModel
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.ScoreItem
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun ScorePage(backStack: MutableList<NavKey>) {
    val viewModel: ScoreViewModel = koinViewModel()
    val scoreState by viewModel.scoreState.collectAsState()
    val scrollBehavior = MiuixScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.fetchScore()
    }


    var tabs by remember { mutableStateOf(listOf("全部")) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var filteredScores by remember { mutableStateOf<List<ScoreItem>>(emptyList()) }

    LaunchedEffect(scoreState, selectedTabIndex) {
        if (scoreState is NetworkState.Success) {
            val scores = scoreState.getOrNull()
            if (scores != null) {
                val semesters = scores.map { it.semester }
                val newTabs = listOf("全部") + semesters.distinct()
                if (newTabs != tabs) {
                    tabs = newTabs
                }
                val selectedSemester = if (selectedTabIndex == 0) {
                    null
                } else {
                    tabs[selectedTabIndex]
                }

                filteredScores = if (selectedSemester == null) {
                    scores
                } else {
                    scores.filter { it.semester == selectedSemester }
                }
            }
        } else {
            filteredScores = emptyList()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, title = "考试成绩", navigationIcon = {
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        backStack.removeAt(backStack.lastIndex)
                    }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            }, actions = {
                IconButton(onClick = { }) {
                    Icon(MiuixIcons.More, contentDescription = "更多")
                }
                Spacer(Modifier.width(4.dp))
            })
        }) { paddingValues ->

        when (val state = scoreState) {
            is NetworkState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    InfiniteProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            is NetworkState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(16.dp),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding()
                    )
                ) {
                    item {
                        SuperDropdown(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            title = "按学期筛选",
                            items = tabs,
                            selectedIndex = selectedTabIndex,
                            onSelectedIndexChange = { selectedTabIndex = it })
                    }
                    items(filteredScores) { score ->
                        ScoreCard(score)
                    }
                }
            }

            is NetworkState.Error -> {
                ErrorPage(
                    message = state.message, onRetry = { viewModel.fetchScore() })
            }

            else -> {

            }
        }


    }
}


@Composable
fun ScoreCard(score: ScoreItem) {
    Spacer(Modifier.height(8.dp))
    SmallTitle(
        text = score.teacherName, insideMargin = PaddingValues(
            8.dp, 0.dp
        )

    )
    Card(
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        BasicComponent(
            title = score.courseName, summary = score.courseType, endActions = {
                Text(
                    text = score.totalScore, color = MiuixTheme.colorScheme.primary
                )
            })


    }
}