package com.example.vcore

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Настройки API Binance
object Constants {
    const val BASE_URL = "https://api.binance.com/api/v3/"
    const val API_KEY = "<YOUR_BINANCE_API_KEY>"
    const val SECRET_KEY = "<YOUR_BINANCE_SECRET_KEY>"
}

@Serializable
data class AccountInfo(val balances: List<Balance>)

@Serializable
data class Balance(val asset: String, val free: String, val locked: String)

class GetBalance {

    /**
     * Метод для получения текущего баланса аккаунта Binance.
     */
    suspend fun fetchAccountBalance(): AccountInfo {
        val timestamp = System.currentTimeMillis().toString()
        val params = mapOf("timestamp" to timestamp)
        val signature = generateSignature(params)

        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }.use { client ->
            client.get("${Constants.BASE_URL}account") {
                accept(ContentType.Application.Json)
                parameter("timestamp", timestamp)
                parameter("signature", signature)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${Constants.API_KEY}")
                }
            }.body()
        }
    }

    /**
     * Генерируем подпись запроса методом HMAC SHA256.
     */
    fun generateSignature(parameters: Map<String, String>): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(Constants.SECRET_KEY.toByteArray(), mac.algorithm)
        mac.init(keySpec)

        val encodedParams = parameters.entries.joinToString("&") { "${it.key}=${it.value}" }
        val digest = mac.doFinal(encodedParams.toByteArray())
        return digest.encodeHex()
    }

    /**
     * Преобразуем массив байтов в hex-кодировку.
     */
    private fun ByteArray.encodeHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }.lowercase()
}

// Тестируем получение баланса
//fun main() {
 //   runBlocking {
 //       val balanceFetcher = GetBalance()
 //       val accountInfo = balanceFetcher.fetchAccountBalance()
 //       println(accountInfo)
 //   }
//}