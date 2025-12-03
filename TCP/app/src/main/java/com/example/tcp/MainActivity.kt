package com.example.tcp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val tcpServer = TcpServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Get the root view
        val mainView = findViewById<View>(R.id.main)

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize your button listener
        opentcpPort(mainView)
    }

    private fun opentcpPort(view: View) {
        val openPort = view.findViewById<Button>(R.id.button)

        openPort.setOnClickListener {

            // Запуск сервера
            tcpServer.startServer(5000) { command ->
                when (command) {
                    "LED_ON" -> runOnUiThread {
                        Toast.makeText(this, "Команда: включить", Toast.LENGTH_SHORT).show()
                    }

                    "LED_OFF" -> runOnUiThread {
                        Toast.makeText(this, "Команда: выключить", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        // неизвестная команда
                    }
                }
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tcpServer.stopServer()
    }
}
