package me.padi.jxh.core.network

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

object ApiClient {
    val cookieStorage = MyCookieStorage()

    val client = HttpClient {
        // 安装日志插件
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }


        // 安装Cookies插件
        install(HttpCookies) {
            storage = cookieStorage
        }

        // 安装内容协商插件
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