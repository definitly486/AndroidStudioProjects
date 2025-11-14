package com.example.vcore

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val textView : TextView = findViewById(R.id.textView)
        val textView2 : TextView = findViewById(R.id.textView2)

        fun getfio5() {

            val url = "https://api.binance.com/api/v3/ticker/price?symbol=FIOUSDT"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            println(Thread.currentThread())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        System.err.println("Response not successful")
                        return
                    }
                    val json = response.body!!.string()
                    val jsonArray = JSONObject(json)
                    val name = jsonArray.getString("price")
                    applicationContext.openFileOutput("fio.txt", Context.MODE_PRIVATE).use()
                    {
                        it.write(name.toByteArray())
                    }
                }

            })

        }



        fun getbtc() {

            val url = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()

            println(Thread.currentThread())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        System.err.println("Response not successful")
                        return
                    }
                    val json = response.body!!.string()
                    val jsonArray = JSONObject(json)
                    val name = jsonArray.getString("price")
                    applicationContext.openFileOutput("btc.txt", Context.MODE_PRIVATE).use()
                    {
                        it.write(name.toByteArray())
                    }
                }

            })

        }




        val myButton3: Button = findViewById(R.id.buttonread)
        myButton3.setOnClickListener {
            myButton3.setBackgroundColor(Color.GREEN);
            Toast.makeText(this, "READ", Toast.LENGTH_SHORT).show()
            getfio5()
            getbtc()
         TimeUnit.SECONDS.sleep(2L)
            val fio = File("/data/data/com.example.vcore/files/fio.txt").readText()
            val btc = File("/data/data/com.example.vcore/files/btc.txt").readText()
            textView.text = fio
            textView2.text = btc
        }


    }
}

