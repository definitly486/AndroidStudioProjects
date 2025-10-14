package com.example.coin

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class CryptoDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: CryptoViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var nameTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var change24hTextView: TextView
    private lateinit var change7dTextView: TextView
    private lateinit var marketCapTextView: TextView
    private lateinit var volumeTextView: TextView
    private lateinit var high24hTextView: TextView
    private lateinit var low24hTextView: TextView
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto_detail)

        initViews()
        setupViewModel()
        setupObservers()

        val cryptoId = intent.getStringExtra("CRYPTO_ID")
        if (cryptoId != null) {
            viewModel.loadCryptoDetail(cryptoId)
        } else {
            errorTextView.text = "Ошибка: не передан ID криптовалюты"
            errorTextView.visibility = TextView.VISIBLE
        }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBarDetail)
        nameTextView = findViewById(R.id.cryptoNameDetail)
        priceTextView = findViewById(R.id.cryptoPriceDetail)
        change24hTextView = findViewById(R.id.change24hDetail)
        change7dTextView = findViewById(R.id.change7dDetail)
        marketCapTextView = findViewById(R.id.marketCapDetail)
        volumeTextView = findViewById(R.id.volumeDetail)
        high24hTextView = findViewById(R.id.high24hDetail)
        low24hTextView = findViewById(R.id.low24hDetail)
        errorTextView = findViewById(R.id.errorTextViewDetail)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CryptoViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.selectedCrypto.observe(this) { cryptoDetail ->
            cryptoDetail?.let {
                displayCryptoDetail(it)
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) ProgressBar.VISIBLE else ProgressBar.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                errorTextView.visibility = TextView.VISIBLE
                errorTextView.text = it
            } ?: run {
                errorTextView.visibility = TextView.GONE
            }
        }
    }

    private fun displayCryptoDetail(crypto: CryptoDetail) {
        nameTextView.text = "${crypto.name} (${crypto.symbol.uppercase()})"

        val currentPrice = crypto.market_data.current_price["usd"] ?: 0.0
        priceTextView.text = "$${String.format("%.2f", currentPrice)}"

        val change24h = crypto.market_data.price_change_percentage_24h
        change24hTextView.text = "24ч: ${String.format("%.2f", change24h)}%"
        setChangeColor(change24hTextView, change24h)

        val change7d = crypto.market_data.price_change_percentage_7d
        change7dTextView.text = "7д: ${String.format("%.2f", change7d)}%"
        setChangeColor(change7dTextView, change7d)

        val marketCap = crypto.market_data.market_cap["usd"] ?: 0.0
        marketCapTextView.text = "Капитализация: $${formatLargeNumber(marketCap)}"

        val volume = crypto.market_data.total_volume["usd"] ?: 0.0
        volumeTextView.text = "Объем 24ч: $${formatLargeNumber(volume)}"

        val high24h = crypto.market_data.high_24h["usd"] ?: 0.0
        high24hTextView.text = "Макс 24ч: $${String.format("%.2f", high24h)}"

        val low24h = crypto.market_data.low_24h["usd"] ?: 0.0
        low24hTextView.text = "Мин 24ч: $${String.format("%.2f", low24h)}"
    }

    private fun setChangeColor(textView: TextView, change: Double) {
        val color = if (change >= 0) {
            getColor(R.color.green)
        } else {
            getColor(R.color.red)
        }
        textView.setTextColor(color)
    }

    private fun formatLargeNumber(number: Double): String {
        return when {
            number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000)
            number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000)
            number >= 1_000 -> String.format("%.2fK", number / 1_000)
            else -> String.format("%.2f", number)
        }
    }
}