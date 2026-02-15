package me.padi.jxh.core.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.core.utils.Constants.Companion.SCHOOL_NEWS_URL
import me.padi.jxh.data.repository.NewsArticleEntity
import me.padi.jxh.data.repository.NewsData
import me.padi.jxh.data.repository.NewsRepository

class NewsViewModel(
    private val newsRepository: NewsRepository,
    private val initialNewsUrl: String = SCHOOL_NEWS_URL  // 添加构造函数参数
) : ViewModel() {

    data class NewsUiState(
        val newsList: List<NewsData> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentPage: Int = 1,
        val hasMorePages: Boolean = true,
        val newsArticle: NewsArticleEntity? = null,
        val newsUrl: String = SCHOOL_NEWS_URL  // 添加到 UI State 中
    )

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    fun fetchSchoolNews(page: Int = _uiState.value.currentPage) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                when (val result = newsRepository.fetchSchoolNews(_uiState.value.newsUrl, page)) {
                    is NetworkState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            newsList = _uiState.value.newsList + result.data,
                            isLoading = false,
                            currentPage = page + 1,
                            hasMorePages = result.data.isNotEmpty()
                        )
                    }

                    is NetworkState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, error = result.message
                        )
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun updateNewsUrl(newUrl: String) {
        _uiState.value = _uiState.value.copy(
            newsUrl = newUrl, newsList = emptyList(), currentPage = 1, hasMorePages = true
        )
        fetchSchoolNews(1)
    }

    fun loadNextPage() {
        if (_uiState.value.hasMorePages && !_uiState.value.isLoading) {
            fetchSchoolNews(_uiState.value.currentPage)
        }
    }

    fun getNewsDetail(url: String) {
        viewModelScope.launch {
            val res = newsRepository.getNewsDetail(url)
            if (res != null) {
                _uiState.value = _uiState.value.copy(newsArticle = res)
            }
        }
    }

    init {
        fetchSchoolNews(1)
    }
}





