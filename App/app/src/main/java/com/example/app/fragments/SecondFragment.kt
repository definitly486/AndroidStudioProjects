package com.example.app.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.app.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SecondFragment : Fragment() {

    var apkHttpUrl = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/"
    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        val button = view.findViewById<Button>(R.id.button5)
        button.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}app_mpv-default-arm64-v8a-release.apk")
            helper.download("${apkHttpUrl}ByeByeDPI-arm64-v8a-release.apk")
            helper.download("${apkHttpUrl}File+Manager++-Premium-v3.5.4_2103054-Mod.apk")
            helper.download("${apkHttpUrl}vcore.apk")
            helper.download("${apkHttpUrl}com.aurora.store_70.apk")
            helper.download("${apkHttpUrl}NewPipe_nightly-1066.apk")
            helper.download("${apkHttpUrl}org-gnucash-android-24003-39426726-deeea690953a751a05a1a35017540c33.apk")
            helper.download("${apkHttpUrl}com.android.chrome_141.0.7390.43-739004331_1lang_2feat_e182f1b6bd7ad7d24534108e4e98f9ed_apkmirror.com.apkm")
            helper.download("${apkHttpUrl}sports+2024_1.2_apkcombo.com_antisplit.apk")
            helper.download("${apkHttpUrl}Hacker_v1.41.1.apk")
            helper.download("${apkHttpUrl}terminal.apk")
            helper.download("${apkHttpUrl}Magisk-v25.2.apk")
            helper.download("${apkHttpUrl}magisk-tb8054-remount-v17.1.zip")
            helper.download("${apkHttpUrl}magisk-remount.zip")
            helper.download("${apkHttpUrl}org.sufficientlysecure.keychain_60200.apk")
            helper.download("${apkHttpUrl}Total_Commander_v.3.60b4d.apk")
            helper.download("${apkHttpUrl}Terminal+Shortcut+Pro+7.0+build+38+.4.0-11.0.+-paid.apk")
        }
        val button6 = view.findViewById<Button>(R.id.button6)
        button6.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.installApk("vcore.apk")
            helper.installApk("NewPipe_nightly-1066.apk")
            helper.installApk("File+Manager++-Premium-v3.5.4_2103054-Mod.apk")
            helper.installApk("app_mpv-default-arm64-v8a-release.apk")
            helper.installApk("ByeByeDPI-arm64-v8a-release.apk")
            helper.installApk("terminal.apk")
            helper.installApk("Hacker_v1.41.1.apk")
            helper.installApk("org.sufficientlysecure.keychain_60200.apk")
        }

        val button7 = view.findViewById<Button>(R.id.button7)
        button7.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}Binance_.BTC.Crypto.and.NFTS_3.4.3_APKPure.xapk")
        }


        val button8 = view.findViewById<Button>(R.id.button8)
        button8.setOnClickListener {
            val helper = DownloadHelper(requireContext())

            helper.installApk("Binance_.BTC.Crypto.and.NFTS_3.4.3_APKPure.xapk")
        }


        val button2 = view.findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}SberbankOnline.apk")
        }

        val button4 = view.findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}Ozon+Job_1.62.0-GMS-release_apkcombo.com_antisplit.apk")
        }

        val button15 = view.findViewById<Button>(R.id.button15)
        button15.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.installApk("Ozon+Job_1.62.0-GMS-release_apkcombo.com_antisplit.apk")
        }

        val button3 = view.findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}com.google.android.trichromelibrary_141.0.7390.43-739004331_minAPI29_apkmirror.com.apk")
        }

        val button14 = view.findViewById<Button>(R.id.button14)
        button14.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.installApk("com.google.android.trichromelibrary_141.0.7390.43-739004331_minAPI29_apkmirror.com.apk")
        }


        val button12 = view.findViewById<Button>(R.id.button12)
        button12.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}gate.base.zip")
        }

        val button13 = view.findViewById<Button>(R.id.button13)
        button13.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            val filePath = "/storage/emulated/0/Android/data/com.example.app/files/Download/gate.base.zip"
            val file = File(filePath)

            if (!file.exists()) {
                Toast.makeText(requireContext(), "Файл не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            unzip_gate("gate.base.zip")
            install_gate("gate.base.apk")
        }

        val button10 = view.findViewById<Button>(R.id.button10)
        button10.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.installApk("SberbankOnline.apk")
        }


        return view
    }
    fun install_gate(filename: String) {
        val folder = getDownloadFolder() ?: return
        val apkFile = File(folder, filename)
        if (apkFile.exists()) {
            val context = this.context ?: return  // Проверка на null
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

    fun unzip_gate(filename: String): Boolean {
        val folder = getDownloadFolder() ?: return false
        val zipFile = File(folder, filename)


        // Check if target APK already exists
        val targetApk = File(folder, "gate.base.apk")
        if (targetApk.exists()) {
            Toast.makeText(context, "Файл gate.base.apk  уже существует", Toast.LENGTH_SHORT).show()

            return true
        }

        try {
            FileInputStream(zipFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val destFile = File(folder, entry!!.name)
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