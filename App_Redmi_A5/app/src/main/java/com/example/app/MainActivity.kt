package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.fragments.FifthFragment
import com.example.app.fragments.FirstFragment
import com.example.app.fragments.SecondFragment
import com.example.app.fragments.SeventhFragment
import com.example.app.fragments.SixthFragment
import com.example.app.fragments.ThirdFragment

class MainActivity : AppCompatActivity() {

    // Список твоих фрагментов (в порядке кнопок)
    private val fragmentList = listOf<Fragment>(
        FirstFragment(),
        SecondFragment(),
        ThirdFragment(),
        FifthFragment(),
        SixthFragment(),
        SeventhFragment()
    )

    private val buttonTitles = listOf(
        "Первая", "Вторая", "Третья", "Пятая", "Шестая", "Седьмая"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // === Создание файла packages.txt ===
        if (savePackagesToFile("packages.txt")) {
            Toast.makeText(this, "Файл packages.txt создан.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка создания файла.", Toast.LENGTH_SHORT).show()
        }

        // === Настройка кнопок ===
        setupActionButtons()

        // Открываем первый фрагмент при запуске
        if (savedInstanceState == null) {
            openFragment(fragmentList[0], buttonTitles[0])
        }
    }

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

    private fun setupActionButtons() {
        val buttonsContainer = findViewById<LinearLayout>(R.id.buttonsContainer)

        buttonTitles.forEachIndexed { index, title ->
            val button = Button(this).apply {
                text = title
                textSize = 13f  // Компактный текст

                minHeight = 0
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    36.dpToPx()  // Компактная высота
                ).apply {
                    setMargins(0, 0, 0, 4.dpToPx())  // Меньше между кнопками
                }

                setPadding(10.dpToPx())  // Внутренний отступ
                setBackgroundResource(android.R.drawable.btn_default)
            }

            button.setOnClickListener {
                openFragment(fragmentList[index], title)
            }

            buttonsContainer.addView(button)
        }
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setReorderingAllowed(true)
            .addToBackStack(title) // Чтобы Back возвращал к нужной кнопке
            .commit()
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}