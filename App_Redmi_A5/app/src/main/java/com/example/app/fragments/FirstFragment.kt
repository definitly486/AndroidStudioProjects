package com.example.app.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app.R
import android.app.DownloadManager
import java.io.File

class FirstFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        downloadHelper = DownloadHelper(requireContext())


        val installButton = view.findViewById<Button>(R.id.installButton)

        // Кнопка загрузки файла
        installButton.setOnClickListener {
            // Замените URL на актуальный для загрузки
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Termos_v2.4_universal.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val installfm = view.findViewById<Button>(R.id.installfm)
        installfm.setOnClickListener {
            // Замените URL на актуальный для загрузки
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/FM+v3.6.3.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
            downloadHelper.installApk("FM+v3.6.3.apk")
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadHelper.cleanup()
    }

    // Внутренний класс DownloadHelper
    class DownloadHelper(private val context: Context) {

        private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        private var lastDownloadId: Long = -1L
        private var downloadReceiver: BroadcastReceiver? = null

        fun getDownloadFolder(): File? {
            return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        }
        fun download(url: String, onDownloadComplete: (File?) -> Unit) {
            val folder = getDownloadFolder() ?: run {
                Toast.makeText(context, "Невозможно получить папку загрузки", Toast.LENGTH_SHORT).show()
                return
            }
            if (!folder.exists()) folder.mkdirs()

            val lastPart = url.substringAfterLast("/")
            val apkFile = File(folder, lastPart)

            if (apkFile.exists()) {
                Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
                onDownloadComplete(apkFile)
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

                // Проверка разрешения на установку (для Android 8+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val canInstall = context.packageManager.canRequestPackageInstalls()
                    if (!canInstall) {
                        val installIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(installIntent)
                        Toast.makeText(context, "Пожалуйста, разрешите установку из неизвестных источников", Toast.LENGTH_LONG).show()
                        return
                    }
                }

                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
            }
        }

        fun cleanup() {
            // Отписка от ресивера
            try {
                if (downloadReceiver != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.unregisterReceiver(downloadReceiver)
                    } else {
                        context.unregisterReceiver(downloadReceiver)
                    }
                    downloadReceiver = null
                }
            } catch (e: IllegalArgumentException) {
                // Уже был отменен или не зарегистрирован
            }
        }
    }
}