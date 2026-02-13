package me.padi.jxh.core.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import me.padi.jxh.core.common.DateStorage
import me.padi.jxh.core.model.CourseViewModel
import me.padi.jxh.data.repository.ClassParams
import me.padi.jxh.data.repository.isEmpty
import org.json.JSONArray
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
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
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import java.time.LocalDate
import kotlin.math.abs

// 提取行高为常量
private val ROW_HEIGHT = 70.dp

// 周数选择相关常量
private val MAX_WEEKS = 20
private val MIN_WEEKS = 1

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CoursePage(
    params: ClassParams, backStack: MutableList<NavKey>
) {
    val viewModel: CourseViewModel = koinViewModel()
    val termStartDate = DateStorage.getDate()
    val initialCurrentWeek = calculateCurrentWeek(termStartDate)

    val currentWeek = remember { mutableStateOf(initialCurrentWeek) }
    val courseState by viewModel.courseState.collectAsState()
    val showLoading = remember { mutableStateOf(true) }

    LaunchedEffect(courseState) {
        showLoading.value = courseState.isLoading()
    }

    LaunchedEffect(Unit) {
        if (params.isEmpty()) {
            viewModel.fetchCourse()
        } else {
            viewModel.fetchClassCourse(params)
        }
    }

    // 解析所有课程数据
    val allCourses = remember(courseState) {
        parseAllCourses(courseState.getOrNull() ?: "")
    }
    val practiceList = remember(courseState) {
        if (params.isEmpty()) {
            emptyList()
        } else {
            parsePracticeList(courseState.getOrNull() ?: "")
        }
    }

    // 根据当前周筛选和排列课程
    val table = remember(allCourses, currentWeek.value) {
        arrangeCoursesByWeek(allCourses, currentWeek.value)
    }

    // 计算当前周的日期列表
    val weekDates = remember(currentWeek.value) {
        getWeekDates(currentWeek.value, termStartDate)
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                // 显示当前周数信息
                WeekInfoBar(
                    currentWeek = currentWeek.value,
                    onWeekChange = { week -> currentWeek.value = week },
                    maxWeeks = MAX_WEEKS
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 周数标题行
                WeekDateHeader(
                    currentWeek = currentWeek.value, weekDates = weekDates
                )

                Spacer(modifier = Modifier.height(8.dp))

                CourseContent(table, currentWeek.value)

                if (practiceList.isNotEmpty()) {
                    SmallTitle("实训课程(共${practiceList.size}门)")
                    Card(
                        modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.primaryVariant
                        ),
                        pressFeedbackType = PressFeedbackType.Sink,
                        showIndication = true,
                    ) {
                        practiceList.forEachIndexed { index, name ->
                            Text(
                                "${index + 1}.$name",
                                modifier = Modifier.padding(4.dp),
                                style = MiuixTheme.textStyles.subtitle,
                                color = MiuixTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
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
                            text = "加载中...",
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant
                        )
                    }
                }
            }
        }
    }
}

