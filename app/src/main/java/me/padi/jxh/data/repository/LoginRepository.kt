package me.padi.jxh.data.repository


import io.ktor.http.Parameters
import me.padi.jxh.Api
import me.padi.jxh.core.common.RSA
import me.padi.jxh.core.network.ApiClient
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.core.network.NetworkState
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class LoginRepository(
    private val network: NetworkDataSource,
    private val apiClient: ApiClient
) {
    open suspend fun login(userName: String, password: String): NetworkState<String> {
        return try {
            apiClient.cookieStorage.clearAll()
            val time = System.currentTimeMillis().toString()
            val result = runCatching {
                val pageText = network.getText(Api.LOGOUT_URL + time).getOrThrow()
                val doc = Jsoup.parse(pageText)
                val csrftoken = doc.selectFirst("input#csrftoken")?.attr("value") ?: ""
                val keyJson = network.getText(Api.LOGIN_KEY_URL + time).getOrThrow()
                val obj = JSONObject(keyJson)
                val exponent = obj.optString("exponent")
                val modulus = obj.optString("modulus")
                if (exponent.isBlank() || modulus.isBlank()) {
                    throw IllegalStateException("密钥解析失败")
                }
                val enPassword = RSA.encrypt(password, exponent, modulus)
                val code = network.ocrCaptcha(Api.CODE_URL + time).getOrThrow()
                val loginResult = network.postText(
                    Api.LOGIN_URL + time, Parameters.build {
                        append("csrftoken", csrftoken)
                        append("yhm", userName)
                        append("mm", enPassword)
                        append("yzm", code)
                    }).getOrThrow()
                val loginDoc = Jsoup.parse(loginResult)

                if (!isLoginSuccessful(loginDoc).first) {
                    throw IllegalStateException(isLoginSuccessful(loginDoc).second)
                }
                "登录成功"
            }
            result.fold(
                onSuccess = { NetworkState.Success(it) },
                onFailure = { NetworkState.Error(it.message ?: "登录失败", it) })

        } catch (e: Exception) {
            NetworkState.Error("登录过程异常: ${e.message}", e)
        }
    }

    suspend fun logout(): Boolean {
        return runCatching {
            apiClient.cookieStorage.clearAll()
            val time = System.currentTimeMillis().toString()
            network.getText(Api.LOGOUT_URL + time).getOrThrow()
            true
        }.getOrElse {
            false
        }
    }


    open suspend fun checkLogin(): Boolean {
        return runCatching {
            val pageText = network.getText(Api.STUDENT_INFO_URL).getOrThrow()
            val doc = Jsoup.parse(pageText)
            val loggedIn = isLoginSuccessful(doc)
            if (!loggedIn.first) {
                throw IllegalStateException(loggedIn.second)
            }
            true
        }.getOrElse { throwable ->
            false
        }
    }
}

private fun isLoginSuccessful(doc: Document): Pair<Boolean, String> {
    val isLoginPage = doc.getElementsByTag("h5").any { it.text() == "用户登录" }
    if (isLoginPage) {
        val test = doc.getElementById("tips")
        val errorMsg = if (test != null && test.text().isNotBlank()) {
            test.text()
        } else {
            "账号或密码错误"
        }
        return Pair(false, errorMsg)
    } else {
        return Pair(true, "登录成功")
    }
}




