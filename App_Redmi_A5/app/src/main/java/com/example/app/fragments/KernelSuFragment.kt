@file:Suppress("SpellCheckingInspection")

package com.example.app.fragments

import DownloadHelper
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.KernelSUInstaller
import com.example.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KernelSuFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper
    private val TAG = "KernelSU_Installer"

    // Храним ожидаемый package name APK, чтобы поймать именно его установку
    private var pendingApkPackageName: String? = null

    // Receiver для отслеживания установки пакета
    private val pkgInstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val addedPkg = intent?.data?.schemeSpecificPart
            Log.d(TAG, "PACKAGE_ADDED received: $addedPkg, pending=$pendingApkPackageName")

            val expected = pendingApkPackageName
            if (expected == null) {
                // Нет ожидаемого пакета — игнорируем
                return
            }

            if (addedPkg == expected) {
                // Удаляем ожидание сразу, чтобы не запустить установку дважды
                pendingApkPackageName = null

                // Запускаем установку APatch-KSU.zip в фоне
                lifecycleScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        KernelSUInstaller.installAPatchKSUfromcachfolder(requireContext())
                    }

                    if (!isAdded) return@launch

                    if (success) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Установка завершена")
                            .setMessage("APatch-KSU успешно установлен!\n\nПерезагрузить устройство сейчас?")
                            .setPositiveButton("Перезагрузить") { _, _ ->
                                try {
                                    Runtime.getRuntime().exec("su -mm -c reboot")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "Не удалось выполнить перезагрузку",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Позже", null)
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка установки APatch-KSU.zip",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        downloadHelper = DownloadHelper(requireContext())
        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        // Регистрируем ресивер глобально (будет слушать, пока фрагмент видим)
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        requireContext().registerReceiver(pkgInstallReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(pkgInstallReceiver)
        } catch (ignored: Exception) {
        }
    }

    private fun setupButtons(view: View) {
        // Кнопка: установить APatch-KSU.zip (если уже установлен KSU можно запустить прямо)
        view.findViewById<Button>(R.id.install_apatch_ksu_zip).setOnClickListener {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            Log.d(TAG, "[$time] Нажата кнопка установки APatch-KSU.zip")

            Toast.makeText(requireContext(), "Установка APatch-KSU…", Toast.LENGTH_LONG).show()

            // Запускаем установку в фоне (если KernelSUInstaller.installAPatchKSU проверяет наличие zip в cache)
            lifecycleScope.launch(Dispatchers.IO) {
                val success = KernelSUInstaller.installAPatchKSUfromcachfolder(requireContext())

                withContext(Dispatchers.Main) {
                    if (success) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Установка завершена")
                            .setMessage("APatch-KSU успешно установлен!\n\nПерезагрузить устройство?")
                            .setPositiveButton("Перезагрузить") { _, _ ->
                                try {
                                    Runtime.getRuntime().exec("su -mm -c reboot")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "Не удалось выполнить перезагрузку",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Позже", null)
                            .setCancelable(false)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: APatch-KSU.zip не найден или установка провалилась",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Кнопка: скачать APatch-KSU.zip в публичную папку
        view.findViewById<Button>(R.id.downloadksuzip).setOnClickListener {
            downloadHelper.downloadToPublic(
                "https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip"
            )
            Toast.makeText(requireContext(), "Скачивание APatch-KSU.zip начато…", Toast.LENGTH_SHORT).show()
        }

        // Кнопка: распаковка и установка KernelSU (APK) - после установки APK автоматически установится zip
        view.findViewById<Button>(R.id.install_kermelsu).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                installKernelSuManager()
            }
        }
    }

    /**
     * 1) Распаковывает assets (APK + ZIP) в cache
     * 2) Определяет packageName APK
     * 3) Запускает системный установщик для APK
     * 4) Ждёт PACKAGE_ADDED с таким packageName и затем устанавливает APatch-KSU.zip
     */
    private fun installKernelSuManager() {

        val context = requireContext()

        // 1. Извлекаем APK + ZIP в cache
        val files = extractAssetsFiles(
            context,
            listOf(
                "KernelSU_v1.0.5_12081-release.apk" to 2_000_000L,
                "APatch-KSU.zip" to 1_000_000L
            )
        )

        val apkFile = files.firstOrNull { it.name.endsWith(".apk") }
        val zipFile = files.firstOrNull { it.name == "APatch-KSU.zip" }

        if (apkFile == null || zipFile == null) {
            Toast.makeText(context, "Файлы не найдены", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Готовим URI для APK
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        // 3. Проверяем разрешение
        if (!context.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        }

        // 4. Запускаем системный установщик APK
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(installIntent)

        // 5. После установки — ставим APatch-KSU.zip (root)
        lifecycleScope.launch(Dispatchers.IO) {

            // Ожидаем появление установленного KernelSU
            var kernelSuInstalled = false

            repeat(20) {    // максимум 10 секунд
                if (isPackageInstalled(context, "me.weishu.kernelsu")) {
                    kernelSuInstalled = true
                    return@repeat
                }
                delay(500)
            }

            if (!kernelSuInstalled) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "KernelSU не установлен", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            // Устанавливаем ZIP через root
            val success = KernelSUInstaller.installAPatchKSUfromcachfolder(context)

            withContext(Dispatchers.Main) {
                if (success) {
                    AlertDialog.Builder(context)
                        .setTitle("APatch-KSU установлен")
                        .setMessage("Перезагрузить устройство?")
                        .setPositiveButton("Перезагрузить") { _, _ ->
                            try {
                                Runtime.getRuntime().exec("su -mm -c reboot")
                            } catch (_: Exception) {
                                Toast.makeText(context, "Ошибка перезагрузки", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Позже", null)
                        .show()
                } else {
                    Toast.makeText(context, "Ошибка установки APatch-KSU.zip", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun isPackageInstalled(context: Context, pkg: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }
    }


    // ---------------------------
    //     РАСПАКОВКА ASSETS ФАЙЛОВ
    // ---------------------------
    private fun extractAssetsFiles(
        context: Context,
        files: List<Pair<String, Long>>
    ): List<File> {

        val out = mutableListOf<File>()

        for ((name, minSize) in files) {
            val target = File(context.cacheDir, name)

            if (target.exists() && target.length() > minSize) {
                out.add(target)
                continue
            }

            if (target.exists()) target.delete()

            try {
                context.assets.open(name).use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                out.add(target)
            } catch (e: Exception) {
                target.delete()
                Log.w(TAG, "Failed extract $name: ${e.message}")
            }
        }

        return out
    }
}
