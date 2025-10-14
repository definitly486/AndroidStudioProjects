
package com.example.coin

import kotlin.Result

class CryptoRepository {
    private val api = CryptoClient.api

    suspend fun getTopCryptos(): Result<List<CryptoCurrency>> {
        return try {
            Result.success(api.getTopCryptos())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCryptoDetail(id: String): Result<CryptoDetail> {
        return try {
            Result.success(api.getCryptoDetail(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}