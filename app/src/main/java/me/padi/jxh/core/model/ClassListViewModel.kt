package me.padi.jxh.core.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import me.padi.jxh.core.common.DateStorage
import me.padi.jxh.core.common.DateStorage.Type
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.CourseRepository
import me.padi.jxh.data.repository.ProcessedClassInfo

@RequiresApi(Build.VERSION_CODES.O)
class ClassListViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    // 1. 搜索文本状态
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // 2. 请求参数状态 (学年, 学期)
    // 初始值直接从 DateStorage 读取
    private val _params = MutableStateFlow(
        DateStorage.getYear(Type.CLASS).toString() to DateStorage.getSemester(Type.CLASS).toString()
    )

    // 3. 原始数据流：当参数变化时，自动触发网络请求
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _classListState = _params.flatMapLatest { (year, semester) ->
        flow {
            emit(NetworkState.Loading)
            // 调用 repository 获取结果
            val result = courseRepository.fetchClassList(year, semester)
            emit(result)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkState.Idle
    )

    // 4. 最终展示流：结合原始数据和搜索文本进行过滤
    val filteredClassList: StateFlow<NetworkState<List<ProcessedClassInfo>>> =
        combine(_classListState, _searchText) { networkState, query ->
            when (networkState) {
                is NetworkState.Success -> {
                    val data = networkState.data
                    if (query.isBlank()) {
                        networkState
                    } else {
                        val filtered = data.filter { classInfo ->
                            classInfo.className.contains(
                                query,
                                ignoreCase = true
                            ) || classInfo.major.contains(
                                query,
                                ignoreCase = true
                            ) || classInfo.college.contains(query, ignoreCase = true)
                        }
                        NetworkState.Success(filtered)
                    }
                }

                else -> networkState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkState.Idle
        )

    /**
     * 更新搜索关键词
     */
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    /**
     * 更新学年学期。
     * 调用此方法后，由于 _params 是 StateFlow，
     * flatMapLatest 会感知到变化并自动重新执行 fetchClassList 逻辑。
     */
    fun updateDate(year: String, semester: String) {
        _params.value = year to semester
    }

    /**
     * 手动刷新功能（可选）
     * 通过重新赋值当前参数，强制触发 flatMapLatest 重跑
     */
    fun refresh() {
        _params.value = _params.value.copy()
    }
}