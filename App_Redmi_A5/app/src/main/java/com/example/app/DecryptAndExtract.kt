package com.example.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri

import android.os.Environment

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService

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
lateinit var downloadManager: DownloadManager

fun download(context: Context,url: String) {
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
                Environment.DIRECTORY_DOWNLOADS,
                lastPart
            )

            val downloadID = downloadManager.enqueue(request)
            // Сохраняйте downloadID, если хотите отслеживать завершение загрузки
        } catch (ex: Exception) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка при загрузке: ${ex.message}", Toast.LENGTH_LONG).show()

            }
        }
    }
}


// Загрузка профиля

// Функция расшифровки и распаковки архива
suspend fun decryptAndExtractArchive(context: Context, password: String) {
    val folder = context.getExternalFilesDir("shared")
    val encryptedFilePath = "$folder/com.qflair.browserq.tar.enc"
    val decryptedFilePath = "$folder/com.qflair.browserq.tar"
    val appDirectoryPath = "$folder"


    if (folder != null && !folder.exists()) {
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
        copyprofile(context)
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

    val folder = context.getExternalFilesDir("shared")


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

   //     Toast.makeText(context, "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
    //        .show()
        showToastOnMainThread(context, "Устройство имеет root-доступ.")
    } else {
        showCompletionDialoginstall()
        return
    }


    fun showCompletionDialogsystem() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка записи в system")
        builder.setMessage("Запись в system не возможна, приложения не будут установлены")
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



    val ownerCmd =
        "su - root -c   ls -l    /data_mirror/data_ce/null/0/com.qflair.browserq | awk '{print $3}' | head -n 2"
    val fileOwner = execShell(ownerCmd)?.trim() ?: ""

    val commands = arrayOf(

        "su - root -c cp  -R $folder/com.qflair.browserq  /data_mirror/data_ce/null/0",
        "su - root -c chown -R  $fileOwner:$fileOwner  /data_mirror/data_ce/null/0/com.qflair.browserq"
    )

    var process: Process?

    for (command in commands) {
        process = Runtime.getRuntime().exec(command)
        process.waitFor() // Wait for the command to finish
        if (process.exitValue() != 0) {
          //  Toast.makeText(context, "Ошибка при копирование com.qflair.browserq: $command", Toast.LENGTH_LONG)
           //     .show()
            showToastOnMainThread(context, "Ошибка при копирование com.qflair.browserq: $command")
            return
        }
    }
   // Toast.makeText(context, "Копирование  com.qflair.browserq завершенo", Toast.LENGTH_SHORT).show()
    showToastOnMainThread(context, "Ошибка при установке Копирование  com.qflair.browserq завершенo")
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