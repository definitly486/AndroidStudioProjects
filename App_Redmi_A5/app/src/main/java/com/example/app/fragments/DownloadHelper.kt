import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.app.fragments.RootChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadHelper(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var lastDownloadId: Long = -1L
    private var downloadReceiver: BroadcastReceiver? = null

    fun getDownloadFolder(): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    fun downloadTool(url: String, toolName: String, onDownloadComplete: (File?) -> Unit) {
        val folder = getDownloadFolder() ?: run {
            Toast.makeText(context, "Невозможно получить папку загрузки", Toast.LENGTH_SHORT).show()
            return
        }
        if (!folder.exists()) folder.mkdirs()

        val lastPart = url.substringAfterLast("/")
        val toolFile = File(folder, lastPart)

        if (toolFile.exists()) {
            Toast.makeText(context, "$toolName уже существует", Toast.LENGTH_SHORT).show()
            onDownloadComplete(toolFile)
            installTool(toolName)
            return
        }

        if (downloadReceiver == null) {
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                    if (id != lastDownloadId) return

                    context?.unregisterReceiver(this)
                    downloadReceiver = null

                    val query = DownloadManager.Query().setFilterById(id)
                    val cursor = downloadManager.query(query)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val statusColumnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusColumnIndex != -1) {
                                val status = it.getInt(statusColumnIndex)
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    val localUriColumnIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    if (localUriColumnIndex != -1) {
                                        val uriString = it.getString(localUriColumnIndex)
                                        val fileUri = Uri.parse(uriString)
                                        val downloadedFile = File(fileUri.path ?: "")
                                        onDownloadComplete(downloadedFile)
                                        installTool(toolName)
                                    } else {
                                        Toast.makeText(context, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show()
                                        onDownloadComplete(null)
                                    }
                                } else {
                                    Toast.makeText(context, "Загрузка не удалась", Toast.LENGTH_SHORT).show()
                                    onDownloadComplete(null)
                                }
                            } else {
                                Toast.makeText(context, "Не удалось получить статус загрузки", Toast.LENGTH_SHORT).show()
                                onDownloadComplete(null)
                            }
                        }
                    }
                }
            }
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(lastPart)
                setDescription("$toolName Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    lastPart
                )
            }
            lastDownloadId = downloadManager.enqueue(request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Ошибка при скачивании: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun installTool(toolName: String) {
        fun showCompletionDialog() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Проверка root")
            builder.setMessage("Root доступ отсутствует, приложения не будут установлены")
            builder.setPositiveButton("Продолжить") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        if (!RootChecker.hasRootAccess(context)) {
            showCompletionDialog()
            return
        }


        fun showCompletionDialog_system() {
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
            showCompletionDialog_system()
            return
        }



        Toast.makeText(context, "Начинается установка $toolName...", Toast.LENGTH_SHORT).show()

        val commands = when (toolName) {
            "busybox" -> arrayOf(
                "su - root -c mount -o rw,remount /system",
                "su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/busybox /system/bin/",
                "su - root -c chmod +x  /system/bin/busybox",
                "su - root -c chmod 0755  /system/bin/busybox",
                "su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/curl /system/bin/",
                "su - root -c chmod +x  /system/bin/curl",
                "su - root -c chmod 0755  /system/bin/curl"
            )
            "openssl" -> arrayOf(
                "su - root -c mount -o rw,remount /system",
                "su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/openssl /system/bin/",
                "su - root -c chmod +x  /system/bin/openssl",
                "su - root -c chmod 0755  /system/bin/openssl"
            )
            "gh" -> arrayOf(
                "su - root -c mount -o rw,remount /system",
                "su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/gh /system/bin/",
                "su - root -c chmod +x  /system/bin/gh",
                "su - root -c chmod 0755  /system/bin/gh"
            )
            else -> emptyArray()
        }

        var process: Process? = null

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Ждем завершения команды
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при установке $toolName: $command", Toast.LENGTH_LONG).show()
                return
            }
        }

        Toast.makeText(context, "Установка $toolName завершена", Toast.LENGTH_SHORT).show()
    }

    fun installApk(filename: String) {
        val folder = getDownloadFolder() ?: return
        val apkFile = File(folder, filename)
        if (apkFile.exists()) {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canInstall = context.packageManager.canRequestPackageInstalls()
                if (!canInstall) {
                    val installIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(installIntent)
                    Toast.makeText(context, "Разрешите установку из неизвестных источников", Toast.LENGTH_LONG).show()
                    return
                }
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadgpg(url: String) {
        val folder = getDownloadFolder() ?: return
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

    fun download(url: String, onDownloadComplete: (File?) -> Unit) {
        val folder = getDownloadFolder() ?: run {
            Toast.makeText(context, "Невозможно получить папку загрузки", Toast.LENGTH_SHORT).show()
            return
        }
        if (!folder.exists()) folder.mkdirs()

        val lastPart = url.substringAfterLast("/")
        val apkFile = File(folder, lastPart)
        val extension = lastPart.substringAfterLast('.')
        if (extension.lowercase() != "apk") {
            Toast.makeText(context, "Это не файл формата APK", Toast.LENGTH_SHORT).show()
            onDownloadComplete(null)
            return
        }

        if (apkFile.exists()) {
            Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
            onDownloadComplete(apkFile)
            installApk(lastPart)
            return
        }

        if (downloadReceiver == null) {
            downloadReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                    if (id != lastDownloadId) return

                    context?.unregisterReceiver(this)
                    downloadReceiver = null

                    val query = DownloadManager.Query().setFilterById(id)
                    val cursor = downloadManager.query(query)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val statusColumnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusColumnIndex != -1) {
                                val status = it.getInt(statusColumnIndex)
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    val localUriColumnIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    if (localUriColumnIndex != -1) {
                                        val uriString = it.getString(localUriColumnIndex)
                                        val fileUri = Uri.parse(uriString)
                                        val downloadedFile = File(fileUri.path ?: "")
                                        onDownloadComplete(downloadedFile)
                                        installApk(lastPart)
                                    } else {
                                        Toast.makeText(context, "Не удалось получить путь к файлу", Toast.LENGTH_SHORT).show()
                                        onDownloadComplete(null)
                                    }
                                } else {
                                    Toast.makeText(context, "Загрузка не удалась", Toast.LENGTH_SHORT).show()
                                    onDownloadComplete(null)
                                }
                            } else {
                                Toast.makeText(context, "Не удалось получить статус загрузки", Toast.LENGTH_SHORT).show()
                                onDownloadComplete(null)
                            }
                        }
                    }
                }
            }

            // 🔥 РЕГИСТРАЦИЯ с флагом RECEIVER_NOT_EXPORTED
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(lastPart)
                setDescription("Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    lastPart
                )
            }
            lastDownloadId = downloadManager.enqueue(request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Ошибка при скачивании: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun cleanup() {
        try {
            if (downloadReceiver != null) {
                context.unregisterReceiver(downloadReceiver)
                downloadReceiver = null
            }
        } catch (e: IllegalArgumentException) {
            // Игнорируем исключение, если приёмник уже удалён
        }
    }
}