package me.padi.jxh.core.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MyCookieStorage(context: Context) : CookiesStorage {

    private val preferences: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        context.getSharedPreferences("ktor_cookies", Context.MODE_PRIVATE)
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val lock = Any()

    private var cache: MutableList<Cookie> = loadFromDisk()

    private fun loadFromDisk(): MutableList<Cookie> = synchronized(lock) {
        runCatching {
            val str = preferences.getString("cookies", null) ?: return mutableListOf()
            json.decodeFromString<List<CookieBean>>(str).map { it.toCookie() }.toMutableList()
        }.getOrElse {
            mutableListOf()
        }
    }

    private fun persist() = synchronized(lock) {
        runCatching {
            val str = json.encodeToString(cache.map { it.toBean() })
            preferences.edit().putString("cookies", str).apply()
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = synchronized(lock) {
        // 过滤过期 cookies
        val now = GMTDate()
        cache.removeAll { it.expires != null && it.expires!! < now }

        cache.filter {
            it.matches(requestUrl)
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = synchronized(lock) {
        val fixed = cookie.fillDefaults(requestUrl)

        // 检查 cookie 是否已过期
        if (fixed.expires != null && fixed.expires!! < GMTDate()) {
            // 删除已过期的 cookie
            cache.removeAll { it.name == fixed.name && it.domain == fixed.domain }
        } else {
            cache.removeAll { it.name == fixed.name && it.domain == fixed.domain }
            cache.add(fixed)
        }

        persist()
    }

    override fun close() {}

    fun clear() = synchronized(lock) {
        cache.clear()
        preferences.edit { remove("cookies") }
    }

    /**
     * 移除指定域名下的所有 cookies
     */
    fun removeCookiesForDomain(domain: String?) = synchronized(lock) {
        cache.removeAll { it.domain == domain }
        persist()
    }

    /**
     * 获取所有 cookies 的数量（用于调试）
     */
    fun getCookieCount(): Int = synchronized(lock) {
        cache.size
    }

}

@Serializable
data class CookieBean(
    val name: String,
    val value: String,
    val domain: String?,
    val path: String?,
    val expires: Long?,
    val secure: Boolean,
    val httpOnly: Boolean
)

fun Cookie.toBean() = CookieBean(
    name, value, domain, path, expires?.timestamp, secure, httpOnly
)

fun CookieBean.toCookie() = Cookie(
    name = name,
    value = value,
    domain = domain,
    path = path,
    expires = expires?.let { GMTDate(it) },
    secure = secure,
    httpOnly = httpOnly
)

// 扩展函数：检查 cookie 是否匹配 URL
private fun Cookie.matches(url: Url): Boolean {
    // 检查 domain
    if (domain != null && !url.host.endsWith(domain!!)) {
        return false
    }

    // 检查 path
    if (path != null && !url.encodedPath.startsWith(path!!)) {
        return false
    }

    // 检查 secure
    if (secure && url.protocol.name != "https") {
        return false
    }

    // 检查过期时间
    if (expires != null && expires!! < GMTDate()) {
        return false
    }

    return true
}