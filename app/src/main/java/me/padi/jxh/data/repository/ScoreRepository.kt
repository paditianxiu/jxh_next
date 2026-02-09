package me.padi.jxh.data.repository

import io.ktor.http.Parameters
import me.padi.jxh.Api
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState
import org.json.JSONObject
import org.jsoup.Jsoup

open class ScoreRepository(private val network: NetworkDataSource) {
    open suspend fun fetchScore(): NetworkState<List<ScoreItem>> {
        val time = System.currentTimeMillis().toString()
        val scoreItems = mutableListOf<ScoreItem>()

        val infoHtml = network.getText(Api.STUDENT_INFO_URL)
            .getOrElse { return NetworkState.Error("获取学生信息失败", it) }

        val doc = Jsoup.parse(infoHtml)
        val xhId = doc.selectFirst("input#xh_id")?.attr("value") ?: ""
        if (xhId.isEmpty()) {
            return NetworkState.Error("获取学号失败")
        }

        val data = Parameters.build {
            append("xh_id", xhId)
            append("xnm", "")
            append("xqm", "")
            append("_search", "false")
            append("nd", time)
            append("queryModel.showCount", "100")
            append("queryModel.currentPage", "1")
            append("queryModel.sortName", "")
            append("queryModel.sortOrder", "aes")
            append("time", "2")
        }

        val scoreHtml = network.postText(Api.SCORE_URL, data)
            .getOrElse { return NetworkState.Error("获取成绩失败", it) }

        return runCatching {
            val json = JSONObject(scoreHtml)
            val items = json.optJSONArray("items") ?: return NetworkState.Error("成绩为空")

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val semester =
                    "${item.optString("xnmmc")}第${item.optString("xqmmc")}学期"

                scoreItems.add(
                    ScoreItem(
                        courseName = item.optString("kcmc"),
                        totalScore = item.optString("cj"),
                        courseType = item.optString("kcxzmc"),
                        examType = item.optString("ksxz"),
                        semester = semester,
                        credit = item.optString("xf"),
                        teacherName = item.optString("jsxm"),
                        department = item.optString("kkbmmc")
                    )
                )
            }

            NetworkState.Success(scoreItems)
        }.getOrElse {
            NetworkState.Error("解析成绩失败", it)
        }
    }
}

data class ScoreItem(
    val courseName: String = "",
    val totalScore: String = "",
    val courseType: String = "",
    val examType: String = "",
    val semester: String = "",
    val credit: String = "",
    val teacherName: String = "",
    val department: String = ""
)