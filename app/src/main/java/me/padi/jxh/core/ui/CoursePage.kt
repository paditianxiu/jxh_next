package me.padi.jxh.core.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import me.padi.jxh.core.model.CourseViewModel
import org.json.JSONArray
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.CheckboxLocation
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.WindowBottomSheet
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.VerticalSplit
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

// 提取行高为常量
private val ROW_HEIGHT = 80.dp

// 周数选择相关常量
private val MAX_WEEKS = 20
private val MIN_WEEKS = 1


@Composable
fun CoursePage(
    initialWeek: Int, backStack: MutableList<NavKey>
) {
    val viewModel: CourseViewModel = koinViewModel()
    val currentWeek = remember { mutableStateOf(initialWeek) }

    val courseState by viewModel.courseState.collectAsState()

    val showLoading = remember { mutableStateOf(true) }

    LaunchedEffect(courseState) {
        showLoading.value = courseState.isLoading()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchCourse()
    }

    val table = remember(courseState.getOrNull(), currentWeek.value) {
        parseCourseTable(courseState.getOrNull() ?: "", currentWeek.value)
    }
    val weeks = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            SmallTopAppBar(title = "课程表", navigationIcon = {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = {
                    if (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }) {
                    Icon(MiuixIcons.VerticalSplit, contentDescription = "返回")
                }
            }, actions = {
                IconButton(onClick = { }) {
                    Icon(MiuixIcons.More, contentDescription = "更多")
                }
                Spacer(Modifier.width(4.dp))
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {


            // 显示当前周数信息
            WeekInfoBar(
                currentWeek = currentWeek.value,
                onWeekChange = { week -> currentWeek.value = week },
                maxWeeks = MAX_WEEKS
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 周数标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.width(50.dp), contentAlignment = Alignment.Center
                ) {
                    Text("节次", fontSize = 12.sp, color = Color.Gray)
                }

                val weekWidth = calculateWeekItemWidth()
                weeks.forEach { week ->
                    Box(
                        modifier = Modifier.width(weekWidth), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = week,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CoursePage(table)

            WindowDialog(
                title = "提示",
                summary = "课表正在极速获取中...",
                show = showLoading,
                onDismissRequest = { }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InfiniteProgressIndicator(size = 20.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "加载中...", color = MiuixTheme.colorScheme.onSurfaceContainerVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WeekInfoBar(
    currentWeek: Int, onWeekChange: (Int) -> Unit, maxWeeks: Int = MAX_WEEKS
) {
    val showWeekSelector = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一周按钮
        IconButton(
            onClick = {
                if (currentWeek > MIN_WEEKS) {
                    onWeekChange(currentWeek - 1)
                }
            }, enabled = currentWeek > MIN_WEEKS, modifier = Modifier.width(48.dp)
        ) {
            Icon(MiuixIcons.ChevronBackward, contentDescription = "上一周")
        }

        // 当前周数显示和选择器
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE3F2FD))
                .clickable { showWeekSelector.value = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "第 $currentWeek 周",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2)
            )
        }

        // 下一周按钮
        IconButton(
            onClick = {
                if (currentWeek < maxWeeks) {
                    onWeekChange(currentWeek + 1)
                }
            }, enabled = currentWeek < maxWeeks, modifier = Modifier.width(48.dp)
        ) {
            Icon(MiuixIcons.ChevronForward, contentDescription = "下一周")
        }
    }


    WindowBottomSheet(
        onDismissRequest = { showWeekSelector.value = false },
        title = "选择周数",
        show = showWeekSelector,
    ) {
        WeekSelector(
            currentWeek = currentWeek, maxWeeks = maxWeeks, onWeekSelected = { week ->
                onWeekChange(week)
                showWeekSelector.value = false
            })
    }


}

@Composable
fun WeekSelector(
    currentWeek: Int, maxWeeks: Int, onWeekSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        for (week in MIN_WEEKS..maxWeeks) {
            val isSelected = week == currentWeek
            SuperCheckbox(
                title = "第 ${week} 周",
                checked = isSelected,
                onCheckedChange = {
                    if (it) onWeekSelected(week)
                },
                checkboxLocation = CheckboxLocation.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun WeekItem(
    week: Int, isSelected: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "第 $week 周",
            fontSize = 16.sp,
            color = if (isSelected) Color(0xFF1976D2) else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CoursePage(table: List<List<List<CourseCell>>>) {
    val times = listOf(
        TimeSlot(1, "09:00", "09:40"),
        TimeSlot(2, "09:45", "10:25"),
        TimeSlot(3, "10:35", "11:15"),
        TimeSlot(4, "11:20", "12:00"),
        TimeSlot(5, "13:30", "14:10"),
        TimeSlot(6, "14:15", "14:55"),
        TimeSlot(7, "15:05", "15:45"),
        TimeSlot(8, "15:50", "16:30")
    )

    val selectedCourse = remember { mutableStateOf<CourseCell?>(null) }
    val scrollState = rememberScrollState()

    // 整个课程表的高度
    val totalHeight = ROW_HEIGHT * 8

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .verticalScroll(scrollState)  // 添加垂直滚动
    ) {
        // 先绘制所有行的时间信息
        repeat(8) { timeSlotIndex ->
            TimeRow(
                timeInfo = times[timeSlotIndex], rowIndex = timeSlotIndex
            )
        }

        // 再绘制所有课程（使用绝对定位）
        for (timeSlot in 0 until 8) {
            for (day in 0 until 7) {
                if (table.isNotEmpty()) {
                    val courses = table[timeSlot][day]
                    courses.forEach { course ->
                        CourseItem(
                            course = course,
                            day = day,
                            onCourseClick = { selectedCourse.value = it })
                    }
                }
            }
        }
    }

    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(selectedCourse.value) {
        showDialog.value = selectedCourse.value != null
    }

    if (selectedCourse.value != null) {
        WindowDialog(
            onDismissRequest = {
                selectedCourse.value = null
                showDialog.value = false
            },
            title = selectedCourse.value!!.name,
            show = showDialog,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailItem(title = "教室", value = selectedCourse.value!!.room)
                DetailItem(
                    title = "节次",
                    value = "${selectedCourse.value!!.start}-${selectedCourse.value!!.end}节"
                )
                DetailItem(
                    title = "上课周次",
                    value = selectedCourse.value!!.weeks.sorted().joinToString(", ")
                )
                DetailItem(title = "老师", value = selectedCourse.value!!.teacher)
            }
            Spacer(Modifier.height(16.dp))
            TextButton(
                text = "关闭", onClick = {
                    selectedCourse.value = null
                    showDialog.value = false
                }, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimeRow(
    timeInfo: TimeSlot, rowIndex: Int
) {
    val weekWidth = calculateWeekItemWidth()
    val rowTop = ROW_HEIGHT * rowIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .absoluteOffset(y = rowTop)
            .height(ROW_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 左侧时间信息
        Column(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "第${timeInfo.id}节", fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
            Text(
                text = timeInfo.startTime, fontSize = 10.sp, color = Color.Gray
            )
            Text(
                text = timeInfo.endTime, fontSize = 10.sp, color = Color.Gray
            )
        }

        // 一周的课程（7天）- 这里只绘制空的列作为背景
        repeat(7) { _ ->
            Box(
                modifier = Modifier
                    .width(weekWidth)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun CourseItem(
    course: CourseCell, day: Int,  // 星期几，0-6
    onCourseClick: (CourseCell) -> Unit
) {
    val weekWidth = calculateWeekItemWidth()

    // 课程的开始行位置（基于0）
    val startRow = course.start - 1
    // 课程跨越的行数
    val span = course.end - course.start + 1

    // 计算位置
    val x = 50.dp + weekWidth * day + 8.dp * (day + 1)  // 50dp是时间列宽度，8dp是间距
    val y = ROW_HEIGHT * startRow
    val height = ROW_HEIGHT * span

    Box(
        modifier = Modifier
            .absoluteOffset(x = x, y = y)
            .width(weekWidth)
            .height(height - 10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(getCourseColor(course.name))
            .clickable { onCourseClick(course) }
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Column {
            Text(
                text = course.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.room,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.9f),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            // 如果是跨节课程，显示节数信息
            if (span > 1) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.teacher,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun DetailItem(
    title: String, value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$title:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value, fontSize = 14.sp, modifier = Modifier.weight(1f)
        )
    }
}

// 根据课程名称生成颜色（简单的hash算法）
fun getCourseColor(courseName: String): Color {
    val colors = listOf(
        Color(0xFF4CAF50), // 绿色
        Color(0xFF4CAF50), // 绿色
        Color(0xFF2196F3), // 蓝色
        Color(0xFF9C27B0), // 紫色
        Color(0xFFF44336), // 红色
        Color(0xFFFF9800), // 橙色
        Color(0xFF607D8B), // 蓝色灰色
        Color(0xFF795548), // 棕色
        Color(0xFF00BCD4), // 青色
        Color(0xFF3F51B5), // 靛蓝
        Color(0xFFE91E63)  // 粉色
    )

    val hash = courseName.hashCode()
    val index = abs(hash) % colors.size
    return colors[index]
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun calculateWeekItemWidth(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    // 计算：屏幕宽度 - 左右padding(32dp) - 左侧时间列(50dp) - 列间距(8dp * 6 = 48dp)
    val totalWidthDp = screenWidthDp - 32 - 50 - 48
    val itemWidthDp = totalWidthDp / 7

    return with(LocalDensity.current) {
        itemWidthDp.dp
    }
}

data class TimeSlot(
    val id: Int, val startTime: String, val endTime: String
)

data class CourseCell(
    val name: String, val room: String, var teacher: String, val weeks: List<Int>,  // 整数列表
    val start: Int,  // 开始节次
    val end: Int     // 结束节次
)

fun parseCourseTable(json: String, currentWeek: Int): List<List<List<CourseCell>>> {
    runCatching {
        val root = JSONObject(json)
        val kbArray = root.optJSONArray("kbList") ?: JSONArray()

        // 8个时间段，7天，每格可能有多个课程
        val table = MutableList(8) {
            MutableList(7) {
                mutableListOf<CourseCell>()
            }
        }

        for (i in 0 until kbArray.length()) {
            val item = kbArray.optJSONObject(i) ?: continue

            val day = item.optString("xqj").toIntOrNull()?.minus(1) ?: continue
            val jcs = item.optString("jcs")
            val name = item.optString("kcmc")
            val room = item.optString("cdbh")
            val weeksStr = item.optString("zcd")
            val teacher = item.optString("xm")


            // 解析周数字符串为整数列表
            val weeks = parseWeeks(weeksStr)

            // 检查当前周是否在课程周数内
            if (!weeks.contains(currentWeek)) continue

            val range = jcs.split("-")
            if (range.size != 2) continue

            val start = range[0].toIntOrNull() ?: continue
            val end = range[1].toIntOrNull() ?: continue

            // 将课程添加到它开始的那一节
            val startIndex = start - 1
            if (startIndex in 0..7 && day in 0..6) {
                table[startIndex][day].add(
                    CourseCell(name, room,teacher, weeks, start, end)
                )
            }
        }

        return table
    }.getOrElse {
        return emptyList()
    }
}

// 解析周数字符串为整数列表
fun parseWeeks(weeksStr: String): List<Int> {
    val result = mutableListOf<Int>()

    // 按逗号分割不同的周数段
    val segments = weeksStr.split(",").map { it.trim() }

    for (segment in segments) {
        // 移除可能的"周"字
        val cleanSegment = segment.replace("周", "")

        if (cleanSegment.contains("-")) {
            // 处理范围，如"3-10周"或"3-10"
            val rangeParts = cleanSegment.split("-")
            if (rangeParts.size == 2) {
                val start = rangeParts[0].toIntOrNull()
                val end = rangeParts[1].toIntOrNull()
                if (start != null && end != null) {
                    result.addAll(start..end)
                }
            }
        } else if (cleanSegment.contains("(")) {
            // 处理特殊格式，如"4-6周(双)" - 双周
            val mainPart = cleanSegment.substringBefore("(")
            val flag = cleanSegment.substringAfter("(").substringBefore(")")

            if (mainPart.contains("-")) {
                val rangeParts = mainPart.split("-")
                if (rangeParts.size == 2) {
                    val start = rangeParts[0].toIntOrNull()
                    val end = rangeParts[1].toIntOrNull()
                    if (start != null && end != null) {
                        for (week in start..end) {
                            if (flag == "双" && week % 2 == 0) {
                                result.add(week)
                            } else if (flag == "单" && week % 2 == 1) {
                                result.add(week)
                            }
                        }
                    }
                }
            }
        } else {
            // 处理单个周数
            val week = cleanSegment.toIntOrNull()
            if (week != null) {
                result.add(week)
            }
        }
    }

    return result.distinct().sorted()
}

// 保留原来的 matchWeek 函数作为兼容
fun matchWeek(zcd: String, week: Int): Boolean {
    val weeks = parseWeeks(zcd)
    return weeks.contains(week)
}