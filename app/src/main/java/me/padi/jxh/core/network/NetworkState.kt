package me.padi.jxh.core.network

import me.padi.jxh.data.repository.NewsData

sealed class NetworkState<out T> {
    data object Idle : NetworkState<Nothing>()
    data object Loading : NetworkState<Nothing>()
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error(val message: String, val error: Throwable? = null) : NetworkState<Nothing>()

    // 辅助函数
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isIdle(): Boolean = this is Idle

    // 安全获取数据
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getErrorOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }

    fun getThrowableOrNull(): Throwable? = when (this) {
        is Error -> error
        else -> null
    }
}