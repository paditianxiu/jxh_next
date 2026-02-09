package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.CourseRepository

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _courseState = MutableStateFlow<NetworkState<String>>(NetworkState.Idle)

    val courseState = _courseState.asStateFlow()

    fun fetchCourse(year: String = "2025", semester: String = "3") {
        viewModelScope.launch {
            setLoading()
            _courseState.value = courseRepository.fetchCourse(year, semester)
        }
    }

    fun setLoading() {
        _courseState.value = NetworkState.Loading
    }


}