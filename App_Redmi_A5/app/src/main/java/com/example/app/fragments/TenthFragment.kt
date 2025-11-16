package com.example.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.app.BuildConfig
import com.example.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import com.example.app.fragments.RootChecker.hasRootAccess

class TenthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tenth, container, false)

        // 1. Преобразуем миллисекунды в дату
        val buildDate = Date(BuildConfig.BUILD_TIME)

        // 2. Форматируем в человекочитаемый вид
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val formattedTime = formatter.format(buildDate)

        // 3. Выводим в TextView
        val buildTimeText = rootView.findViewById<TextView>(R.id.buildTimeText)
        buildTimeText.text = "APK создан: $formattedTime"

        // Получаем ссылку на CheckBox
        val checkBox = rootView.findViewById<CheckBox>(R.id.checkBox)

        // Проверяем наличие root-доступа
        val hasRootAccess = hasRootAccess(requireContext()) // requireContext() вернёт непустой Context
        checkBox.isChecked = hasRootAccess

        return rootView
    }
}