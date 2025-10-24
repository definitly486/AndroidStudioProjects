package com.example.app.fragments

import DownloadHelper
import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.*
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import androidx.appcompat.app.AlertDialog
import java.io.File

class SecondFragment : Fragment() {
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2
    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        // Инициализация
        downloadHelper = DownloadHelper(requireContext())

        val installButton = view.findViewById<Button>(R.id.deletepkg)
        installButton.setOnClickListener {
      deletepkg()
        }


        val downloadbusybox = view.findViewById<Button>(R.id.downloadbusybox)
        downloadbusybox.setOnClickListener {
            downloadHelper.downloadbusybox("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/busybox/busybox") { file ->
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "Файл загружен: ${file.name}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }

            downloadHelper.downloadbusybox("https://github.com/definitly486/redmia5/releases/download/curl/curl") { file ->
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "Файл загружен: ${file.name}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }


        }

        val installopenssl = view.findViewById<Button>(R.id.installopenssl)
        installopenssl.setOnClickListener {
            downloadHelper.downloadopenssl("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/openssl/openssl") { file ->
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "Файл загружен: ${file.name}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installgh = view.findViewById<Button>(R.id.installgh)
        installgh.setOnClickListener {
            downloadHelper.downloadgh("https://github.com/definitly486/redmia5/releases/download/gh/gh") { file ->
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "Файл загружен: ${file.name}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val downloadksuzip = view.findViewById<Button>(R.id.downloadksuzip)
        downloadksuzip.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip"
            downloadHelper.downloadgpg(apkUrl1)

        }


        val button6 = view.findViewById<Button>(R.id.downloadmain)
        button6.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.downloadgpg("https://github.com/definitly486/redmia5/archive/main.tar.gz")
        }

        val button7 = view.findViewById<Button>(R.id.unpackmain)
        button7.setOnClickListener {
            val folder = getDownloadFolder() ?: return@setOnClickListener
            val tarGzFile = File(folder,"main.tar.gz")
            val outputDir = File(folder, "")
            if (!tarGzFile.exists()) {
                Toast.makeText(requireContext(), "Файл main.tar.gz не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            downloadHelper2 = DownloadHelper2(requireContext())
            downloadHelper2.decompressTarGz (tarGzFile, outputDir)
            Thread.sleep(5000L)
            downloadHelper2.copymain()
        }

        val installgit = view.findViewById<Button>(R.id.installgit)
        installgit.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            val folder = getDownloadFolder() ?: return@setOnClickListener
            val tarGzFile = File(folder,"git_aarch64.tar.xz")
            val outputDir = File(folder, "")
            if (!tarGzFile.exists()) {
                Toast.makeText(requireContext(), "Файл git_aarch64.tar.xz не существует", Toast.LENGTH_SHORT).show()
                helper.downloadgpg("https://github.com/definitly486/redmia5/releases/download/git/git_aarch64.tar.xz")
                return@setOnClickListener
            }

            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/git/git_aarch64.tar.xz"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
            downloadHelper2 = DownloadHelper2(requireContext())
            downloadHelper2.unpackTarXz(tarGzFile, outputDir)
            Thread.sleep(5000L)
            downloadHelper2.copygit()


        }


        return view
    }

    suspend fun deletePackage(packageName: String) {
        withContext(Dispatchers.IO) {
            // Показываем сообщение о текущем процессе
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Удаляется пакет: $packageName", Toast.LENGTH_SHORT).show()
            }
            try {
                val process = Runtime.getRuntime().exec("su -c pm uninstall --user 0 $packageName")
                val exitCode = process.waitFor()
                // Можно проверить exitCode, чтобы убедиться, что команда завершилась успешно
                if (exitCode == 0) {
                    // Успешно
                } else {
                    // Ошибка
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletepkg() {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Начинается удаление пакетов ...", Toast.LENGTH_SHORT).show()

            val packagesToDelete = listOf(
                "com.miui.analytics.go",
                "ru.ivi.client",
                "ru.vk.store",
                "com.vk.vkvideo",
                "ru.beru.android",
                "ru.more.play",
                "ru.oneme.app",
                "com.miui.player",
                "com.miui.videoplayer",
                "com.miui.theme.lite",
                "com.miui.gameCenter.overlay",
                "com.miui.videoplayer.overlay",
                "com.miui.bugreport",
                "com.miui.cleaner.go",
                "com.miui.player.overlay",
                "com.miui.msa.global",
                "com.yandex.searchapp",
                "com.yandex.browser",
                "com.google.android.youtube",
                "com.google.android.apps.youtube.music",
                "com.android.shareMe.overlay",
                "com.vitastudio.mahjong",
                "com.oakever.tiletrip",
                "com.ordinaryjoy.woodblast",
                "com.go.browser",
                "com.facebook.appmanager",
                "com.google.android.apps.tachyon",
                "com.xiaomi.midrop",
                "com.miui.global.packageinstaller",
                "com.xiaomi.discover",
                "com.xiaomi.mipicks",
                "com.google.android.videos",
                "com.miui.android.fashiongallery",
                "com.google.android.apps.safetyhub",
                "com.google.android.overlay.gmsconfig.searchgo",
                "com.google.android.apps.searchlite",
                "com.google.android.appsearch.apk",
                "com.google.android.apps.docs",
                "com.xiaomi.glgm",
                "com.google.android.gm",
                "com.yandex.preinstallsatellite",
                "com.tencent.soter.soterserver " ,
                "com.android.bookmarkprovider",
                "com.xiaomi.mipicks",
                "com.xiaomi.discover",
                "com.facebook.services",
                "com.android.bips",
                "com.android.stk",
                "com.facebook.system",
                "com.google.android.feedback",
                "com.google.android.go.documentsui",
                "android.autoinstalls.config.Xiaomi.model",
                "com.google.android.apps.wellbeing",
                "com.android.vending",
                "com.android.musicfx",
                "com.google.android.tts",
                "com.mi.globalminusscreen",
                "com.android.printspooler",
                "com.google.android.printservice.recommendation",
                "com.google.android.setupwizard",
                "com.android.ons",
                "com.google.android.partnersetup",
                "com.android.providers.partnerbookmarks",
                "com.mi.android.globalFileexplorer.overlay",
                "com.android.backupconfirm",
                "android.overlay.multiuser",
                "com.android.calllogbackup",
                "com.android.cameraextensions",
                "com.google.android.marvin.talkback",
                "org.ifaa.aidl.manager",
                "com.android.wallpaperbackup",
                "com.android.avatarpicker",
                "com.google.android.apps.subscriptions.red",
                "com.google.android.ext.shared",
                "com.android.sharedstoragebackup",
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.walletnfcrel",
                "com.kms.free",
                "com.google.android.apps.magazines",
                "com.google.android.apps.assistant",
                "com.yandex.searchapp",
                "com.silead.factorytest"
            )

            for (packageName in packagesToDelete) {
                deletePackage(packageName)
                // Можно добавить задержку, если нужно
                delay(500) }
            // После завершения удаления показываем диалог
            showCompletionDialog(requireContext())
            Toast.makeText(context, "Удаление завершено!", Toast.LENGTH_SHORT).show()
        }
    }
    fun showCompletionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Удаление завершено")
        builder.setMessage("Все выбранные пакеты успешно удалены.")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}