package com.example.app.fragments

import DownloadHelper
import android.annotation.SuppressLint

import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app.R


class FirstFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        // Инициализация
        downloadHelper = DownloadHelper(requireContext())

        val installButton = view.findViewById<Button>(R.id.installfm)
        installButton.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/FM+v3.6.3.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installtermos = view.findViewById<Button>(R.id.installtermos)
        installtermos.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Termos_v2.4_universal.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installaurora = view.findViewById<Button>(R.id.installaurora)
        installaurora.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/com.aurora.store_70.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadHelper.cleanup()
    }
}