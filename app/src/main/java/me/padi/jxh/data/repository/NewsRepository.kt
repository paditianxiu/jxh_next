package me.padi.jxh.data.repository

import me.padi.jxh.Api.BASE_SCHOOL_URL
import me.padi.jxh.core.common.d
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState
import org.jsoup.Jsoup
import java.net.URL

open class NewsRepository(private val network: NetworkDataSource) {
    open suspend fun fetchSchoolNews(newsUrl: String, page: Int): NetworkState<List<NewsData>> {
        val newsData = mutableListOf<NewsData>()
        return runCatching {
            val url = if (page == 1) {
                "$newsUrl.htm"
            } else {
                val firstPageResult = network.getText("$newsUrl.htm").getOrThrow()
                val dom = Jsoup.parse(firstPageResult)
                val pages = dom.getElementsByClass("p_pages")
                val lastNum =
                    pages.firstOrNull()?.getElementsByClass("p_no")?.lastOrNull()?.text()?.toInt()
                        ?: 1
                val pageNum = lastNum - page + 1
                "$newsUrl/$pageNum.htm"
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
                var fullPicUrl = ""
                if (style.isNotEmpty()) {
                    val picUrl = if (style.contains("url(")) {
                        val start = style.indexOf("url(") + 4
                        val end = style.indexOf(")", start)
                        style.substring(start, end).replace("\"", "").replace("'", "").trim()
                    } else {
                        ""
                    }
                    fullPicUrl = if (picUrl.startsWith("http")) {
                        picUrl
                    } else {
                        "$BASE_SCHOOL_URL$picUrl"
                    }
                }
                val url = new.getElementsByTag("a").firstOrNull()?.attr("href") ?: ""
                val fullUrl = when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    url.startsWith("/") -> {
                        val baseUri = URL(BASE_SCHOOL_URL)
                        "${baseUri.protocol}://${baseUri.host}$url"
                    }

                    else -> {
                        val cleanPath = url.replace(Regex("^\\.\\./+"), "")
                        val normalizedBase =
                            if (BASE_SCHOOL_URL.endsWith("/")) BASE_SCHOOL_URL else "$BASE_SCHOOL_URL/"
                        "$normalizedBase$cleanPath"
                    }
                }.replace("../", "")


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
            ?: dom.getElementsByClass("v_news_content").firstOrNull()?.toString()
        var attachments = dom.select("ul[style*='list-style-type:none'] li").mapNotNull { li ->
            li.selectFirst("a")?.let { link ->
                AttachmentEntity(
                    fileName = link.text().trim(),
                    fileType = link.text().trim().getFileType(),
                    url = link.attr("href").toFullUrl()
                )
            }
        }

        if (attachments.isEmpty()) {
            attachments = dom.getElementsByClass("vsbcontent_end").mapNotNull { p ->
                p.selectFirst("a")?.let { link ->
                    AttachmentEntity(
                        fileName = link.text().trim(),
                        fileType = link.text().trim().getFileType(),
                        url = link.attr("href").toFullUrl()
                    )
                }
            }
        }


        return NewsArticleEntity(
            title = title,
            publishDate = time,
            articleContent = articleContent,
            attachment = attachments
        )
    }

    private suspend fun String.getRealUrl(): String {
        if (this.isEmpty()) return ""

        val code =
            network.ocrCaptcha("/system/resource/js/filedownload/createimage.jsp?randnum=${System.currentTimeMillis()}".toFullUrl())
                .getOrNull()

        val res = network.getText(this).getOrThrow()

        if (!res.contains("请输入验证码")) {
            return this
        }
        val separator = if (this.contains("?")) "&" else "?"
        val finalUrl = "$this${separator}codeValue=$code"
        finalUrl.d()
        return finalUrl
    }
}


private fun String.getFileType(): String {
    val lastDotIndex = this.lastIndexOf(".")
    return if (lastDotIndex > 0 && lastDotIndex < this.length - 1) {
        this.substring(lastDotIndex + 1)
    } else {
        "未知"
    }
}

private fun String.toFullUrl(): String {
    if (this.isEmpty()) return ""

    if (this.startsWith("http://") || this.startsWith("https://")) {
        return this
    }

    val normalizedPath = if (!this.startsWith("/")) "/$this" else this

    return BASE_SCHOOL_URL + normalizedPath
}


data class NewsData(
    val title: String = "",
    val text: String = "",
    val time: String = "",
    val img: String = "",
    val url: String = "",
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