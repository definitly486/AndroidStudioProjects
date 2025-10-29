package com.example.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    return context.getExternalFilesDir("shared")
}

// Скачивание файла
fun download(context: Context, url: String) {
    val folder = getDownloadFolder(context) ?: return
    if (!folder.exists()) folder.mkdirs()

    val lastPart = url.split("/").last()
    val gpgFile = File(folder, lastPart)

    if (gpgFile.exists()) {
        Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Начинается загрузка...", Toast.LENGTH_SHORT).show()
            }

            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(lastPart)
            request.setDescription("Загружается...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(
                context,
                "shared",  // Папка "shared"
                lastPart
            )

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadID = downloadManager.enqueue(request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка при загрузке: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// Функция расшифровки и распаковки архива
suspend fun decryptAndExtractArchive(context: Context, archiveName: String, password: String) {
    val folder = context.getExternalFilesDir("shared")
    val encryptedFilePath = "${folder!!.absolutePath}/${archiveName}.tar.enc"
    val decryptedFilePath = "${folder.absolutePath}/${archiveName}.tar"
    val appDirectoryPath = folder.absolutePath

    if (!folder.exists()) {
        folder.mkdirs()
    }

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

        copyprofile(context,archiveName)
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

// Универсальный метод копирования профиля
fun copyprofile(context: Context, appPackageName: String) {
    val folder = context.getExternalFilesDir("shared")

    fun showCompletionDialoginstall() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка root")
        builder.setMessage("Root доступ отсутствует, приложения не будут установлены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    if (RootChecker.hasRootAccess(context)) {
        showToastOnMainThread(context, "Устройство имеет root-доступ.")
    } else {
        showCompletionDialoginstall()
        return
    }

    fun showCompletionDialogsystem() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка записи в system")
        builder.setMessage("Запись в system невозможна, приложения не будут установлены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Проверка возможности записи в папку '/system'
    val pathToCheck = "/system"
    if (!RootChecker.checkWriteAccess(pathToCheck)) {
        showCompletionDialogsystem()
        return
    }


    if ("$appPackageName" == "org.thunderdog.challegram") {

        "su - root -c   ls -l   /data_mirror/data_ce/null/0/$appPackageName/files/tdlib/td.binlog"

    }


    val ownerCmd =
        "su - root -c   ls -l /data/data/ | grep $appPackageName | head -n 1 | awk '{print$3}'"
    val fileOwner = execShell(ownerCmd)?.trim() ?: ""
    showToastOnMainThread(context, "ID $fileOwner")
    val commands = arrayOf(
        "su - root -c cp  -R ${folder!!.absolutePath}/$appPackageName  /data_mirror/data_ce/null/0",
        "su - root -c chown -R  $fileOwner:$fileOwner  /data_mirror/data_ce/null/0/$appPackageName/"
    )

    for (command in commands) {
        CoroutineScope(Dispatchers.IO).launch {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            if (process.exitValue() != 0) {
                showToastOnMainThread(context, "Ошибка при копировании $appPackageName: $command")
                return@launch
            }
        }
    }

    showToastOnMainThread(context, "Копирование $appPackageName завершено")
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