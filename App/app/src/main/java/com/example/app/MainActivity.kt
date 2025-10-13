package com.example.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.app.adapters.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Найти ViewPager2 по его ID
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Установить адаптер для ViewPager2
        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        viewPager.adapter = sectionsPagerAdapter

        // Получить ссылку на TabLayout
        val tabs = findViewById<TabLayout>(R.id.tabs)

        // Связать TabLayout с ViewPager2
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Первая"
                1 -> "Вторая"
                2 -> "Третья"
                3 -> "Четвертая"
                else -> ""
            }
        }.attach()
    }

}
