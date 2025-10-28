package com.example.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.File
import java.lang.ProcessBuilder
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Paths

// Функция для загрузки профиля
fun downloadplumaprofile(context: Context, url: String) {
    val folder = context.getExternalFilesDir(null) ?: return
    if (!folder.exists()) folder.mkdirs()

    val lastPart = url.split("/").last()
    val gpgFile = File(folder, lastPart)

    if (gpgFile.exists()) {
        showToastOnMainThread(context, "Файл уже существует")
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            withContext(Dispatchers.Main) {
                showToastOnMainThread(context, "Начинается загрузка...")
            }

            // Логика загрузки файла
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

            val downloadID =
                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(
                    request
                )
        } catch (ex: Exception) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                showToastOnMainThread(context, "Ошибка при загрузке: ${ex.message}")
            }
        }
    }
}

// Основная функция расшифровки и установки
fun decryptAndExtractArchive(context: Context, password: String) {
    val encryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/com.qflair.browserq.tar.enc"
    val decryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/com.qflair.browserq.tar"
    val appDirectoryPath = "/storage/emulated/0/Android/data/com.example.app/files/browserq_data"

    // Загружаем файл, если его нет
    if (!File(encryptedFilePath).exists()) {
        downloadplumaprofile(
            context,
            "https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc"
        )
    }

    // Реализуем процесс расшифровки и распаковки здесь
    CoroutineScope(Dispatchers.IO).launch {
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
}

// Вспомогательная функция для вывода Toast в Main Thread
fun showToastOnMainThread(context: Context, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}