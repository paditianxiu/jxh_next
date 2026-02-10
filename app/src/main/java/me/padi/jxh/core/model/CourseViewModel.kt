package me.padi.jxh.core.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.common.DateStorage
import me.padi.jxh.core.common.DateStorage.Type
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.ClassParams
import me.padi.jxh.data.repository.CourseRepository

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {


    private val _courseState = MutableStateFlow<NetworkState<String>>(NetworkState.Idle)

    val courseState = _courseState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchCourse(
        year: String = DateStorage.getYear(Type.PERSONAL).toString(),
        semester: String = DateStorage.getSemester(Type.PERSONAL).toString()
    ) {
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


}