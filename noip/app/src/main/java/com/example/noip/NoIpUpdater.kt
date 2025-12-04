package com.example.noip
import okhttp3.*
import okhttp3.Credentials.basic
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.util.concurrent.TimeUnit

object NoIpUpdater {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Все возможные ответы от No-IP
    sealed class UpdateResult {
        data class Success(val message: String, val ip: String?, val changed: Boolean) : UpdateResult()
        data class NoChange(val ip: String) : UpdateResult()
        sealed class Error(val message: String) : UpdateResult() {
            object NoHost : Error("Хост не найден")
            object BadAuth : Error("Неверный логин или пароль")
            object BadAgent : Error("Недопустимый User-Agent")
            object Abuse : Error("Хост заблокирован за частые обновления")
            object NotFqdn : Error("Некорректное имя хоста")
            object NoHostProvided : Error("Не указан hostname")
            object TooManyRequests : Error("Слишком много запросов (rate limit)")
            object ServerError : Error("Временная ошибка сервера No-IP (!911)")
            data class Network(val cause: Throwable) : Error("Сетевая ошибка: ${cause.message}")
            data class Http(val code: Int, val body: String) : Error("HTTP $code: $body")
            data class Unknown(val raw: String) : Error("Неизвестная ошибка: $raw")
        }
    }

    /**
     * Асинхронное обновление с полной обработкой всех ошибок No-IP
     */
    suspend fun updateIp(
        username: String,
        password: String,
        hostname: String,
        ip: String? = null,
        userAgent: String = "KotlinNoIpClient/3.0 contact@yourmail.com"
    ): UpdateResult = withContext(Dispatchers.IO) {

        if (username.isBlank() || password.isBlank() || hostname.isBlank()) {
            return@withContext UpdateResult.Error.BadAuth
        }

        val credentials = basic(username, password)
        val url = buildString {
            append("https://dynupdate.no-ip.com/nic/update?")
            append("hostname=").append(hostname)
            if (ip != null && ip.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}\$"))) {
                append("&myip=").append(ip)
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", credentials)
            .header("User-Agent", userAgent)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""

                if (!response.isSuccessful) {
                    return@withContext when (response.code) {
                        401 -> UpdateResult.Error.BadAuth
                        429 -> UpdateResult.Error.TooManyRequests
                        in 500..599 -> UpdateResult.Error.ServerError
                        else -> UpdateResult.Error.Http(response.code, body)
                    }
                }

                // Обработка ответов No-IP (регистронезависимо)
                return@withContext when {
                    body.startsWith("good", ignoreCase = true) -> {
                        val currentIp = body.substringAfter(" ")
                        UpdateResult.Success("IP успешно обновлён", currentIp, changed = true)
                    }
                    body.startsWith("nochg", ignoreCase = true) -> {
                        val currentIp = body.substringAfter(" ")
                        UpdateResult.NoChange(currentIp)
                    }
                    body.contains("nohost", ignoreCase = true) -> UpdateResult.Error.NoHost
                    body.contains("badauth", ignoreCase = true) -> UpdateResult.Error.BadAuth
                    body.contains("badagent", ignoreCase = true) -> UpdateResult.Error.BadAgent
                    body.contains("abuse", ignoreCase = true) -> UpdateResult.Error.Abuse
                    body.contains("notfqdn", ignoreCase = true) -> UpdateResult.Error.NotFqdn
                    body.contains("!donator", ignoreCase = true) -> UpdateResult.Error.Abuse
                    body.contains("911", ignoreCase = true) -> UpdateResult.Error.ServerError
                    body.isEmpty() -> UpdateResult.Error.Unknown("Пустой ответ сервера")
                    else -> UpdateResult.Error.Unknown(body)
                }
            }

        } catch (e: IOException) {
            UpdateResult.Error.Network(e)
        } catch (e: Exception) {
            UpdateResult.Error.Network(e)
        }
    }
}