fun parsePracticeList(jsonString: String): List<String> {
    val result = mutableListOf<String>()
    try {
        val json = JSONObject(jsonString)
        val jsonArray = json.optJSONArray("sjkList")
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i)
                val name = item?.optString("qtkcgs", "")
                if (name?.isNotBlank() ?: false) {
                    result.add(name)
                }
            }

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateCurrentWeek(termStartDate: LocalDate): Int {
    val today = LocalDate.now()
    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(termStartDate, today)
    val currentWeek = (daysBetween / 7).toInt() + 1
    return maxOf(1, currentWeek)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekDateHeader(
    currentWeek: Int, weekDates: List<String>
) {
    val weekWidth = calculateWeekItemWidth()
    val weeks = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 第一行：星期
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.width(25.dp), contentAlignment = Alignment.Center
            ) {
                Text("抚州", fontSize = 12.sp, color = Color.Gray)
            }

            weeks.forEach { week ->
                Box(
                    modifier = Modifier.width(weekWidth), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = week,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val month = if (weekDates.isNotEmpty()) {
            val mondayDate = LocalDate.parse(weekDates[0])
            mondayDate.monthValue
        } else {
            LocalDate.now().monthValue
        }
        // 第二行：日期
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.width(25.dp), contentAlignment = Alignment.Center
            ) {
                Text("${month}月", fontSize = 12.sp, color = Color.LightGray)
            }

            weekDates.forEachIndexed { index, date ->
                Box(
                    modifier = Modifier.width(weekWidth), contentAlignment = Alignment.Center
                ) {
                    val formattedDate = formatDateForDisplay(date)
                    val dateColor = if (isToday(date)) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedDate,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = dateColor,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isToday(date)) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF1976D2))
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateForDisplay(dateStr: String): String {
    return try {
        if (isToday(dateStr)) {
            return "今天"
        }
        val date = LocalDate.parse(dateStr)
        val day = date.dayOfMonth
        "${day}日"
    } catch (_: Exception) {
        dateStr
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isToday(dateStr: String): Boolean {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        date == today
    } catch (_: Exception) {
        false
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

@RequiresApi(Build.VERSION_CODES.O)
fun getWeekDates(weekNumber: Int, termStartDate: LocalDate): List<String> {
    val currentWeekMonday = termStartDate.plusWeeks((weekNumber - 1).toLong())
    val weekDates = mutableListOf<String>()
    for (i in 0 until 7) {
        val date = currentWeekMonday.plusDays(i.toLong())
        weekDates.add(date.toString())
    }
    return weekDates
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
fun CourseContent(
    table: List<List<List<CourseCell>>>, currentWeek: Int
) {
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

    // 对每个时间段、每个星期的课程进行去重处理，只保留离今天最近的课程
    val filteredTable = remember(table, currentWeek) {
        table.map { timeSlot ->
            timeSlot.map { dayCourses ->
                if (dayCourses.size > 1) {
                    // 找出离今天最近的课程
                    val nearestCourse = findNearestCourse(dayCourses, currentWeek)
                    listOf(nearestCourse)
                } else {
                    dayCourses
                }
            }
        }
    }

    val selectedCourse = remember { mutableStateOf<CourseCell?>(null) }
    val scrollState = rememberScrollState()
    val totalHeight = ROW_HEIGHT * 8

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .verticalScroll(scrollState)
    ) {
        // 先绘制所有行的时间信息
        repeat(8) { timeSlotIndex ->
            TimeRow(
                timeInfo = times[timeSlotIndex], rowIndex = timeSlotIndex
            )
        }

        // 再绘制所有课程（使用过滤后的表格）
        for (timeSlot in 0 until 8) {
            for (day in 0 until 7) {
                if (filteredTable.isNotEmpty()) {
                    val courses = filteredTable[timeSlot][day]
                    courses.forEach { course ->
                        CourseItem(
                            course = course,
                            day = day,
                            currentWeek = currentWeek,
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


/**
 * 找出离当前周最近的课程
 * 优先级：
 * 1. 包含当前周的课程（本周课程）
 * 2. 离当前周最近的未来课程
 * 3. 离当前周最近的过去课程
 * 4. 按周数排序的第一个课程
 */
fun findNearestCourse(courses: List<CourseCell>, currentWeek: Int): CourseCell {
    if (courses.isEmpty()) return courses.first()

    // 1. 找出包含当前周的课程
    val currentWeekCourses = courses.filter { it.weeks.contains(currentWeek) }
    if (currentWeekCourses.isNotEmpty()) {
        // 如果有多个本周课程，按课程名称排序取第一个
        return currentWeekCourses.minByOrNull { it.name }!!
    }

    // 2. 找出离当前周最近的未来课程
    val futureCourses = courses.mapNotNull { course ->
        val futureWeeks = course.weeks.filter { it > currentWeek }
        if (futureWeeks.isNotEmpty()) {
            val nearestWeek = futureWeeks.minOrNull()
            course to nearestWeek
        } else {
            null
        }
    }.sortedBy { it.second } // 按最近的未来周排序

    if (futureCourses.isNotEmpty()) {
        return futureCourses.first().first
    }

    // 3. 找出离当前周最近的过去课程
    val pastCourses = courses.mapNotNull { course ->
        val pastWeeks = course.weeks.filter { it < currentWeek }
        if (pastWeeks.isNotEmpty()) {
            val nearestWeek = pastWeeks.maxOrNull() // 过去课程取最大的（最近的过去）
            course to nearestWeek
        } else {
            null
        }
    }.sortedByDescending { it.second } // 按最近的过去周排序

    if (pastCourses.isNotEmpty()) {
        return pastCourses.first().first
    }

    // 4. 按周数排序取第一个课程
    return courses.minByOrNull { it.weeks.minOrNull() ?: Int.MAX_VALUE } ?: courses.first()
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
                .width(25.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeInfo.id.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
            Text(
                text = timeInfo.startTime, fontSize = 8.sp, color = Color.Gray
            )
            Text(
                text = timeInfo.endTime, fontSize = 8.sp, color = Color.Gray
            )
        }

        // 一周的课程
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
    course: CourseCell, day: Int, currentWeek: Int, onCourseClick: (CourseCell) -> Unit
) {
    val weekWidth = calculateWeekItemWidth()

    // 判断是否为本周课程
    val isCurrentWeek = course.weeks.contains(currentWeek)

    // 课程的开始行位置
    val startRow = course.start - 1
    val span = course.end - course.start + 1
    val x = 25.dp + weekWidth * day + 8.dp * (day + 1)
    val y = ROW_HEIGHT * startRow
    val height = ROW_HEIGHT * span

    val backgroundColor = if (isCurrentWeek) getCourseColor(course.name) else Color.LightGray

    Box(
        modifier = Modifier
            .absoluteOffset(x = x, y = y)
            .width(weekWidth)
            .height(height - 10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onCourseClick(course) }
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Column {
            if (!isCurrentWeek) {
                Text(
                    "[非本周]",
                    fontSize = 8.sp,
                    color = Color.Yellow,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
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
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            if (span > 1) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.teacher,
                    fontSize = 8.sp,
                    color = Color.White,
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

fun getCourseColor(courseName: String): Color {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFFFF9800),
        Color(0xFF607D8B),
        Color(0xFF795548),
        Color(0xFF00BCD4),
        Color(0xFF3F51B5),
        Color(0xFFE91E63)
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
    val totalWidthDp = screenWidthDp - 32 - 25 - 48
    val itemWidthDp = totalWidthDp / 7
    return with(LocalDensity.current) {
        itemWidthDp.dp
    }
}

data class TimeSlot(
    val id: Int, val startTime: String, val endTime: String
)

data class CourseCell(
    val name: String,
    val room: String,
    var teacher: String,
    val weeks: List<Int>,
    val day: Int,        // 星期几，0-6
    val start: Int,
    val end: Int
)

// 解析所有课程数据
fun parseAllCourses(json: String): List<CourseCell> {
    val courses = mutableListOf<CourseCell>()

    runCatching {
        val root = JSONObject(json)
        val kbArray = root.optJSONArray("kbList") ?: JSONArray()

        for (i in 0 until kbArray.length()) {
            val item = kbArray.optJSONObject(i) ?: continue

            // 从"xqj"字段获取星期信息（1-7转换为0-6）
            val day = item.optString("xqj").toIntOrNull()?.minus(1) ?: continue
            val jcs = item.optString("jcs")
            val name = item.optString("kcmc")
            val room = item.optString("cdmc")
            val weeksStr = item.optString("zcd")
            val teacher = item.optString("xm")

            // 解析周数字符串
            val weeks = parseWeeks(weeksStr)

            // 解析节次范围
            val range = jcs.split("-")
            if (range.size != 2) continue

            val start = range[0].toIntOrNull() ?: continue
            val end = range[1].toIntOrNull() ?: continue

            courses.add(
                CourseCell(
                    name = name,
                    room = room,
                    teacher = teacher,
                    weeks = weeks,
                    day = day,
                    start = start,
                    end = end
                )
            )
        }
    }.getOrElse {
        // 解析出错时返回空列表
    }

    return courses
}

// 根据当前周排列课程
fun arrangeCoursesByWeek(
    allCourses: List<CourseCell>, currentWeek: Int
): List<List<List<CourseCell>>> {
    // 8个时间段，7天，每格可能有多个课程
    val table = MutableList(8) {
        MutableList(7) {
            mutableListOf<CourseCell>()
        }
    }

    // 将所有课程添加到表格中
    for (course in allCourses) {
        // 课程会显示在对应的星期和节次位置
        val startIndex = course.start - 1
        val day = course.day

        if (startIndex in 0..7 && day in 0..6) {
            table[startIndex][day].add(course)
        }
    }

    return table
}

// 解析周数字符串为整数列表
fun parseWeeks(weeksStr: String): List<Int> {
    val result = mutableListOf<Int>()
    val segments = weeksStr.split(",").map { it.trim() }

    for (segment in segments) {
        val cleanSegment = segment.replace("周", "")

        if (cleanSegment.contains("-")) {
            val rangeParts = cleanSegment.split("-")
            if (rangeParts.size == 2) {
                val start = rangeParts[0].toIntOrNull()
                val end = rangeParts[1].toIntOrNull()
                if (start != null && end != null) {
                    result.addAll(start..end)
                }
            }
        } else if (cleanSegment.contains("(")) {
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
            val week = cleanSegment.toIntOrNull()
            if (week != null) {
                result.add(week)
            }
        }
    }

    return result.distinct().sorted()
}