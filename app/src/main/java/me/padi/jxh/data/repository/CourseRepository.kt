package me.padi.jxh.data.repository

import io.ktor.http.Parameters
import me.padi.jxh.Api
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState

open class CourseRepository(
    private val network: NetworkDataSource
) {

    open suspend fun fetchCourse(
        year: String, semester: String
    ): NetworkState<String> {
        return runCatching {
            val result = network.postText(Api.COURSE_URL, Parameters.build {
                append("xnm", year)
                append("xqm", semester)
            }).getOrThrow()
            NetworkState.Success(result)
        }.getOrElse { throwable ->
            NetworkState.Error("获取课程表失败", throwable)
        }
    }
}