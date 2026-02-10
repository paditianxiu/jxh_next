package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.data.repository.CourseRepository
import me.padi.jxh.data.repository.ProcessedClassInfo

class ClassListViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val _classListState =
        MutableStateFlow<NetworkState<List<ProcessedClassInfo>>>(NetworkState.Idle)

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    val filteredClassList: StateFlow<NetworkState<List<ProcessedClassInfo>>> =
        combine(_classListState, _searchText) { networkState, searchText ->
            when (networkState) {
                is NetworkState.Success -> {
                    val data = networkState.data
                    if (searchText.isBlank()) {
                        networkState
                    } else {
                        val filtered = data.filter { classInfo ->
                            classInfo.className.contains(searchText, ignoreCase = true) ||
                                    classInfo.major.contains(searchText, ignoreCase = true) ||
                                    classInfo.college.contains(searchText, ignoreCase = true)
                        }
                        NetworkState.Success(filtered)
                    }
                }
                else -> networkState
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NetworkState.Idle
        )

    fun fetchClassList(
        year: String = "2025",
        semester: String = "12"
    ) {
        viewModelScope.launch {
            _classListState.value = NetworkState.Loading
            _classListState.value = courseRepository.fetchClassList(year, semester)
        }
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    init {
        fetchClassList()
    }
}