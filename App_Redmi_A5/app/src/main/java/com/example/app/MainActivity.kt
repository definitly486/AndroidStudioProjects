package com.example.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.app.adapters.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // === СОЗДАНИЕ ФАЙЛА (твой код) ===
        if (this.savePackagesToFile("packages.txt")) {
            Toast.makeText(this, "Файл успешно создан.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка при создании файла.", Toast.LENGTH_SHORT).show()
        }

        // === НАСТРОЙКА ViewPager2 и TabLayout ===
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabs = findViewById<TabLayout>(R.id.tabs) // ID из <include android:id="@+id/tabs" ...>

        // Адаптер для фрагментов
        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        viewPager.adapter = sectionsPagerAdapter

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Первая"
                1 -> "Вторая"
                2 -> "Третья"
                3 -> "Пятая"
                4 -> "Шестая"
                5 -> "Седьмая"
                else -> "Вкладка ${position + 1}"
            }
        }.attach()
    }

    // === ТВОЯ ФУНКЦИЯ (добавь её в класс, если ещё не добавлена) ===
    private fun AppCompatActivity.savePackagesToFile(fileName: String): Boolean {
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
}