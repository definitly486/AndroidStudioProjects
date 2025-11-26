package com.example.app.fragments

import DownloadHelper
import KernelSUInstaller  // ← Добавь этот импорт!
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import java.text.SimpleDateFormat
import java.util.*

class KernelSuFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    // Убираем старый kernelScript, если он больше не нужен
    // private lateinit var kernelScript: KernelSetupScript

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadHelper = DownloadHelper(requireContext())
        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        val installApatchKsu = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installApatchKsu.setOnClickListener {
            val timeStamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                .format(Date())

            Log.d("KernelInstaller", "[$timeStamp] Нажата кнопка установки APatch-KSU.zip")

            Toast.makeText(requireContext(), "Запуск установки APatch-KSU…", Toast.LENGTH_LONG).show()

            // Запуск установки в отдельном потоке (чтобы UI не вис)
            Thread {
                val success = KernelSUInstaller.installAPatchKSU()

                activity?.runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "APatch-KSU успешно установлен! Перезагружаю…", Toast.LENGTH_LONG).show()
                        // Автоматическая перезагрузка
                        try {
                            Runtime.getRuntime().exec("su -mm -c reboot")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Ошибка: APatch-KSU.zip не найден в Download или установка провалилась", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        // Кнопка скачивания
        val downloadksuzip = view.findViewById<Button>(R.id.downloadksuzip)
        downloadksuzip.setOnClickListener {
            downloadKSUZip()
        }
    }

    private fun downloadKSUZip() {
        downloadHelper.downloadToPublic("https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip")
    }
}