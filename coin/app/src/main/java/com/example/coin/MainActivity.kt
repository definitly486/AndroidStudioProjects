package com.example.coin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: CryptoViewModel
    private lateinit var adapter: CryptoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupViewModel()
        setupObservers()

        refreshButton.setOnClickListener {
            viewModel.loadTopCryptos()
        }

        // Первоначальная загрузка
        viewModel.loadTopCryptos()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.cryptoRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        refreshButton = findViewById(R.id.refreshButton)
    }

    private fun setupRecyclerView() {
        adapter = CryptoAdapter { crypto ->
            // Обработка клика по элементу
            showCryptoDetail(crypto)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CryptoViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.cryptoList.observe(this) { cryptos ->
            cryptos?.let {
                adapter.submitList(it)
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = it
            } ?: run {
                errorTextView.visibility = View.GONE
            }
        }
    }

    private fun showCryptoDetail(crypto: CryptoCurrency) {
        val intent = Intent(this, CryptoDetailActivity::class.java).apply {
            putExtra("CRYPTO_ID", crypto.id)
        }
        startActivity(intent)
    }
}