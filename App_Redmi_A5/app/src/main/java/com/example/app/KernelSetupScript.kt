package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Класс выполняет установку модуля из файла в папке "Загрузки".
 *
 * Важно: этот класс НЕ выполняет запрос разрешений — это обязанность вызывающего (Fragment / Activity).
 */
class KernelSetupScript(private val activity: ComponentActivity) {

    // Имя ZIP-файла в публичной папке "Загрузки"
    companion object {
        const val MODULE_FILE_NAME = "APatch-KSU.zip" // поменяй при необходимости
    }

    /**
     * Проверяет, есть ли у приложения разрешение на чтение внешнего хранилища.
     * Замечание: поведение на Android 11+ может отличаться — лучше использовать SAF в будущем.
     */
    fun hasStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Запустить установку. Если разрешения нет, вызывающий должен запросить его (requestPermission lambda).
     * requestPermission — лямбда для запуска ActivityResultLauncher (запрос разрешения).
     */
    fun startInstall(requestPermission: (() -> Unit)? = null) {
        if (hasStoragePermission()) {
            installFromDownload()
        } else {
            requestPermission?.invoke()
        }
    }

    /**
     * Пытается найти ZIP в публичной папке Загрузки и запустить установку через root.
     * Этот метод публичный, чтобы фрагмент мог вызывать его после получения разрешения.
     */
    fun installFromDownload() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val zipFile = File(downloadDir, MODULE_FILE_NAME)

        if (!zipFile.exists()) {
            Toast.makeText(
                activity,
                "Файл «$MODULE_FILE_NAME» не найден в папке Загрузки!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        installViaRoot(zipFile)
    }

    private fun installViaRoot(zipFile: File) {
        val moduleId = MODULE_FILE_NAME.removeSuffix(".zip")

        // Собираем команду — учитываем, что unzip может отсутствовать на некоторых прошивках
        // Поэтому сначала пытаемся распаковать через toybox/unzip, если нет — попытаемся через busybox unzip.
        val zipPathEscaped = zipFile.absolutePath.replace("\"", "\\\"")
        val cmd = """
            rm -rf /data/adb/modules/$moduleId
            mkdir -p /data/adb/modules/$moduleId
            if command -v unzip >/dev/null 2>&1; then
                unzip -o "$zipPathEscaped" -d /data/adb/modules/$moduleId
            elif command -v busybox >/dev/null 2>&1 && busybox unzip >/dev/null 2>&1; then
                busybox unzip -o "$zipPathEscaped" -d /data/adb/modules/$moduleId
            else
                echo "NO_UNZIP"
            fi
            # Создаём module.prop если его нет — лучше, чтобы он содержал необходимые поля
            if [ ! -f /data/adb/modules/$moduleId/module.prop ]; then
                touch /data/adb/modules/$moduleId/module.prop 2>/dev/null
            fi
            chmod -R 755 /data/adb/modules/$moduleId 2>/dev/null
            echo "END_OF_INSTALL"
        """.trimIndent()

        try {
            val p = Runtime.getRuntime().exec("su")
            p.outputStream.bufferedWriter().use {
                it.write(cmd)
                it.newLine()
                it.write("exit")
                it.newLine()
                it.flush()
            }

            val exit = p.waitFor()

            // читаем stdout/stderr для диагностических сообщений (не обязателен)
            val stdout = p.inputStream.bufferedReader().readText().trim()
            val stderr = p.errorStream.bufferedReader().readText().trim()

            if (exit == 0 && stdout.contains("END_OF_INSTALL")) {
                Toast.makeText(activity, "Модуль установлен успешно!\nПерезагрузи устройство", Toast.LENGTH_LONG).show()
            } else if (stdout.contains("NO_UNZIP") || stdout.contains("can't find") || stderr.isNotEmpty()) {
                Toast.makeText(activity, "Ошибка установки: отсутствует unzip (или произошла другая ошибка).", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Ошибка root (код $exit).", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Root недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
