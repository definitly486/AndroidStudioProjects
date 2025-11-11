package com.example.kodi

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.kodi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {

    private val kodiUrl = "http://192.168.8.45:8081/jsonrpc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val seekBar = findViewById<SeekBar>(R.id.seekBarVolume)
        val tvVolume = findViewById<TextView>(R.id.tvVolume)

        tvVolume.text = "${seekBar.progress}%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvVolume.text = "$progress%"
                    setKodiVolume(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setKodiVolume(volume: Int) {
        lifecycleScope.launch {
            sendVolumeCommand(volume)
        }
    }

    private suspend fun sendVolumeCommand(volume: Int) = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = """
            {"jsonrpc":"2.0","id":1,"method":"Application.SetVolume","params":{"volume":$volume}}
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(kodiUrl).post(body).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) println("Ошибка: ${response.code}")
            }
        } catch (e: Exception) {
            println("Ошибка сети: ${e.message}")
        }
    }
}