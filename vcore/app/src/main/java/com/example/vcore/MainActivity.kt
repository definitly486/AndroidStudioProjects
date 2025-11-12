package com.example.vcore

import GetBalance
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
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Один клиент на всё приложение
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val textView: TextView = findViewById(R.id.tvTitle)
        val textView2: TextView = findViewById(R.id.tvSubtitle)
        val getBalanceButton: Button = findViewById(R.id.btnGetBalance)
        val readButton: Button = findViewById(R.id.btnRead)

        // Кнопка: Получить баланс
        getBalanceButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // Создаём экземпляр с context
                    val getBalance = GetBalance(this@MainActivity)
                    val accountInfo = getBalance.fetchAccountBalance()

                    // Фильтруем только ненулевые балансы
                    val nonZero = accountInfo.balances
                        .filter {
                            val free = it.free.toDoubleOrNull() ?: 0.0
                            val locked = it.locked.toDoubleOrNull() ?: 0.0
                            free > 0 || locked > 0
                        }
                        .joinToString("\n") { "${it.asset}: ${it.free} (locked: ${it.locked})" }

                    textView.text = nonZero.ifEmpty { "No assets" }
                    Toast.makeText(this@MainActivity, "Balance loaded", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    textView.text = "Error: ${e.message}"
                    Toast.makeText(this@MainActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Кнопка: Получить цены FIO и BTC
        readButton.setOnClickListener {
            readButton.setBackgroundColor(Color.GREEN)
            lifecycleScope.launch {
                try {
                    val fioPrice = fetchPrice("FIOUSDT")
                    val btcPrice = fetchPrice("BTCUSDT")

                    // Сохраняем в файлы
                    applicationContext.openFileOutput("fio.txt", MODE_PRIVATE).use {
                        it.write(fioPrice.toByteArray())
                    }
                    applicationContext.openFileOutput("btc.txt", MODE_PRIVATE).use {
                        it.write(btcPrice.toByteArray())
                    }

                    // Читаем обратно (без задержки!)
                    val fio = applicationContext.getFileStreamPath("fio.txt").readText()
                    val btc = applicationContext.getFileStreamPath("btc.txt").readText()

                    withContext(Dispatchers.Main) {
                        textView.text = fio
                        textView2.text = btc
                        Toast.makeText(this@MainActivity, "Prices updated", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Универсальная функция для получения цены
    private suspend fun fetchPrice(symbol: String): String = withContext(Dispatchers.IO) {
        val url = "https://api.binance.com/api/v3/ticker/price?symbol=$symbol"
        val request = Request.Builder().url(url).build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed: ${response.code}")
            val json = JSONObject(response.body!!.string())
            json.getString("price")
        }
    }
}