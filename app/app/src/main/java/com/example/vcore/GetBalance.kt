package com.example.vcore

import android.content.Context
import com.example.vcore.R
import com.google.gson.annotations.SerializedName
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.serialization.gson.gson
import timber.log.Timber
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Constants {
    const val BASE_URL = "https://api.binance.com/api/v3/"

    private var _apiKey: String? = null
    private var _secretKey: String? = null

    val API_KEY: String get() = _apiKey ?: throw IllegalStateException("API_KEY not initialized")
    val SECRET_KEY: String get() = _secretKey ?: throw IllegalStateException("SECRET_KEY not initialized")

    fun initialize(context: Context) {
        if (_apiKey == null || _secretKey == null) {
            loadKeys(context)
        }
    }

    private fun loadKeys(context: Context) {
        try {
            context.resources.openRawResource(R.raw.binance_keys).use { input ->
                input.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        if (line.contains("=")) {
                            val (key, value) = line.split("=", limit = 2).map { it.trim() }
                            when (key) {
                                "API_KEY" -> _apiKey = value
                                "SECRET_KEY" -> _secretKey = value
                            }
                        }
                    }
                }
            }
            check(_apiKey != null && _secretKey != null) {
                "Failed to load API_KEY or SECRET_KEY"
            }
            Timber.d("Binance API keys loaded")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load binance_keys")
            throw e
        }
    }
}

data class AccountInfo(
    @SerializedName("makerCommission") val makerCommission: Int = 0,
    @SerializedName("takerCommission") val takerCommission: Int = 0,
    @SerializedName("buyerCommission") val buyerCommission: Int = 0,
    @SerializedName("sellerCommission") val sellerCommission: Int = 0,
    @SerializedName("canTrade") val canTrade: Boolean = false,
    @SerializedName("canWithdraw") val canWithdraw: Boolean = false,
    @SerializedName("canDeposit") val canDeposit: Boolean = false,
    @SerializedName("updateTime") val updateTime: Long = 0L,
    @SerializedName("accountType") val accountType: String = "",
    @SerializedName("balances") val balances: List<Balance>? = null,
    @SerializedName("permissions") val permissions: List<String>? = null
)

data class Balance(
    @SerializedName("asset") val asset: String = "",
    @SerializedName("free") val free: String = "0",
    @SerializedName("locked") val locked: String = "0"
)

class GetBalance(private val context: Context) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { gson() }
    }

    init {
        Constants.initialize(context)
        Timber.d("GetBalance initialized")
    }

    suspend fun fetchAccountBalance(): AccountInfo {
        val timestamp = System.currentTimeMillis()
        val recvWindow = 5000L
        val params = mapOf("timestamp" to timestamp.toString(), "recvWindow" to recvWindow.toString())
        val signature = generateSignature(params)

        Timber.d("Fetching account balance...")
        return try {
            val response: AccountInfo = client.get("${Constants.BASE_URL}account") {
                headers { append("X-MBX-APIKEY", Constants.API_KEY) }
                parameter("timestamp", timestamp)
                parameter("recvWindow", recvWindow)
                parameter("signature", signature)
            }.body()
            Timber.d("Balance received: ${response.balances?.size ?: 0} assets")
            response
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch balance")
            throw e
        }
    }

    private fun generateSignature(params: Map<String, String>): String {
        val queryString = params.entries.sortedBy { it.key }.joinToString("&") { "${it.key}=${it.value}" }
        Timber.v("Signature query length: ${queryString.length}")
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(Constants.SECRET_KEY.toByteArray(), "HmacSHA256")
        mac.init(keySpec)
        val digest = mac.doFinal(queryString.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun close() {
        client.close()
        Timber.d("GetBalance client closed")
    }
}