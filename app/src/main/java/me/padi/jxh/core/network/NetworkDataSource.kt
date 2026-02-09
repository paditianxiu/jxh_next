package me.padi.jxh.core.network

import android.os.Build
import androidx.annotation.RequiresApi
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import org.json.JSONObject
import java.util.Base64


class NetworkDataSource(
    private val client: HttpClient
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun downloadImageToBase64(imageUrl: String, imageType: String? = null): Result<String> =
        runCatching {
            val response = client.get(imageUrl)
            val channel: ByteReadChannel = response.bodyAsChannel()
            val bytes = channel.readRemaining().readBytes()
            val actualImageType =
                imageType ?: determineImageType(imageUrl, response.headers[HttpHeaders.ContentType])
            bytesToBase64(bytes, actualImageType)
        }

    private fun determineImageType(imageUrl: String, contentType: String?): String {
        // 优先从Content-Type推断
        contentType?.let {
            when {
                it.contains("jpeg") || it.contains("jpg") -> return "jpeg"
                it.contains("png") -> return "png"
                it.contains("gif") -> return "gif"
                it.contains("bmp") -> return "bmp"
                it.contains("webp") -> return "webp"
            }
        }

        // 从URL后缀推断
        val lowerUrl = imageUrl.lowercase()
        when {
            lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") -> return "jpeg"
            lowerUrl.endsWith(".png") -> return "png"
            lowerUrl.endsWith(".gif") -> return "gif"
            lowerUrl.endsWith(".bmp") -> return "bmp"
            lowerUrl.endsWith(".webp") -> return "webp"
            else -> return "jpeg" // 默认值
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun bytesToBase64(imageBytes: ByteArray, imageType: String = "jpeg"): String {
        val base64 = Base64.getEncoder().encodeToString(imageBytes)
        return "data:image/$imageType;base64,$base64"
    }

    suspend fun ocrImageFromUrl(imageUrl: String, imageType: String? = null): Result<OcrResponse> =
        runCatching {
            // 下载图片并转换为Base64
            val base64Image = downloadImageToBase64(imageUrl, imageType).getOrThrow()

            // 进行OCR识别
            ocrImage(base64Image).getOrThrow()
        }

    suspend fun ocrImage(base64Image: String): Result<OcrResponse> = runCatching {
        val response: HttpResponse = client.post("https://jxh.karpov.cn/api/ocr") {
            contentType(Json)
            setBody(
                """
                {
                "img":"${base64Image}"
                }
            """.trimIndent()
            )
        }

        // 解析响应
        val json = JSONObject(response.bodyAsText())
        val result = json.optString("result")
        val success = json.optBoolean("success")
        OcrResponse(result = result, success = success)
    }

    suspend fun ocrCaptcha(captchaUrl: String): Result<String> = runCatching {
        val response = ocrImageFromUrl(captchaUrl).getOrThrow()
        if (response.success) {
            response.result
        } else {
            throw Exception("OCR识别失败")
        }
    }


    suspend fun getText(url: String): Result<String> = runCatching {
        client.get(url).bodyAsText()
    }

    suspend fun getResponse(url: String): Result<HttpResponse> = runCatching {
        client.get(url)
    }

    suspend fun postText(
        url: String, params: Parameters
    ): Result<String> = runCatching {
        val response = client.post(url) {
            setBody(
                FormDataContent(
                    params
                )
            )
        }
        response.bodyAsText()
    }
}


data class OcrResponse(
    val result: String, val success: Boolean
)


