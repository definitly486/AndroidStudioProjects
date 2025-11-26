package com.example.app

import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.File

/**
 * Установка APatch/KSU с правильным ID = read-write.
 */
class KernelSetupScript(private val activity: ComponentActivity) {

    companion object {
        const val MODULE_FILE_NAME = "APatch-KSU.zip"
        const val MODULE_ID = "read-write"   // <<< ВАЖНО: правильный id для модуля
    }

    /**
     * Главный запуск.
     */
    fun startInstall() {
        autoGrantPermissions()
        installFromDownload()
    }

    /**
     * Авто-выдача разрешений через root.
     */
    private fun autoGrantPermissions() {
        val pkg = activity.packageName

        val permissions = listOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.POST_NOTIFICATIONS"
        )

        val cmds = permissions.joinToString("\n") { perm ->
            "pm grant $pkg $perm 2>/dev/null"
        } + "\necho PERM_DONE"

        try {
            val p = Runtime.getRuntime().exec("su")
            p.outputStream.bufferedWriter().use {
                it.write(cmds)
                it.newLine()
                it.write("exit")
                it.newLine()
                it.flush()
            }

            val output = p.inputStream.bufferedReader().readText()

            if (output.contains("PERM_DONE")) {
                Toast.makeText(activity, "Разрешения автоматически выданы", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(activity, "Не удалось выдать разрешения", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(activity, "Root недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Ищет ZIP в Загрузках.
     */
    private fun installFromDownload() {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val zipFile = File(downloadDir, MODULE_FILE_NAME)

        if (!zipFile.exists()) {
            Toast.makeText(
                activity,
                "Файл «$MODULE_FILE_NAME» не найден!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        installViaRoot(zipFile)
    }

    /**
     * Установка модуля через root.
     */
    private fun installViaRoot(zipFile: File) {
        val moduleDir = "/data/adb/modules/$MODULE_ID"
        val zipPathEscaped = zipFile.absolutePath.replace("\"", "\\\"")

        val cmd = """
        rm -rf "$moduleDir"
        mkdir -p "$moduleDir"

        # Распаковка ZIP
        if command -v unzip >/dev/null 2>&1; then
            unzip -o "$zipPathEscaped" -d "$moduleDir"
        elif command -v busybox >/dev/null 2>&1 && busybox unzip >/dev/null 2>&1; then
            busybox unzip -o "$zipPathEscaped" -d "$moduleDir"
        else
            echo "NO_UNZIP"
        fi

        # Удаление META-INF (обязательно)
        rm -rf "$moduleDir/META-INF"

        # Создаём правильный module.prop
        cat <<EOF > "$moduleDir/module.prop"
id=read-write
name=APatch-KSU可读写
version=2.0
versionCode=1
author=idyll™2018
description=[运行中😊]📲APatch与KSU通过OverlayFS实现分区可读写，通过删除/data/adb/modules/.rw文件夹恢复！
EOF

        chmod -R 755 "$moduleDir"
        chmod 644 "$moduleDir/module.prop"

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
                    Toast.makeText(activity, "Модуль установлен! Требуется перезагрузка.", Toast.LENGTH_LONG).show()

                stdout.contains("NO_UNZIP") ->
                    Toast.makeText(activity, "Ошибка: отсутствует unzip/busybox.", Toast.LENGTH_LONG).show()

                stderr.isNotEmpty() ->
                    Toast.makeText(activity, "Root ошибка: $stderr", Toast.LENGTH_LONG).show()

                else ->
                    Toast.makeText(activity, "Неизвестная ошибка root (код $exit)", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(activity, "Root недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
