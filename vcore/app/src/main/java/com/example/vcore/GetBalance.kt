import android.content.Context
import com.example.vcore.R
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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

object Constants {
    const val BASE_URL = "https://api.binance.com/api/v3/"
    lateinit var API_KEY: String
    lateinit var SECRET_KEY: String
}

@Serializable
data class AccountInfo(
    val makerCommission: Int,
    val takerCommission: Int,
    val buyerCommission: Int,
    val sellerCommission: Int,
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean,
    val updateTime: Long,
    val accountType: String,
    val balances: List<Balance>,
    val permissions: List<String>
)

@Serializable
data class Balance(
    val asset: String,
    val free: String,
    val locked: String
)

class GetBalance(public val context: Context) {

    init {
        loadApiKeys(context)
    }

    private fun loadApiKeys(context: Context) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.binance_keys)
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("=")) {
                        val parts = line.split('=', limit = 2)
                        when (parts[0].trim()) {
                            "API_KEY" -> Constants.API_KEY = parts[1].trim()
                            "SECRET_KEY" -> Constants.SECRET_KEY = parts[1].trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load Binance API keys", e)
        }
    }

    suspend fun fetchAccountBalance(): AccountInfo {
        val timestamp = System.currentTimeMillis()
        val recvWindow = 5000L

        val params = linkedMapOf(
            "timestamp" to timestamp.toString(),
            "recvWindow" to recvWindow.toString()
        )

        val signature = generateSignature(params)

        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }.use { client ->
            client.get("${Constants.BASE_URL}account") {
                headers {
                    append("X-MBX-APIKEY", Constants.API_KEY)
                }
                parameter("timestamp", timestamp)
                parameter("recvWindow", recvWindow)
                parameter("signature", signature)
            }.body()
        }
    }

    private fun generateSignature(parameters: Map<String, String>): String {
        val queryString = parameters.entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }

        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(Constants.SECRET_KEY.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val digest = mac.doFinal(queryString.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}