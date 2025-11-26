// KernelSetupScript.kt
package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File

class KernelSetupScript(private val activity: ComponentActivity) {

    // <-- ИМЯ ТВОЕГО ZIP-ФАЙЛА В ПАПКЕ "Загрузки"
    private val MODULE_FILE_NAME = "APatch-KSU.zip"   // ← поменяй, если у тебя другое имя

    // Регистрация происходит сразу в конструкторе — это безопасно, если объект создаётся в onViewCreated
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) installFromDownload()
        else Toast.makeText(activity, "Доступ к файлам отклонён", Toast.LENGTH_LONG).show()
    }
    /** Вызывай эту функцию по нажатию кнопки */
    fun startInstall() {
        if (hasStoragePermission()) {
            installFromDownload()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun hasStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    private fun installFromDownload() {
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

        val cmd = """
            rm -rf /data/adb/modules/$moduleId
            mkdir -p /data/adb/modules/$moduleId
            unzip -o "${zipFile.absolutePath}" -d /data/adb/modules/$moduleId
            touch /data/adb/modules/$moduleId/module.prop 2>/dev/null
            chmod -R 755 /data/adb/modules/$moduleId 2>/dev/null
            echo "Модуль $moduleId установлен"
        """.trimIndent()

        try {
            val p = Runtime.getRuntime().exec("su")
            p.outputStream.bufferedWriter().use {
                it.write(cmd)
                it.newLine()
                it.write("exit")
                it.flush()
            }
            val code = p.waitFor()
            if (code == 0) {
                Toast.makeText(activity, "Модуль установлен успешно!\nПерезагрузи устройство", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Ошибка root (код $code)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Root недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}