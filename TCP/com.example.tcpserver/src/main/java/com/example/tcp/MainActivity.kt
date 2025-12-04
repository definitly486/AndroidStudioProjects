
com.example.tcpserver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tcpserver.TcpServer
import com.example.tcpserver.TcpServerService


class MainActivity : AppCompatActivity() {

    private var isServerRunning = false

    private lateinit var btnToggle: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvAddress: TextView

    // ← Добавь это поле
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("command") ?: return
            runOnUiThread {
                when (command.uppercase()) {
                    "LED_ON" -> Toast.makeText(this@MainActivity, "LED ON", Toast.LENGTH_LONG).show()
                    "LED_OFF" -> Toast.makeText(this@MainActivity, "LED OFF", Toast.LENGTH_LONG).show()
                    else -> Toast.makeText(this@MainActivity, "Команда: $command", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        btnToggle = findViewById(R.id.button)
        tvStatus = findViewById(R.id.tv_status)
        tvAddress = findViewById(R.id.tv_address)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateUi()

        btnToggle.setOnClickListener {
            if (isServerRunning) stopServer() else startServer()
        }

        // ← Регистрация приёмника с правильным флагом
        val filter = IntentFilter("com.example.tcp.COMMAND_RECEIVED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(commandReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(commandReceiver, filter)
        }
    }

    private fun startServer() {
        val intent = Intent(this, TcpServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServerRunning = true
        updateUi()

        val ip = TcpServer.getIp()
        tvAddress.text = "Адрес: http://$ip:9000"
        Toast.makeText(this, "Сервер запущен на порту 9000", Toast.LENGTH_LONG).show()
    }

    private fun stopServer() {
        stopService(Intent(this, TcpServerService::class.java))
        isServerRunning = false
        updateUi()
        tvAddress.text = "Адрес: —"
        Toast.makeText(this, "Сервер остановлен", Toast.LENGTH_SHORT).show()
    }

    private fun updateUi() {
        if (isServerRunning) {
            btnToggle.text = "Остановить сервер"
            tvStatus.text = "Статус: Работает"
            tvStatus.setTextColor(android.graphics.Color.GREEN)
        } else {
            btnToggle.text = "Запустить сервер"
            tvStatus.text = "Статус: Остановлен"
            tvStatus.setTextColor(android.graphics.Color.RED)
        }
    }

    override fun onDestroy() {
        // Обязательно отписываемся!
        try {
            unregisterReceiver(commandReceiver)
        } catch (e: Exception) { /* уже отписан */ }
        if (isServerRunning) stopServer()
        super.onDestroy()
    }
}