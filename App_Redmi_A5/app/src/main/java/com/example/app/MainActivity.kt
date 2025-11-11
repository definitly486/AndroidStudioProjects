package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // === СОЗДАНИЕ ФАЙЛА packages.txt ===
        if (savePackagesToFile("packages.txt")) {
            Toast.makeText(this, "Файл packages.txt успешно создан.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка при создании файла.", Toast.LENGTH_SHORT).show()
        }

        // === НАСТРОЙКА КНОПОК ===
        setupActionButtons()
    }

    // === Функция создания файла с пакетами ===
    private fun savePackagesToFile(fileName: String): Boolean {
        return try {
            val packages = packageManager.getInstalledPackages(0)
            val data = packages.joinToString("\n") { it.packageName }
            openFileOutput(fileName, MODE_PRIVATE).use { output ->
                output.write(data.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // === НОВАЯ ФУНКЦИЯ: Создание списка кнопок вместо табов ===
    private fun setupActionButtons() {
        val buttonsContainer = findViewById<LinearLayout>(R.id.buttonsContainer)
        val buttonTitles = listOf(
            "Первая",
            "Вторая",
            "Третья",
            "Пятая",
            "Шестая",
            "Седьмая"
        )

        buttonTitles.forEachIndexed { index, title ->
            val button = Button(this).apply {
                text = title
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8.dpToPx())
                }
                setPadding(16.dpToPx())
                setOnClickListener {
                    onButtonClicked(title, index)
                }
            }
            buttonsContainer.addView(button)
        }
    }

    // === Обработчик нажатия на кнопку ===
    private fun onButtonClicked(title: String, position: Int) {
        Toast.makeText(this, "Нажата: $title (позиция: $position)", Toast.LENGTH_SHORT).show()

        // Здесь можно открыть нужный фрагмент или выполнить действие
        when (position) {
            0 -> showToast("Открываем Первый фрагмент")
            1 -> showToast("Открываем Второй фрагмент")
            2 -> showToast("Открываем Третий фрагмент")
            3 -> showToast("Открываем Пятый фрагмент")
            4 -> showToast("Открываем Шестой фрагмент")
            5 -> showToast("Открываем Седьмой фрагмент")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // === Утилита: dp → px ===
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}