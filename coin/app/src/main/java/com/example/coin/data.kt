package com.example.coin

data class CryptoCurrency(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val market_cap: Long,
    val market_cap_rank: Int,
    val price_change_percentage_24h: Double,
    val price_change_24h: Double,
    val last_updated: String
)

data class CryptoDetail(
    val id: String,
    val symbol: String,
    val name: String,
    val description: Map<String, String>,
    val image: Map<String, String>,
    val market_data: MarketData
)

data class MarketData(
    val current_price: Map<String, Double>,
    val price_change_percentage_24h: Double,
    val price_change_percentage_7d: Double,
    val market_cap: Map<String, Double>,
    val total_volume: Map<String, Double>,
    val high_24h: Map<String, Double>,
    val low_24h: Map<String, Double>
)