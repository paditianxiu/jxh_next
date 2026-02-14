package me.padi.jxh.data.repository

import me.padi.jxh.core.common.d
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState
import me.padi.jxh.core.utils.Constants.Companion.SCHOOL_NEWS_URL
import org.jsoup.Jsoup
import java.net.URL

open class NewsRepository(private val network: NetworkDataSource) {
    open suspend fun fetchSchoolNews(page: Int): NetworkState<List<NewsData>> {
        val newsData = mutableListOf<NewsData>()
        return runCatching {
            val url = if (page == 1) {
                "$SCHOOL_NEWS_URL.htm"
            } else {
                val firstPageResult = network.getText("$SCHOOL_NEWS_URL.htm").getOrThrow()
                val dom = Jsoup.parse(firstPageResult)
                val pages = dom.getElementsByClass("p_pages")
                val lastNum =
                    pages.firstOrNull()?.getElementsByClass("p_no")?.lastOrNull()?.text()?.toInt()
                        ?: 1
                val pageNum = lastNum - page + 1
                "$SCHOOL_NEWS_URL/$pageNum.htm"
            }
            val result = network.getText(url).getOrThrow()
            val dom = Jsoup.parse(result)
            val innerPage = dom.getElementsByClass("inner_page").firstOrNull()
            val news = innerPage?.getElementsByTag("li")
            news?.forEach { new ->
                val title = new.getElementsByTag("h3").firstOrNull()?.text() ?: ""
                val text = new.getElementsByTag("p").firstOrNull()?.text() ?: ""
                val time = new.getElementsByTag("time").firstOrNull()?.text() ?: ""
                val style = new.getElementsByClass("pic").firstOrNull()?.getElementsByClass("a")
                    ?.firstOrNull()?.attr("style") ?: ""
                val picUrl = if (style.contains("url(")) {
                    val start = style.indexOf("url(") + 4
                    val end = style.indexOf(")", start)
                    style.substring(start, end).replace("\"", "").replace("'", "").trim()
                } else {
                    ""
                }
                val fullPicUrl = if (picUrl.startsWith("http")) {
                    picUrl
                } else {
                    "https://jhzyedu.cn$picUrl"
                }
                val baseUrl = "https://www.jhzyedu.cn"
                val url = new.getElementsByTag("a").firstOrNull()?.attr("href") ?: ""
                val fullUrl = when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    url.startsWith("/") -> {
                        val baseUri = URL(baseUrl)
                        "${baseUri.protocol}://${baseUri.host}$url"
                    }

                    else -> {
                        val cleanPath = url.replace(Regex("^\\.\\./+"), "")
                        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                        "$normalizedBase$cleanPath"
                    }
                }.replace("../", "")

                fullUrl.d()
                newsData.add(NewsData(title, text, time, fullPicUrl, fullUrl))
            }
            NetworkState.Success(newsData)
        }.getOrElse { throwable ->
            NetworkState.Error("获取新闻失败", throwable)
        }
    }


    open suspend fun getNewsDetail(url: String): NewsArticleEntity? {
        val result = network.getText(url).getOrThrow()
        val dom = Jsoup.parse(result)
        val titleDom = dom.getElementsByClass("dtl_tit").firstOrNull()
        val title = titleDom?.getElementsByTag("h1")?.text()
        val time = titleDom?.getElementsByClass("li")?.firstOrNull()?.text()
        val articleContent = dom.getElementById("vsb_content")?.toString()
        return NewsArticleEntity(
            title = title,
            publishDate = time,
            articleContent = articleContent,
        )
    }
}


data class NewsData(
    val title: String = "",
    val text: String = "",
    val time: String = "",
    val img: String = "",
    val url: String = ""
)

data class NewsArticleEntity(
    var title: String?,
    val publishDate: String?,
    var articleContent: String?,
    val attachment: List<AttachmentEntity>? = emptyList()
)

data class AttachmentEntity(
    val fileName: String,
    val url: String,
    val fileType: String,
    val isNeedOnlineView: Boolean = false
)