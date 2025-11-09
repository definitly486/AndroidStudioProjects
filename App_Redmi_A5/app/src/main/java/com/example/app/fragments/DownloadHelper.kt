import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
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

    // Папка приложения: /Android/data/com.example.app/files/Download/
    fun getDownloadFolder(): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (dir?.exists() == false) dir.mkdirs()
        return dir
    }

    // Папка приложения APK: /Android/data/com.example.app/files/APK/
    fun getDownloadFolderapk(): File? {
        return context.getExternalFilesDir("APK")
    }

    fun getDownloadFolder2(): File? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (dir?.exists() == false) dir.mkdirs()
        return dir
    }

    // === ЗАГРУЗКА APK ===
    fun download(url: String, onDownloadComplete: (File?) -> Unit) {
        val folder = getDownloadFolder() ?: run {
            Toast.makeText(context, "Невозможно получить папку загрузки", Toast.LENGTH_SHORT).show()
            return
        }

        val lastPart = url.substringAfterLast("/").substringBefore("?").substringBefore("#")
        val extension = lastPart.substringAfterLast('.', "").lowercase()
        if (extension != "apk") {
            Toast.makeText(context, "Это не файл формата APK", Toast.LENGTH_SHORT).show()
            onDownloadComplete(null)
            return
        }

        val apkFile = File(folder, lastPart)
        if (apkFile.exists()) {
            Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
            onDownloadComplete(apkFile)
            installApk(lastPart)
            return
        }

        if (downloadReceiver == null) {
            downloadReceiver = createDownloadReceiver(lastPart, onDownloadComplete)
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED  // ОТКРЫТ ДЛЯ DownloadManager
            )
            Log.d("DOWNLOAD", "Receiver зарегистрирован (EXPORTED)")
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(lastPart)
                setDescription("Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, lastPart)
            }
            lastDownloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "Начало загрузки: $lastPart", Toast.LENGTH_SHORT).show()
            Log.d("DOWNLOAD", "Started: $lastPart, ID: $lastDownloadId")
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Ошибка: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    // === ЗАГРУЗКА ИНСТРУМЕНТОВ (busybox, openssl, gh) ===
    fun downloadTool(url: String, toolName: String, onDownloadComplete: (File?) -> Unit) {
        val folder = getDownloadFolder() ?: run {
            Toast.makeText(context, "Невозможно получить папку загрузки", Toast.LENGTH_SHORT).show()
            return
        }

        val lastPart = url.substringAfterLast("/").substringBefore("?").substringBefore("#")
        val toolFile = File(folder, lastPart)

        if (toolFile.exists()) {
            Toast.makeText(context, "$toolName уже существует", Toast.LENGTH_SHORT).show()
            onDownloadComplete(toolFile)
            installTool(toolName)
            return
        }

        if (downloadReceiver == null) {
            downloadReceiver = createToolDownloadReceiver(lastPart, toolName, onDownloadComplete)
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(lastPart)
                setDescription("$toolName Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, lastPart)
            }
            lastDownloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "Загрузка $toolName...", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Ошибка: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    // === RECEIVER ДЛЯ APK ===
    private fun createDownloadReceiver(fileName: String, onComplete: (File?) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id != lastDownloadId) return

                Log.d("DOWNLOAD", "onReceive: ID=$id")

                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                var downloadedFile: File? = null
                var success = false

                cursor?.use {
                    if (it.moveToFirst()) {
                        val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriString = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                            val fileUri = Uri.parse(uriString)
                            downloadedFile = File(fileUri.path ?: "")
                            success = downloadedFile?.exists() == true && downloadedFile?.name == fileName
                            Log.d("DOWNLOAD", "Файл: ${downloadedFile?.absolutePath}, exists: ${downloadedFile?.exists()}")
                        }
                    }
                }

                // ОТПИСКА ПОСЛЕ ОБРАБОТКИ
                try { ctx?.unregisterReceiver(this) } catch (e: Exception) { e.printStackTrace() }
                downloadReceiver = null
                lastDownloadId = -1

                if (success && downloadedFile != null) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Загрузка завершена: $fileName", Toast.LENGTH_LONG).show()
                        installApk(fileName)
                    }
                    onComplete(downloadedFile)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                    }
                    onComplete(null)
                }
            }
        }
    }

    // === RECEIVER ДЛЯ ИНСТРУМЕНТОВ ===
    private fun createToolDownloadReceiver(fileName: String, toolName: String, onComplete: (File?) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id != lastDownloadId) return

                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                var downloadedFile: File? = null
                var success = false

                cursor?.use {
                    if (it.moveToFirst()) {
                        val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriString = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                            val fileUri = Uri.parse(uriString)
                            downloadedFile = File(fileUri.path ?: "")
                            success = downloadedFile?.exists() == true && downloadedFile?.name == fileName
                        }
                    }
                }

                try { ctx?.unregisterReceiver(this) } catch (e: Exception) {}
                downloadReceiver = null
                lastDownloadId = -1

                if (success && downloadedFile != null) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Загрузка $toolName завершена", Toast.LENGTH_LONG).show()
                        installTool(toolName)
                    }
                    onComplete(downloadedFile)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Ошибка загрузки $toolName", Toast.LENGTH_SHORT).show()
                    }
                    onComplete(null)
                }
            }
        }
    }


    // === RECEIVER ДЛЯ APK в папке  /Android/data/com.example.app/files/APK/ ===
    private fun createAPKDownloadReceiver(fileName: String, onComplete: (File?) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id != lastDownloadId) return

                Log.d("DOWNLOAD", "onReceive: ID=$id")

                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                var downloadedFile: File? = null
                var success = false

                cursor?.use {
                    if (it.moveToFirst()) {
                        val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriString = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                            val fileUri = Uri.parse(uriString)
                            downloadedFile = File(fileUri.path ?: "")
                            success = downloadedFile?.exists() == true && downloadedFile?.name == fileName
                            Log.d("DOWNLOAD", "Файл: ${downloadedFile?.absolutePath}, exists: ${downloadedFile?.exists()}")
                        }
                    }
                }

                // ОТПИСКА ПОСЛЕ ОБРАБОТКИ
                try { ctx?.unregisterReceiver(this) } catch (e: Exception) { e.printStackTrace() }
                downloadReceiver = null
                lastDownloadId = -1

                if (success && downloadedFile != null) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Загрузка завершена: $fileName", Toast.LENGTH_LONG).show()
                        installApk2(fileName)
                    }
                    onComplete(downloadedFile)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(ctx, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                    }
                    onComplete(null)
                }
            }
        }
    }


    // === УСТАНОВКА APK ===
    fun installApk(filename: String) {
        val folder = getDownloadFolder() ?: return
        val apkFile = File(folder, filename)

        if (!apkFile.exists()) {
            Toast.makeText(context, "Файл не найден: $filename", Toast.LENGTH_SHORT).show()
            return
        }

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

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть установщик", Toast.LENGTH_SHORT).show()
        }
    }


    // === УСТАНОВКА APK находящихся в папке /Android/data/com.example.app/files/APK/ ===
    fun installApk2(filename: String) {
        val folder = getDownloadFolderapk() ?: return
        val apkFile = File(folder, filename)

        if (!apkFile.exists()) {
            Toast.makeText(context, "Файл не найден: $filename", Toast.LENGTH_SHORT).show()
            return
        }

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

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть установщик", Toast.LENGTH_SHORT).show()
        }
    }



    // === УСТАНОВКА ИНСТРУМЕНТОВ (через root) ===
    fun installTool(toolName: String) {
        if (!RootChecker.hasRootAccess(context)) {
            AlertDialog.Builder(context)
                .setTitle("Root")
                .setMessage("Root-доступ отсутствует")
                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                .show()
            return
        }

        if (!RootChecker.checkWriteAccess("/system")) {
            AlertDialog.Builder(context)
                .setTitle("Запись")
                .setMessage("Нет прав на запись в /system")
                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                .show()
            return
        }

        Toast.makeText(context, "Установка $toolName...", Toast.LENGTH_SHORT).show()

        val basePath = getDownloadFolder()?.absolutePath ?: return
        val commands = when (toolName) {
            "busybox" -> arrayOf(
                "su -c mount -o rw,remount /system",
                "su -c cp $basePath/busybox /system/bin/",
                "su -c chmod 0755 /system/bin/busybox",
                "su -c cp $basePath/curl /system/bin/",
                "su -c chmod 0755 /system/bin/curl",
                "su -c cp $basePath/openssl /system/bin/",
                "su -c chmod 0755 /system/bin/openssl"
            )
            "openssl" -> arrayOf(
                "su -c mount -o rw,remount /system",
                "su -c cp $basePath/openssl /system/bin/",
                "su -c chmod 0755 /system/bin/openssl"
            )
            "gh" -> arrayOf(
                "su -c mount -o rw,remount /system",
                "su -c cp $basePath/gh /system/bin/",
                "su -c chmod 0755 /system/bin/gh"
            )
            else -> emptyArray()
        }

        var process: Process?
        for (cmd in commands) {
            process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка: $cmd", Toast.LENGTH_LONG).show()
                return
            }
        }

        Toast.makeText(context, "Установка $toolName завершена", Toast.LENGTH_SHORT).show()
    }

    // === ОЧИСТКА ===
    fun cleanup() {
        try {
            downloadReceiver?.let {
                context.unregisterReceiver(it)
                downloadReceiver = null
            }
        } catch (e: Exception) { /* ignore */ }
    }

    // === ПРОЧИЕ МЕТОДЫ ===

    fun download2(url: String) {
        val folder = getDownloadFolder2() ?: return
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
                request.setDestinationInExternalPublicDir(
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


    fun downloadapk(url: String,onDownloadComplete: (File?) -> Unit) {
        val folder = getDownloadFolderapk() ?: return
        if (!folder.exists()) folder.mkdirs()

        val lastPart = url.split("/").last()
        val gpgFile = File(folder, lastPart)

        if (gpgFile.exists()) {
            Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
            return
        }


        if (downloadReceiver == null) {
            downloadReceiver = createAPKDownloadReceiver(lastPart, onDownloadComplete)
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED  // ОТКРЫТ ДЛЯ DownloadManager
            )
            Log.d("DOWNLOAD", "Receiver зарегистрирован (EXPORTED)")
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle(lastPart)
                setDescription("Загружается...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
            //    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, lastPart)
                setDestinationInExternalFilesDir(
                    context,
                    "APK",  // Папка "APK"
                    lastPart
                )
            }
            lastDownloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "Начало загрузки: $lastPart", Toast.LENGTH_SHORT).show()
            Log.d("DOWNLOAD", "Started: $lastPart, ID: $lastDownloadId")
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "Ошибка: ${ex.message}", Toast.LENGTH_LONG).show()
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


}