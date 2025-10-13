package com.example.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R

import android.app.DownloadManager

import android.content.Context
import android.content.Intent

import android.net.Uri

import android.os.Environment

import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FirstFragment : Fragment() {


    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }
    var apkHttpUrl = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/busybox/"
    var apkHttpUrl2 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/openssl/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        val button = view.findViewById<Button>(R.id.firstButton)
        button.setOnClickListener {

            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}busybox")
            helper.download("${apkHttpUrl}busybox.sh")
        }
        val installbutton = view.findViewById<Button>(R.id.installButton)
        installbutton.setOnClickListener {

            val folder = getDownloadFolder() ?: return@setOnClickListener
            val file = File(folder, "busybox")

            if (!file.exists()) {
                Toast.makeText(requireContext(), "Файл  busybox не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val helper = DownloadHelper(requireContext())
            helper.installbusybox()
        }
        val  downloadopenssl = view.findViewById<Button>(R.id.downloadopenssl)
        downloadopenssl.setOnClickListener {

            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl2}openssl")
        }
        val  installopenssl = view.findViewById<Button>(R.id.installopenssl)
        installopenssl.setOnClickListener {

            val folder = getDownloadFolder() ?: return@setOnClickListener
            val file = File(folder, "openssl")

            if (!file.exists()) {
                Toast.makeText(requireContext(), "Файл  openssl не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val helper = DownloadHelper(requireContext())
            helper.installopenssl()
        }

        return view
    }
}


    class DownloadHelper(private val context: Context) {

        private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        fun getDownloadFolder(): File? {
            return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        }

        fun installbusybox(){

            Toast.makeText(context, "Начинается установка busybox...", Toast.LENGTH_SHORT).show()
            Runtime.getRuntime().exec("su - root -c mount -o rw,remount /")
            Runtime.getRuntime().exec("su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/busybox /system/bin/busybox")
            Runtime.getRuntime().exec("su - root -c chmod +x  /system/bin/busybox")
            Runtime.getRuntime().exec("su - root -c chmod 0755  /system/bin/busybox")
          
        }

        fun installopenssl(){

            Toast.makeText(context, "Начинается установка openssl...", Toast.LENGTH_SHORT).show()
            Runtime.getRuntime().exec("su - root -c mount -o rw,remount /")
            Runtime.getRuntime().exec("su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/openssl /system/bin/")
            Runtime.getRuntime().exec("su - root -c chmod +x  /system/bin/openssl")
            Runtime.getRuntime().exec("su - root -c chmod 0755  /system/bin/openssl")

        }



        fun download(url: String) {
            val folder = getDownloadFolder() ?: return
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val lastPart = url.split("/").last()
            val file = File(folder, lastPart)

            if (file.exists()) {
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
                    request.allowScanningByMediaScanner()
                    request.setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_DOWNLOADS,
                        lastPart
                    )

                    val downloadID = downloadManager.enqueue(request)
                    // Save downloadID if needed to track completion
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Ошибка при загрузке: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }
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
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
            }
        }

        fun unzip(filename: String): Boolean {
            val folder = getDownloadFolder() ?: return false
            val zipFile = File(folder, filename)
            val outputFolder = File(folder, "")
            outputFolder.mkdirs()

            // Check if target APK already exists
            val targetApk = File(outputFolder, filename)
            if (targetApk.exists()) {
                Toast.makeText(context, "Файл уже существует", Toast.LENGTH_SHORT).show()
                installApk(filename)
                return true
            }

            try {
                FileInputStream(zipFile).use { fis ->
                    ZipInputStream(fis).use { zis ->
                        var entry: ZipEntry?
                        while (zis.nextEntry.also { entry = it } != null) {
                            val destFile = File(outputFolder, entry!!.name)
                            destFile.parentFile?.mkdirs()
                            if (!entry!!.isDirectory) {
                                FileOutputStream(destFile).use { fos ->
                                    val buffer = ByteArray(4096)
                                    var count: Int
                                    while (zis.read(buffer).also { count = it } != -1) {
                                        fos.write(buffer, 0, count)
                                    }
                                }
                            }
                            zis.closeEntry()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }
    }





