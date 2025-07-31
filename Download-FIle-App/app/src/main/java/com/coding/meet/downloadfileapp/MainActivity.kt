package com.coding.meet.downloadfileapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import java.io.File


class  MainActivity : AppCompatActivity() {

    var mydownloaid : Long = 0

    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf()
    } else {
        arrayListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
    private lateinit var snackbar: Snackbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<View>(R.id.rootView)
        snackbar = Snackbar.make(
            rootView,
            "No Internet Connection",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Setting") {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }


    }




    fun downloadtermux(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/termux-app_v0.119.0-beta.3+apt-android-5-github-debug_arm64-v8a.apk")

    }

    fun downloadmain(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/archive/main.tar.gz")

    }

    fun downloadnote(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://raw.githubusercontent.com/definitly486/Lenovo_Tab_3_7_TB3-730X/refs/heads/main/note")

    }


    fun installtermux(@Suppress("UNUSED_PARAMETER")view: View) {

        install("termux-app_v0.119.0-beta.3+apt-android-5-github-debug_arm64-v8a.apk")

    }


    fun installtc(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Total_Commander_v.3.60b4d.apk")
        install("Total_Commander_v.3.60b4d.apk")
    }

    fun installnekobox(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/NekoBox-1.3.8-arm64-v8a.apk")
        install("NekoBox-1.3.8-arm64-v8a.apk")
    }

    fun installfirefox(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Firefox+139.0.4.apk")
        install("Firefox+139.0.4.apk")
    }



    fun installchrome(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Google+Chrome+106.0.5249.126+Android6.arm.apk")
        install("Google+Chrome+106.0.5249.126+Android6.arm.apk")
    }

    fun installgnucash(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/GnucashAndroid_v2.4.0.apk")
        install("GnucashAndroid_v2.4.0.apk")
    }

    fun installmpv(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/is.xyz.mpv_41.apk")
        install("is.xyz.mpv_41.apk")
    }

    fun installaimp(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/AIMP_vv3.30.1250.apk")
        install("AIMP_vv3.30.1250.apk")
    }

    fun installpluma(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk")
        install("Pluma_.private_fast.browser_1.80_APKPure.apk")
    }

    fun installbyedpi(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/byedpi-1.2.0.apk")
        install("byedpi-1.2.0.apk")
    }

    fun installlibretorrent(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/LibreTorrent-3.2-armeabi-v7a.apk")
        install("LibreTorrent-3.2-armeabi-v7a.apk")
    }

    fun installgesture(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Gesture.apk")
        install("Gesture.apk")
    }
    fun installhibernator(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Hibernator.apk")
        install("Hibernator.apk")
    }

    fun installv2rayng(@Suppress("UNUSED_PARAMETER")view: View) {

        download("https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/v2rayNG.apk")
        install("v2rayNG.apk")
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun install(url: String) {

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            Uri.fromFile(File(Environment.getExternalStorageDirectory().toString() + "/Download/"+url)),
                            "application/vnd.android.package-archive"
                        )
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // without this flag android returned a intent error!
                        startActivity(intent)


    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun download(url: String) {


        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/"

        )
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val lastname = url.split("/").last()

        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/"+lastname

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
        mydownloaid = downloadManager.enqueue(request)

    }



    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully

                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }
}