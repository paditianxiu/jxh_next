package me.padi.jxh.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import me.padi.jxh.core.common.DateStorage
import me.padi.jxh.core.components.ListNumberPicker
import me.padi.jxh.core.model.ClassListViewModel
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.LocalWindowDialogState
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CourseSetting(
    backStack: MutableList<NavKey>
) {
    val scrollBehavior = MiuixScrollBehavior()
    val showStartDialog = remember { mutableStateOf(false) }
    val showYearSemesterDialog = remember { mutableStateOf(false) }

    // 基础设置：开始上课日期
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    var startClassDate by remember {
        mutableStateOf(
            DateStorage.getDate().format(formatter)
        )
    }

    // 课表配置状态
    var isSame by remember { mutableStateOf(DateStorage.getIsSame()) } // 建议从 MMKV 读取初始值
    val currentType = remember { mutableStateOf(DateStorage.Type.PERSONAL) }

    // 个人和班级的数据状态
    var personalYear by remember {
        mutableStateOf(
            DateStorage.getYear(
                DateStorage.Type.PERSONAL
            )
        )
    }
    var personalSemester by remember {
        mutableStateOf(
            DateStorage.getSemester(
                DateStorage.Type.PERSONAL
            )
        )
    }
    var classYear by remember { mutableStateOf(DateStorage.getYear(DateStorage.Type.CLASS)) }
    var classSemester by remember {
        mutableStateOf(
            DateStorage.getSemester(
                DateStorage.Type.CLASS
            )
        )
    }

    fun getSemesterText(s: Int) = if (s == 3) "第一学期" else "第二学期"


    val viewModel: ClassListViewModel = koinViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "课程表设置", scrollBehavior = scrollBehavior, navigationIcon = {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = {
                        if (backStack.isNotEmpty()) backStack.removeAt(backStack.lastIndex)
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                })
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(16.dp),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
        ) {
            item {
                SmallTitle("基础设置", insideMargin = PaddingValues(10.dp, 8.dp))

                SuperArrow(
                    title = "开始上课的时间",
                    summary = "课表开始的第一天，不是开学时间",
                    onClick = { showStartDialog.value = true },
                    endActions = {
                        Text(startClassDate, color = MiuixTheme.colorScheme.primary)
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )

                SmallTitle("课表配置", insideMargin = PaddingValues(10.dp, 8.dp))

                SuperSwitch(
                    title = "保持一致",
                    summary = "班级课表和个人课表保持一致",
                    checked = isSame,
                    onCheckedChange = {
                        isSame = it
                        DateStorage.saveIsSame(it)
                        if (it) {
                            // 开启同步时，立即将个人配置同步给班级
                            classYear = personalYear
                            classSemester = personalSemester
                            DateStorage.saveYear(
                                DateStorage.Type.CLASS, classYear
                            )
                            DateStorage.saveSemester(
                                DateStorage.Type.CLASS, classSemester
                            )
                            viewModel.updateDate(classYear.toString(), classSemester.toString())
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )

                Spacer(Modifier.height(8.dp))

                // 设置个人课表
                SuperArrow(
                    title = "设置个人课表",
                    summary = "${personalYear}学年 | ${getSemesterText(personalSemester)}",
                    onClick = {
                        currentType.value = DateStorage.Type.PERSONAL
                        showYearSemesterDialog.value = true
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )

                Spacer(Modifier.height(8.dp))

                // 设置班级课表
                SuperArrow(
                    title = "设置班级课表",
                    enabled = !isSame,
                    summary = if (isSame) "已与个人课表同步" else "${classYear}学年 | ${
                        getSemesterText(
                            classSemester
                        )
                    }",
                    onClick = {
                        currentType.value = DateStorage.Type.CLASS
                        showYearSemesterDialog.value = true
                        viewModel.updateDate(classYear.toString(), classSemester.toString())
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
            }
        }

        // --- 弹窗逻辑 ---

        // 1. 日期选择弹窗
        WindowDialog(
            title = "选择开始上课的时间",
            show = showStartDialog,
            onDismissRequest = { showStartDialog.value = false }) {
            DatePickerContent(
                nowDate = DateStorage.getDate()
            ) { localDate ->
                val newFormattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                DateStorage.saveDate(localDate)
                startClassDate = newFormattedDate
            }
        }

        // 2. 学年学期选择弹窗
        WindowDialog(
            title = if (currentType.value == DateStorage.Type.PERSONAL) "设置个人课表" else "设置班级课表",
            show = showYearSemesterDialog,
            onDismissRequest = { showYearSemesterDialog.value = false }) {
            val isPersonal = currentType.value == DateStorage.Type.PERSONAL
            YearSemesterPickerContent(
                initialYear = if (isPersonal) personalYear else classYear,
                initialSemester = if (isPersonal) personalSemester else classSemester,
                onConfirm = { year, semester ->
                    if (isPersonal) {
                        personalYear = year
                        personalSemester = semester
                        DateStorage.saveYear(DateStorage.Type.PERSONAL, year)
                        DateStorage.saveSemester(
                            DateStorage.Type.PERSONAL, semester
                        )
                        // 如果开启了同步，同时保存给 Class
                        if (isSame) {
                            classYear = year
                            classSemester = semester
                            DateStorage.saveYear(DateStorage.Type.CLASS, year)
                            DateStorage.saveSemester(
                                DateStorage.Type.CLASS, semester
                            )

                        }
                    } else {
                        classYear = year
                        classSemester = semester
                        DateStorage.saveYear(DateStorage.Type.CLASS, year)
                        DateStorage.saveSemester(
                            DateStorage.Type.CLASS, semester
                        )
                    }
                    viewModel.updateDate(year.toString(), semester.toString())
                })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearSemesterPickerContent(
    initialYear: Int, initialSemester: Int, onConfirm: (Int, Int) -> Unit
) {
    val dismiss = LocalWindowDialogState.current

    // 数据源
    val currentYear = LocalDate.now().year
    val yearData = remember { (currentYear - 9..currentYear + 1).toList() } // 近10年+明年
    val semesterData = remember { listOf(3, 12) }

    var tempYear by remember { mutableIntStateOf(initialYear) }
    var tempSemester by remember { mutableIntStateOf(initialSemester) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 学年选择器
            DatePickerItem(
                data = yearData,
                selected = tempYear,
                onSelect = { tempYear = it },
                label = "学年",
                modifier = Modifier.weight(1f)
            )

            // 学期选择器
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ListNumberPicker(
                    data = semesterData,
                    selectIndex = semesterData.indexOf(tempSemester).coerceAtLeast(0),
                    visibleCount = 3,
                    modifier = Modifier.fillMaxSize(),
                    onSelect = { _, item -> tempSemester = item }) { item ->
                    Text(
                        text = if (item == 3) "第一学期" else "第二学期",
                        color = if (item == tempSemester) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.secondaryVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (item == tempSemester) 20.sp else 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                modifier = Modifier.weight(1f), text = "取消", onClick = { dismiss.invoke() })
            Spacer(Modifier.width(16.dp))
            Button(
                modifier = Modifier.weight(1f), onClick = {
                    onConfirm(tempYear, tempSemester)
                    dismiss.invoke()
                }, colors = ButtonDefaults.buttonColorsPrimary()
            ) {
                Text("确定", color = MiuixTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerContent(nowDate: LocalDate, onConfirm: (LocalDate) -> Unit) {
    val dismiss = LocalWindowDialogState.current

    // 状态管理
    var selectYear by remember { mutableIntStateOf(nowDate.year) }
    var selectMonth by remember { mutableIntStateOf(nowDate.monthValue) }
    var selectDay by remember { mutableIntStateOf(nowDate.dayOfMonth) }

    // 数据源生成
    val yearData = remember { (nowDate.year - 5..nowDate.year + 5).toList() }
    val monthData = remember { (1..12).toList() }

    // 动态计算当前年月下的天数
    val dayData = remember(selectYear, selectMonth) {
        val daysInMonth = java.time.YearMonth.of(selectYear, selectMonth).lengthOfMonth()
        (1..daysInMonth).toList()
    }

    // 修正逻辑：如果从31号切到30号的月份，自动将选中日调低
    LaunchedEffect(dayData) {
        if (selectDay > dayData.size) {
            selectDay = dayData.size
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 年份
            DatePickerItem(
                data = yearData,
                selected = selectYear,
                onSelect = { selectYear = it },
                label = "年",
                modifier = Modifier.weight(1.2f)
            )

            // 月份
            DatePickerItem(
                data = monthData,
                selected = selectMonth,
                onSelect = { selectMonth = it },
                label = "月",
                modifier = Modifier.weight(1f)
            )

            // 日期
            DatePickerItem(
                data = dayData,
                selected = selectDay,
                onSelect = { selectDay = it },
                label = "日",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                modifier = Modifier.weight(1.0f), text = "关闭", onClick = { dismiss.invoke() })
            Spacer(Modifier.width(16.dp))
            Button(
                modifier = Modifier.weight(1.0f), onClick = {
                    val confirmedDate = LocalDate.of(selectYear, selectMonth, selectDay)
                    onConfirm(confirmedDate)
                    dismiss.invoke()
                }, colors = ButtonDefaults.buttonColorsPrimary()
            ) {
                Text("确定", color = MiuixTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))


    }
}

@Composable
fun <T> DatePickerItem(
    data: List<T>, selected: T, onSelect: (T) -> Unit, label: String, modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        ListNumberPicker(
            data = data,
            selectIndex = data.indexOf(selected).coerceAtLeast(0),
            visibleCount = 3,
            modifier = Modifier.fillMaxSize(),
            onSelect = { _, item -> onSelect(item) }) { item ->
            Text(
                text = "$item$label",
                color = if (item == selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.secondaryVariant,
                fontWeight = FontWeight.Bold,
                fontSize = if (item == selected) 22.sp else 16.sp
            )
        }
    }
}