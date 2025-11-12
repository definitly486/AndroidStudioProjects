package com.example.kodi

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {

    private val kodiUrl = "http://192.168.8.45:8081/jsonrpc"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val seekBar = findViewById<SeekBar>(R.id.seekBarVolume)
        val tvVolume = findViewById<TextView>(R.id.tvVolume)
        val play = findViewById<Button>(R.id.playbutton)
        val stop = findViewById<Button>(R.id.stopbutton)


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

        play.setOnClickListener {
            play()
        }

        stop.setOnClickListener {
            stop()
        }
    }

    private fun setKodiVolume(volume: Int) {
        lifecycleScope.launch {
            sendVolumeCommand(volume)
        }
    }

    private fun play () {

        lifecycleScope.launch {
            sendPlayCommand()
        }
    }

    private suspend fun sendPlayCommand() = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        // JSON body for Player.Open method to open the specified channel
        val json = """
        {"jsonrpc":"2.0","id":1,"method":"Player.Open","params":{"item":{"channelid":1}}}
    """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(kodiUrl)  // Make sure kodiUrl points to the correct IP address and port of your Kodi instance
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Error: ${response.code}")
                } else {
                    println("Play command sent successfully.")
                }
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
        }
    }



    private fun stop () {
        lifecycleScope.launch {
            sendStopCommand()
        }

    }

    private suspend fun sendStopCommand() = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        // JSON body
        val json = """
        {"jsonrpc": "2.0", "method": "Player.Stop", "params": { "playerid": 1 }, "id": 1}
    """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(kodiUrl)  // Make sure the kodiUrl is the correct IP address of your Kodi instance
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Error: ${response.code}")
                } else {
                    println("Stop command sent successfully.")
                }
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
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