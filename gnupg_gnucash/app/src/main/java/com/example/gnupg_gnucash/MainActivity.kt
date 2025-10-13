package com.example.gnupg_gnucash

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001
val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var downloadManager: DownloadManager
    var mydownloadID: Long = 0
    var apkHttpUrl = "https://github.com/xinitronix/gnucash/raw/refs/heads/main/"

    companion object {
        const val WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkWritePermission()
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    /**
     * Проверка и запрос разрешения на запись и чтение внешних данных
     */
    private fun checkWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Новое хранилище данных для Android 11 и выше
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                WRITE_EXTERNAL_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults.all { it == PackageManager.PERMISSION_GRANTED })
                ) {
                    Log.d(TAG, "Разрешения на запись и чтение предоставлено!")
                } else {
                    Log.e(TAG, "Не хватает разрешения на запись и чтение.")
                }
            }
        }
    }

    /**
     * Метод для начала загрузки файла
     */
    fun download(url: String) {
        val folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val lastPart = url.split("/").last()
        val file = File(folder, lastPart)

        if (file.exists()) {
            Toast.makeText(this, "Файл уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Начинается загрузка...", Toast.LENGTH_SHORT)
                        .show()
                }

                val fileName = url.substringAfterLast('/')
                val request = DownloadManager.Request(Uri.parse(url))
                request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )
                request.setTitle(fileName)
                request.setDescription("Загружается...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.allowScanningByMediaScanner()
                request.setDestinationInExternalFilesDir(
                    this@MainActivity,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

                mydownloadID = downloadManager.enqueue(request)
            } catch (ex: Exception) {
                ex.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка при загрузке: ${ex.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Приемник уведомлений о завершении загрузки
    private val downloadCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == mydownloadID) {
                Toast.makeText(applicationContext, "Загрузка завершена", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        registerReceiver(
            downloadCompleteBroadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onPause() {
        unregisterReceiver(downloadCompleteBroadcastReceiver)
        super.onPause()
    }

    fun downloadgnucash(view: View) {
        download("${apkHttpUrl}definitly.gnucash.gpg")
    }


}