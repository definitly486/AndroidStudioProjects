@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.KernelSUInstaller
import com.example.app.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

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
            extractApk(requireContext())
            installAutoAPK(requireContext())
        }


    }
    // Функция распаковки apk
    fun extractApk(context: Context) {
        val assetManager = context.assets
        val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destinationFile = File(externalDir, "KernelSU_v1.0.5_12081-release.apk")

        if (!destinationFile.exists()) { // Проверяем существование файла
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = assetManager.open("KernelSU_v1.0.5_12081-release.apk") // Читаем файл из assets
                outputStream = FileOutputStream(destinationFile)     // Записываем в Destination

                val buffer = ByteArray(8 * 1024)
                var readBytes: Int
                while (true) {
                    readBytes = inputStream.read(buffer)
                    if (readBytes <= 0) break
                    outputStream.write(buffer, 0, readBytes)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        }
    }
    private val LOG_TAG = "ExtractApkLogger"
    private fun installAutoAPK(context: Context?) {
        if (context == null) {
            Log.e(LOG_TAG, "Context is null → автоустановка отменена")
            return
        }

        val packageManager = context.packageManager

        // Список APK-файлов (можно менять как угодно)
        val apks = listOf(

            "KernelSU_v1.0.5_12081-release.apk",

        )

        // Путь к папке с APK (оставил твой оригинальный, но с безопасным созданием)
        val appApkDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            ""
        ).also { dir ->
            if (!dir.exists()) {
                val created = dir.mkdirs()
                Log.i(LOG_TAG, if (created) "Директория создана: ${dir.absolutePath}"
                else "Не удалось создать директорию: ${dir.absolutePath}")
            }
        }

        Log.i(LOG_TAG, "Запуск автоустановки APK из: ${appApkDir.absolutePath}")

        for (apkFileName in apks) {
            val apkFile = File(appApkDir, apkFileName)

            if (!apkFile.exists()) {
                Log.w(LOG_TAG, "Файл не найден → пропуск: $apkFileName")
                continue
            }




            try {
                // Временно отключаем SELinux (если нужно на твоём устройстве)
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setenforce 0"))

                // pm install -r = переустановка, если вдруг старая версия есть (на всякий случай)
                val cmd = "pm install -r \"${apkFile.absolutePath}\""
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))

                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Log.i(LOG_TAG, "Успешно установлен: $apkFileName ")
                } else {
                    val error = process.errorStream.bufferedReader().use { it.readText() }.trim()
                    Log.e(LOG_TAG, "Ошибка установки $apkFileName (код $exitCode): $error")
                }

                // Возвращаем SELinux в безопасное состояние
                Runtime.getRuntime().exec(arrayOf("su", "-c", "setenforce 1")).waitFor()

            } catch (e: Exception) {
                Log.e(LOG_TAG, "Исключение при установке $apkFileName", e)
            }

            // Задержка между установками (чтобы система не захлебнулась)
            Thread.sleep(700)
        }

        Log.i(LOG_TAG, "Автоустановка APK завершена.")
    }


}