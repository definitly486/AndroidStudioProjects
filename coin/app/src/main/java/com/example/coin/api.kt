package com.example.coin

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CryptoApiService {

    @GET("coins/markets")
    suspend fun getTopCryptos(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CryptoCurrency>

    @GET("coins/{id}")
    suspend fun getCryptoDetail(
        @Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): CryptoDetail

    @GET("simple/price")
    suspend fun getSpecificPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") currencies: String = "usd,eur,rub"
    ): Map<String, Map<String, Double>>
}