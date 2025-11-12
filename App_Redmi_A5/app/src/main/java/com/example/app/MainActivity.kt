package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.fragments.*

class MainActivity : AppCompatActivity() {

    private val fragmentList = listOf<Fragment>(
        FirstFragment(),
        SecondFragment(),
        ThirdFragment(),
        FifthFragment(),
        SixthFragment(),
        SeventhFragment(),
        NinthFragment()
    )

    private val buttonTitles = listOf(
        "Первая", "Вторая", "Третья", "Пятая", "Шестая", "Седьмая" ,"Восьмая"
    )

    private var selectedButton: Button? = null
    private lateinit var buttonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonsContainer = findViewById(R.id.buttonsContainer)

        // Создаём файл packages.txt
        if (savePackagesToFile("packages.txt")) {
            Toast.makeText(this, "Файл packages.txt создан.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка создания файла.", Toast.LENGTH_SHORT).show()
        }

        // Настраиваем кнопки
        setupActionButtons(savedInstanceState)

        // Открываем первый фрагмент, если первый запуск
        if (savedInstanceState == null) {
            openFragment(fragmentList[0], buttonTitles[0])
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем индекс выбранной кнопки
        val selectedIndex = selectedButton?.let { button ->
            buttonsContainer.indexOfChild(button)
        } ?: 0
        outState.putInt("SELECTED_BUTTON_INDEX", selectedIndex)
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

    private fun setupActionButtons(savedInstanceState: Bundle?) {
        buttonsContainer.removeAllViews()

        // Определяем, какую кнопку подсветить
        val savedIndex = savedInstanceState?.getInt("SELECTED_BUTTON_INDEX", 0) ?: 0

        buttonTitles.forEachIndexed { index, title ->
            val button = Button(this).apply {
                text = title
                textSize = 13f
                minHeight = 0
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    36.dpToPx()
                ).apply {
                    setMargins(0, 0, 0, 4.dpToPx())
                }
                setPadding(10.dpToPx())
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_selector)
            }

            button.setOnClickListener {
                // Сбрасываем старую подсветку
                selectedButton?.isSelected = false

                // Подсвечиваем новую
                button.isSelected = true
                selectedButton = button

                openFragment(fragmentList[index], title)
            }

            buttonsContainer.addView(button)

            // Подсвечиваем нужную кнопку
            if (index == savedIndex) {
                button.isSelected = true
                selectedButton = button
            }
        }

        // Открываем нужный фрагмент при восстановлении
        if (savedIndex in fragmentList.indices) {
            openFragment(fragmentList[savedIndex], buttonTitles[savedIndex])
        }
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setReorderingAllowed(true)
            .addToBackStack(title)
            .commit()
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}