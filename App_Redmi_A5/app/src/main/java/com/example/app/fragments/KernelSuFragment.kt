@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app.KernelSUInstaller
import com.example.app.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KernelSuFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

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
        val installButton = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installButton.setOnClickListener {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            Log.d("KernelInstaller", "[$time] Нажата кнопка установки APatch-KSU.zip")

            Toast.makeText(requireContext(), "Установка APatch-KSU…", Toast.LENGTH_LONG).show()

            Thread {
                val success = KernelSUInstaller.installAPatchKSU()

                activity?.runOnUiThread {
                    if (success) {
                        // Показываем диалог с предложением перезагрузки
                        AlertDialog.Builder(requireContext())
                            .setTitle("Установка завершена")
                            .setMessage("APatch-KSU успешно установлен!\n\nПерезагрузить устройство сейчас?")
                            .setPositiveButton("Перезагрузить") { _, _ ->
                                try {
                                    Runtime.getRuntime().exec("su -mm -c reboot")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "Не удалось выполнить перезагрузку",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Позже", null)
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: APatch-KSU.zip не найден в папке Download\nили установка провалилась",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }

        // Кнопка скачивания APatch-KSU.zip
        view.findViewById<Button>(R.id.downloadksuzip).setOnClickListener {
            downloadHelper.downloadToPublic(
                "https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip"
            )
            Toast.makeText(requireContext(), "Скачивание APatch-KSU.zip начато…", Toast.LENGTH_SHORT).show()
        }

        //Кнопка распаковки и установки KernelSU
        view.findViewById<Button>(R.id.install_kermelsu).setOnClickListener {
            installKernelSuManager(requireContext())
        }
    }
    // Функция распаковки apk
    private val TAG = "KernelSU_Installer"

    private fun installKernelSuManager(context: Context) {
        val files = extractAssetsFiles(
            context,
            listOf(
                "KernelSU_v1.0.5_12081-release.apk" to 2_000_000L,
                "APatch-KSU.zip" to 1_000_000L
            )
        )

        val apkFile = files.firstOrNull { it.name == "KernelSU_v1.0.5_12081-release.apk" }

        if (apkFile == null) {
            Log.e(TAG, "Нужный APK не найден")
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
        }

        context.startActivity(intent)
    }

    private fun extractAssetsFiles(
        context: Context,
        files: List<Pair<String, Long>> // имя, минимальный размер
    ): List<File> {

        val out = mutableListOf<File>()

        for ((name, minSize) in files) {
            val target = File(context.cacheDir, name)

            if (target.exists() && target.length() > minSize) {
                out.add(target)
                continue
            }

            if (target.exists()) target.delete()

            try {
                context.assets.open(name).use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                out.add(target)
            } catch (e: Exception) {
                target.delete()
            }
        }

        return out
    }

}