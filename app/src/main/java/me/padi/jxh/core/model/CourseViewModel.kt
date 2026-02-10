package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.ClassParams
import me.padi.jxh.data.repository.CourseRepository
import me.padi.jxh.data.repository.ProcessedClassInfo

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _courseState = MutableStateFlow<NetworkState<String>>(NetworkState.Idle)

    val courseState = _courseState.asStateFlow()

    fun fetchCourse(year: String = "2025", semester: String = "3") {
        viewModelScope.launch {
            _courseState.value = NetworkState.Loading
            _courseState.value = courseRepository.fetchCourse(year, semester)
        }
    }


    fun fetchClassCourse(params: ClassParams) {
        viewModelScope.launch {
            _courseState.value = NetworkState.Loading
            _courseState.value = courseRepository.fetchClassCourse(params)
        }
    }


    private val _classListState =
        MutableStateFlow<NetworkState<List<ProcessedClassInfo>>>(NetworkState.Idle)

    val classListState = _classListState.asStateFlow()

    fun fetchClassList(
        year: String = "2025", semester: String = "12"
    ) {
        viewModelScope.launch {
            _classListState.value = NetworkState.Loading
            _classListState.value = courseRepository.fetchClassList(year, semester)
        }
    }

}