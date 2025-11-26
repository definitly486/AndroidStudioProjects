package com.example.app

import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.File

/**
 * Установка модуля APatch/KSU из публичной папки "Загрузки".
 * Полностью удалена логика проверки разрешений — установка запускается всегда.
 */
class KernelSetupScript(private val activity: ComponentActivity) {

    companion object {
        const val MODULE_FILE_NAME = "APatch-KSU.zip"
    }

    /**
     * Старт установки без каких-либо проверок разрешений.
     */
    fun startInstall() {
        installFromDownload()
    }

    /**
     * Ищет ZIP в папке "Загрузки" и запускает root-установку.
     */
    fun installFromDownload() {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

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

    /**
     * Выполняет установку через root.
     */
    private fun installViaRoot(zipFile: File) {
        val moduleId = MODULE_FILE_NAME.removeSuffix(".zip")
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
            val stdout = p.inputStream.bufferedReader().readText().trim()
            val stderr = p.errorStream.bufferedReader().readText().trim()

            when {
                exit == 0 && stdout.contains("END_OF_INSTALL") ->
                    Toast.makeText(activity, "Модуль установлен! Перезагрузка обязательна.", Toast.LENGTH_LONG).show()

                stdout.contains("NO_UNZIP") ->
                    Toast.makeText(activity, "Ошибка: unzip отсутствует. Установи busybox.", Toast.LENGTH_LONG).show()

                stderr.isNotEmpty() ->
                    Toast.makeText(activity, "Ошибка установки: $stderr", Toast.LENGTH_LONG).show()

                else ->
                    Toast.makeText(activity, "Неизвестная ошибка root (код $exit).", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(activity, "Root недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
