package com.example.noip

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnToggle = findViewById(R.id.ipupdate)

        btnToggle.setOnClickListener {
            updateNoIp()
        }
    }

    private fun updateNoIp() {
        // Запускаем корутину в Main-потоке
        CoroutineScope(Dispatchers.Main).launch {
            // Показываем, что процесс пошёл
            btnToggle.isEnabled = false
            btnToggle.text = "Обновляю..."

            val result = withContext(Dispatchers.IO) {
                // Здесь твоя suspend-функция выполняется в фоне
                NoIpUpdater.updateIp(
                    username = "mylogin",
                    password = "my-update-token-123456",
                    hostname = "home.ddns.net",
                    ip = null // null — автоматически определит текущий IP
                )
            }

            // Возвращаемся в главный поток и показываем результат
            when (result) {
                is NoIpUpdater.UpdateResult.Success -> {
                    Toast.makeText(this@MainActivity, "IP успешно обновлён на ${result.ip}", Toast.LENGTH_LONG).show()
                }
                is NoIpUpdater.UpdateResult.NoChange -> {
                    Toast.makeText(this@MainActivity, "IP не изменился: ${result.ip}", Toast.LENGTH_SHORT).show()
                }
                is NoIpUpdater.UpdateResult.Error.BadAuth -> {
                    Toast.makeText(this@MainActivity, "Ошибка авторизации! Проверь логин/токен", Toast.LENGTH_LONG).show()
                }
                is NoIpUpdater.UpdateResult.Error.Abuse -> {
                    Toast.makeText(this@MainActivity, "Хост заблокирован (abuse). Подожди 30+ минут", Toast.LENGTH_LONG).show()
                }
                is NoIpUpdater.UpdateResult.Error.Network -> {
                    Toast.makeText(this@MainActivity, "Нет сети: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NoIpUpdater.UpdateResult.Error.ServerError -> {
                    Toast.makeText(this@MainActivity, "Сервер No-IP недоступен (код 911)", Toast.LENGTH_LONG).show()
                }
                else -> {
                    val msg = if (result is NoIpUpdater.UpdateResult.Error) result.message else "Неизвестная ошибка"
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                }
            }

            // Возвращаем кнопку в нормальное состояние
            btnToggle.isEnabled = true
            btnToggle.text = "Обновить IP"
        }
    }
}