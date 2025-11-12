package com.example.vcore

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val getBalance by lazy {
        Timber.d("Creating GetBalance")
        GetBalance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Timber.d("MainActivity created")

        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = findViewById(R.id.tvSubtitle)
        val btnGetBalance: Button = findViewById(R.id.btnGetBalance)
        val btnRead: Button = findViewById(R.id.btnRead)

        btnGetBalance.setOnClickListener {
              Toast.makeText(this@MainActivity, "test", Toast.LENGTH_SHORT).show()
            Timber.d("Get Balance clicked")
            lifecycleScope.launch {
                try {
                    val account = getBalance.fetchAccountBalance()
                    val activeBalances = (account.balances ?: emptyList())
                        .filter {
                            val free = it.free.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val locked = it.locked.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            free > BigDecimal("0.0001") || locked > BigDecimal.ZERO
                        }
                        .joinToString("\n") { "${it.asset}: ${it.free} (locked: ${it.locked})" }

                    tvTitle.text = if (activeBalances.isEmpty()) "No assets" else activeBalances
                    Timber.d("Balance shown")
                } catch (e: Exception) {
                    tvTitle.text = "Error: ${e.message}"
                    Timber.e(e, "Balance error")
                }
            }
        }

        btnRead.setOnClickListener {
            Timber.d("Update Prices clicked")
            btnRead.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            lifecycleScope.launch {
                try {
                    val fioPrice = fetchPrice("FIOUSDT")
                    val btcPrice = fetchPrice("BTCUSDT")
                    Timber.d("Prices: FIO=$fioPrice, BTC=$btcPrice")

                    applicationContext.openFileOutput("fio.txt", MODE_PRIVATE).use { it.write(fioPrice.toByteArray()) }
                    applicationContext.openFileOutput("btc.txt", MODE_PRIVATE).use { it.write(btcPrice.toByteArray()) }

                    val fio = applicationContext.filesDir.resolve("fio.txt").readText()
                    val btc = applicationContext.filesDir.resolve("btc.txt").readText()
Timber.i("This is an info message")
                    withContext(Dispatchers.Main) {
                        tvTitle.text = "FIO: $fio"
                        tvSubtitle.text = "BTC: $btc"
                        Toast.makeText(this@MainActivity, "Updated", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    Timber.e(e, "Price update failed")
                } finally {
                    withContext(Dispatchers.Main) { btnRead.backgroundTintList = null }
                }
            }
        }
    }

    private suspend fun fetchPrice(symbol: String): String = withContext(Dispatchers.IO) {
        val url = "https://api.binance.com/api/v3/ticker/price?symbol=$symbol"
        Timber.v("Fetching $symbol")
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            JSONObject(response.body!!.string()).optString("price", "N/A")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getBalance.close()
        Timber.d("MainActivity destroyed")
    }
}