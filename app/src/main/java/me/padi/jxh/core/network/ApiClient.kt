package me.padi.jxh.core.network

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.padi.jxh.core.common.MyCookieStorage

class ApiClient(context: Context) {
    private val cookieStorage = MyCookieStorage(context)

    val client: HttpClient by lazy {
        HttpClient {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            install(HttpCookies) {
                storage = cookieStorage
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            install(DefaultRequest) {
                headers {
                    append(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"
                    )
                    append(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
                    )
                    append("Accept-Language", "zh-CN,zh;q=0.9")
                    append("Cache-Control", "max-age=0")
                    append("Connection", "keep-alive")
                    append("Upgrade-Insecure-Requests", "1")
                }
            }
        }
    }

    fun clearCookies() {
        cookieStorage.clear()
    }

    fun getCookieCount(): Int = cookieStorage.getCookieCount()
}