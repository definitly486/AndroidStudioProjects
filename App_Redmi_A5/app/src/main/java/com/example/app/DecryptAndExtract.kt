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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ProcessBuilder
import java.lang.RuntimeException

const val DOWNLOAD_COMPLETE_ACTION = "android.intent.action.DOWNLOAD_COMPLETE"

// Основной класс для приема широковещательного события
class DownloadCompleteReceiver(
    private val downloadID: Long,
    private val onDownloadComplete: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val receivedDownloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (receivedDownloadID == downloadID) {
            // Unregister using the context provided by the system
            try {
                context.unregisterReceiver(this)
            } catch (e: IllegalArgumentException) {
                // Already unregistered or invalid — safe to ignore
                Log.w("DownloadReceiver", "Receiver already unregistered", e)
            }

            // Do work off main thread
            CoroutineScope(Dispatchers.IO).launch {
            }
        }
    }
}

// Вспомогательные функции

// Получение директории загрузки
fun getDownloadFolder(context: Context): File? {
    return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
}

// Загрузка профиля
fun downloadPlumaProfile(context: Context, url: String) {
    val folder = getDownloadFolder(context) ?: return
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
            showToastOnMainThread(context, "Идет загрузка")

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

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadID = downloadManager.enqueue(request)

            // Регистрация приемника
            val receiver = DownloadCompleteReceiver(downloadID) {
                // Optional callback if needed
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DOWNLOAD_COMPLETE_ACTION),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            withContext(Dispatchers.Main) {
                showToastOnMainThread(context, "Ошибка при загрузке: ${ex.message}")
            }
        }
    }
}

// Функция расшифровки и распаковки архива
suspend fun decryptAndExtractArchive(context: Context, password: String) {
    val encryptedFilePath = "/storage/emulated/0/Android/data/files/com.qflair.browserq.tar.enc"
    val decryptedFilePath = "/storage/emulated/0/Android/data/files/com.qflair.browserq.tar"
    val appDirectoryPath = "/storage/emulated/0/Android/data/files/browserq_data"

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

// Вспомогательная функция для показа Toast на главном потоке
fun showToastOnMainThread(context: Context, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}