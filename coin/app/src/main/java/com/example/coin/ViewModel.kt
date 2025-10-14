package com.example.coin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Result<List<CryptoCurrency>>.data: List<CryptoCurrency>?

class CryptoViewModel : ViewModel() {
    private val repository = CryptoRepository()

    private val _cryptoList = MutableLiveData<List<CryptoCurrency>>()
    val cryptoList: LiveData<List<CryptoCurrency>> = _cryptoList

    private val _selectedCrypto = MutableLiveData<CryptoDetail>()
    val selectedCrypto: LiveData<CryptoDetail> = _selectedCrypto

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadTopCryptos() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            when (val result = repository.getTopCryptos()) {
                is ApiResult.Success<*> -> {
                    _cryptoList.value = result.data as List<CryptoCurrency>?
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun loadCryptoDetail(id: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            when (val result = repository.getCryptoDetail(id)) {
                is ApiResult.Success<*> -> {
                    _selectedCrypto.value = result.data as CryptoDetail?
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }
}

class ApiResult(block: suspend (CoroutineScope) -> Unit) {

}
