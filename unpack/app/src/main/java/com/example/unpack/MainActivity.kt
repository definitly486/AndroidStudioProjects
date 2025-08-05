package com.example.unpack


import android.app.DownloadManager

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import androidx.core.net.toUri
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream



import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf()
    } else {
        arrayListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)



    }


    fun decompressTarGz(tarGzFile: File, outputDir: File) {

        // Ensure canonical path for security
        val canonicalOutputDir = outputDir.canonicalFile

        if (!tarGzFile.exists()) throw FileNotFoundException("File not found: ${tarGzFile.path}") as Throwable
        GzipCompressorInputStream(BufferedInputStream(FileInputStream(tarGzFile))).use { gzIn ->
            TarArchiveInputStream(gzIn).use { tarIn ->
                generateSequence { tarIn.nextEntry }.forEach { entry ->

                    val outputFile = File(outputDir, entry.name).canonicalFile

                    // Check if the extracted file stays inside outputDir
                    // Prevent Zip Slip Vulnerability
                   // if (!outputFile.toPath().startsWith(canonicalOutputDir.toPath())) {
                   //     throw SecurityException("Zip Slip vulnerability detected! Malicious entry: ${entry.name}")
                  //  }

                    if (entry.isDirectory) outputFile.mkdirs()
                    else {
                        outputFile.parentFile.mkdirs()
                        outputFile.outputStream().use { outStream ->
                            tarIn.copyTo(outStream)
                        }
                    }
                }
            }
        }
    }





    fun unpackTarXz(tarXzFile: File, outputDirectory: File) {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        FileInputStream(tarXzFile).use { fis ->
            BufferedInputStream(fis).use { bis ->
                XZCompressorInputStream(bis).use { xzIn ->
                    TarArchiveInputStream(xzIn).use { tarIn ->
                        var entry = tarIn.nextEntry
                        while (entry != null) {
                            val outputFile = File(outputDirectory, entry.name)
                            if (entry.isDirectory) {
                                outputFile.mkdirs()
                            } else {
                                FileOutputStream(outputFile).use { fos ->
                                    tarIn.copyTo(fos)
                                }
                            }
                            entry = tarIn.nextEntry
                        }
                    }
                }
            }
        }

    }


    fun unpackfile(view: View) {
        val tarXzFile = File("/storage/emulated/0/Download/main.tar.gz")
        val outputDir = File("/storage/emulated/0/Download/")
        unpackTarXz(tarXzFile, outputDir)

    }

    fun downloadfile(url: String) {


        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/"

        )
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val lastname = url.split("/").last()

        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/" + lastname

        )
        if (file.exists()) {
            Toast.makeText(this, "file  exist", Toast.LENGTH_SHORT).show()
            return
        }


        Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show()
        val fileName = url.split("/").last()
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(url.toUri())
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        request.setTitle(fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(
           Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
        downloadManager.enqueue(request)

    }

    fun downloadcomtermux(view: View) {
        downloadfile("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/archive/main.tar.gz")

    }
    fun unpackmaintargz(view: View) {

        decompressTarGz(File("/storage/emulated/0/Download/main.tar.gz"), File("/storage/emulated/0/Download/"))
    }
}