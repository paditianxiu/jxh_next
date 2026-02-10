package me.padi.jxh.core.common

import com.tencent.mmkv.MMKV
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.padi.jxh.core.network.ApiClient

class MyCookieStorage : CookiesStorage {

    private val mmkv by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MMKV.mmkvWithID("ktor_cookies")
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val lock = Any()

    private var cache: MutableList<Cookie> = loadFromDisk()

    private fun loadFromDisk(): MutableList<Cookie> = synchronized(lock) {
        runCatching {
            val str = mmkv.decodeString("cookies") ?: return mutableListOf()
            json.decodeFromString<List<CookieBean>>(str).map { it.toCookie() }.toMutableList()
        }.getOrElse {
            mutableListOf()
        }
    }

    private fun persist() = synchronized(lock) {
        runCatching {
            val str = json.encodeToString(cache.map { it.toBean() })
            mmkv.encode("cookies", str)
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = synchronized(lock) {
        cache.filter {
            it.domain == null || requestUrl.host.endsWith(it.domain!!)
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = synchronized(lock) {
        val fixed = cookie.fillDefaults(requestUrl)
        cache.removeAll { it.name == fixed.name && it.domain == fixed.domain }
        cache.add(fixed)
        persist()
    }

    override fun close() {}

    fun clear() = synchronized(lock) {
        cache.clear()
        mmkv.removeValueForKey("cookies")
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
