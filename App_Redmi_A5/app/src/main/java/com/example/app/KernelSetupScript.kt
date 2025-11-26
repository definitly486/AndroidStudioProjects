// KernelSetupScript.kt
package com.example.app// ← замени на свой пакет

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Класс для автоматической установки модуля KernelSU из папки Download одним нажатием
 *
 * Использование в Activity:
 *   val kernelSetup = KernelSetupScript(this)
 *   button.setOnClickListener { kernelSetup.installFromDownload() }
 */
class KernelSetupScript(private val activity: ComponentActivity) {

    // Имя файла модуля в папке Download (можно менять)
    private val MODULE_FILE_NAME = "MyModule.zip" // ← положи свой ZIP сюда

    // Альтернатива: если используешь уже распакованную папку
    // private val MODULE_FOLDER_NAME = "MyModule"

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) installFromDownload()
        else Toast.makeText(activity, "Доступ к файлам отклонён", Toast.LENGTH_LONG).show()
    }

    /**
     * Вызывай эту функцию по нажатию кнопки
     */
    fun installFromDownload() {
        if (!checkStoragePermission()) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            return
        }

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val moduleFile = File(downloadDir, MODULE_FILE_NAME)

        if (!moduleFile.exists()) {
            Toast.makeText(
                activity,
                "Файл не найден!\nПоложи $MODULE_FILE_NAME в папку Загрузки",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Опционально: открываем KernelSU
        try {
            val intent = activity.packageManager.getLaunchIntentForPackage("me.weishu.kernelsu")
            intent?.let { activity.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        } catch (_: Exception) {}

        installModuleViaSu(moduleFile)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun installModuleViaSu(zipFile: File) {
        val moduleId = MODULE_FILE_NAME.removeSuffix(".zip")

        val commands = """
            # Удаляем старый модуль (если был)
            rm -rf /data/adb/modules/$moduleId
            
            # Создаём директорию модуля
            mkdir -p /data/adb/modules/$moduleId
            
            # Распаковываем ZIP прямо в место KernelSU
            unzip -o '${zipFile.absolutePath}' -d /data/adb/modules/$moduleId
            
            # Делаем модуль активным
            touch /data/adb/modules/$moduleId/module.prop 2>/dev/null || true
            
            # Права на исполнение (на всякий случай)
            chmod -R 755 /data/adb/modules/$moduleId 2>/dev/null || true
            
            echo "Модуль $moduleId успешно установлен!"
        """.trimIndent()

        try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.bufferedWriter().use {
                it.write(commands)
                it.newLine()
                it.write("exit")
                it.flush()
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Toast.makeText(
                    activity,
                    "Модуль установлен!\nПерезагрузи устройство для применения",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(activity, "Ошибка root (код $exitCode)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Root недоступен или отказал:\n${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}