package com.example.app.fragments

import DownloadHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.KernelSetupScript
import com.example.app.R

@Suppress("SpellCheckingInspection")
class KernelSuFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    private lateinit var kernelScript: KernelSetupScript

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kernelScript = KernelSetupScript(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        val installApatchKsu = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installApatchKsu.setOnClickListener {

            // Метка времени для логов
            val timeStamp = System.currentTimeMillis()

            // Логируем нажатие кнопки
            android.util.Log.d("KernelInstaller", "[$timeStamp] Нажата кнопка установки APatch-KSU.zip")

            // Покажем пользователю, что процесс начат
            Toast.makeText(requireContext(), "[$timeStamp] Запуск установки…", Toast.LENGTH_SHORT).show()

            // Реальный запуск установки
            kernelScript.startInstall()
        }

        // Кнопка скачивания ksuzip
        val downloadksuzip = view.findViewById<Button>(R.id.downloadksuzip)
        downloadksuzip.setOnClickListener { downloadKSUZip() }
    }

    private fun downloadKSUZip() {
        downloadHelper.downloadToPublic("https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip")

    }
}