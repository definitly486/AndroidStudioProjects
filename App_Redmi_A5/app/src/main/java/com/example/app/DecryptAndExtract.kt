package com.example.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.app.fragments.RootChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.ProcessBuilder
import java.lang.RuntimeException



// Вспомогательные функции

// Получение директории загрузки
fun getDownloadFolder(context: Context): File? {
    return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}

// Загрузка профиля

// Функция расшифровки и распаковки архива
suspend fun decryptAndExtractArchive(context: Context, password: String) {
    val encryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/Download/com.qflair.browserq.tar.enc"
    val decryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/com.qflair.browserq.tar"
    val appDirectoryPath = "/storage/emulated/0/Android/data/com.example.app/files"

    try {
        // Расшифровка файла
        val processDecrypt = ProcessBuilder(
            "openssl",
            "enc",
            "-aes-256-cbc",
            "-pbkdf2",
            "-iter",
            "100000",
            "-d",
            "-in",
            encryptedFilePath,
            "-out",
            decryptedFilePath,
            "-pass",
            "pass:$password"
        ).start()

        processDecrypt.waitFor()

        if (!File(decryptedFilePath).exists()) {
            throw RuntimeException("Расшифровка прошла неудачно.")
        }

        // Распаковка архива
        val processUnpack = ProcessBuilder(
            "busybox",
            "tar",
            "xf",
            decryptedFilePath,
            "-C",
            appDirectoryPath
        ).start()

        processUnpack.waitFor()

        withContext(Dispatchers.Main) {
            showToastOnMainThread(context, "Архив успешно установлен!")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            showToastOnMainThread(context, "Ошибка при установке: ${e.message}")
        }
    }
}

fun copyprofile(context: Context) {


    fun showCompletionDialoginstall() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка root")
        builder.setMessage("Root доступ отсуствует,приложения не будут установлены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }


    if (RootChecker.hasRootAccess(context)) {

        Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
            .show()
    } else {
        showCompletionDialoginstall()
        return
    }

    Toast.makeText(context, "Копируем redmia5-main ...", Toast.LENGTH_SHORT).show()

    val ownerCmd =
        "su - root -c   ls -l   /data_mirror/data_ce/null/0/com.termos | awk '{print $3}' | head -n 2"
    val fileOwner = execShell(ownerCmd)?.trim() ?: ""

    val commands = arrayOf(

        "su - root -c cp  -R /storage/emulated/0/Android/data/com.example.app/files/Download/redmia5-main /data_mirror/data_ce/null/0/com.termos/files/home",
        "su - root -c chmod -R 0755 /data_mirror/data_ce/null/0/com.termos/files/home",
        "su - root -c chown -R  $fileOwner:$fileOwner /data_mirror/data_ce/null/0/com.termos/files/home/redmia5-main"
    )

    var process: Process?

    for (command in commands) {
        process = Runtime.getRuntime().exec(command)
        process.waitFor() // Wait for the command to finish
        if (process.exitValue() != 0) {
            Toast.makeText(context, "Ошибка при копирование main: $command", Toast.LENGTH_LONG)
                .show()
            return
        }
    }
    Toast.makeText(context, "Копирование  main завершенo", Toast.LENGTH_SHORT).show()
}

private fun execShell(cmd: String): String? {
    try {
        val process = Runtime.getRuntime().exec(cmd)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw Exception("Ошибка при выполнении команды: $cmd")
        }

        val outputStream = BufferedReader(InputStreamReader(process.inputStream))
        val resultBuilder = StringBuilder()
        while (true) {
            val line = outputStream.readLine() ?: break
            resultBuilder.append(line).append("\n")
        }
        return resultBuilder.toString().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// Вспомогательная функция для показа Toast на главном потоке
fun showToastOnMainThread(context: Context, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}