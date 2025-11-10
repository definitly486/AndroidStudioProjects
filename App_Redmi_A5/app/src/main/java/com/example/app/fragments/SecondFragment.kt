package com.example.app.fragments

import DownloadHelper
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import androidx.appcompat.app.AlertDialog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileReader
import java.io.IOException


class SecondFragment : Fragment() {
private val REQUEST_CODE_WRITE_SETTINGS_PERMISSION = 1001
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2

    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        downloadHelper2 = DownloadHelper2(requireContext())

        // Настройка кнопок
        setupButtons(view)

        return view
    }

    private fun setupButtons(view: View) {
        // Кнопка удаления пакета
        val installButton = view.findViewById<Button>(R.id.deletepkg)
        installButton.setOnClickListener { deletePkgFromFile("packages.txt") }

        // Кнопка скачивания busybox
        val downloadbusybox = view.findViewById<Button>(R.id.downloadbusybox)
        downloadbusybox.setOnClickListener { downloadBusyBox() }

        // Кнопка установки openssl
        val installopenssl = view.findViewById<Button>(R.id.installopenssl)
        installopenssl.setOnClickListener { downloadOpenSSL() }

        // Кнопка установки gh
        val installgh = view.findViewById<Button>(R.id.installgh)
        installgh.setOnClickListener { downloadGH() }

        // Кнопка скачивания ksuzip
        val downloadksuzip = view.findViewById<Button>(R.id.downloadksuzip)
        downloadksuzip.setOnClickListener { downloadKSUZip() }

        // Кнопка скачивания main
        val button6 = view.findViewById<Button>(R.id.downloadmain)
        button6.setOnClickListener { downloadMain() }

        // Кнопка распаковки main
        val button7 = view.findViewById<Button>(R.id.unpackmain)
        button7.setOnClickListener { unpackMain() }

        // Кнопка установки git
        val installgit = view.findViewById<Button>(R.id.installgit)
        installgit.setOnClickListener { installGit() }

        // Кнопка установки gnupg
        val installgnupg = view.findViewById<Button>(R.id.installgnupg)
        installgnupg.setOnClickListener { installGNUPG() }

        // Кнопка скачивания  APK
        val downloadapk = view.findViewById<Button>(R.id.downloadapk)
        downloadapk.setOnClickListener { downloadAPK() }

        // Кнопка установки  APK
        val installapk = view.findViewById<Button>(R.id.installapk)
        installapk.setOnClickListener { installAPK() }

        // Кнопка установки настроек
        val setting = view.findViewById<Button>(R.id.setsettings)
        setting.setOnClickListener { setSettings() }

    }

    private fun downloadBusyBox() {
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/busybox/busybox","busybox") { file ->
            handleDownloadResult(file, "busybox")
        }
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/curl/curl","curl") { file ->
            handleDownloadResult(file, "curl")
        }
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/openssl/openssl","openssl") { file ->
            handleDownloadResult(file, "openssl")
        }
    }

    private fun downloadOpenSSL() {
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/openssl/openssl","openssl") { file ->
            handleDownloadResult(file, "openssl")
        }
    }

    private fun downloadGH() {
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/gh/gh","gh") { file ->
            handleDownloadResult(file, "gh")
        }
    }

    private fun downloadAPK() {
        CoroutineScope(Dispatchers.Main).launch {
            launchLoadSequence()
        }
    }

    suspend fun launchLoadSequence() {
        val urls = listOf(
            "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/k9mail-13.0.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Google+Authenticator+7.0.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/com.aurora.store_70.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk",
            "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Telegram+X+0.27.5.1747-arm64-v8a.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Core+Music+Player_1.0.apk"
        )

        urls.forEachIndexed { index, url ->
            val result = downloadSingleAPK(url)
            handleResult(result, index + 1)
        }
    }

    suspend fun downloadSingleAPK(url: String): File? {
        return suspendCancellableCoroutine<File?> { continuation ->
            downloadHelper.downloadapk(url) { file ->
                continuation.resumeWith(Result.success(file))
            }
        }
    }

    fun handleResult(file: File?, index: Int) {
        if (file != null) {
            Toast.makeText(
                requireContext(),
                "Файл №$index загружен: ${file.name}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки файла №$index", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun downloadKSUZip() {
        downloadHelper.download2("https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip")

    }

    private fun downloadMain() {
        downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/archive/main.tar.gz")
    }

    private fun installAPK() {
        val urls = listOf(
            "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/k9mail-13.0.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Google+Authenticator+7.0.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/com.aurora.store_70.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk",
            "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk",
            "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Telegram+X+0.27.5.1747-arm64-v8a.apk",
            "https://github.com/definitly486/redmia5/releases/download/apk/Core+Music+Player_1.0.apk"
        )

        for (url in urls) {
            downloadHelper.installApk2(url)
        }
    }

    private fun setSettings() {

        if (!RootChecker.hasRootAccess(requireContext())) {
            showCompletionDialogroot(requireContext())
            return
        }


        // Анонимный объект для выполнения shell-команд
        val shellExecutor = object {
            fun execShellCommand(command: String): Boolean {
                var process: Process? = null
                var outputStream: DataOutputStream? = null

                return try {
                    process = Runtime.getRuntime().exec("su") // запускаем процесс с правами root
                    outputStream = DataOutputStream(process.outputStream)
                    outputStream.writeBytes("$command\nexit\n") // выполняем команду и заканчиваем сеанс
                    outputStream.flush()
                    outputStream.close()
                    process.waitFor()
                    Log.d("ShellExecutor", "Выполнено с результатом ${process.exitValue()}") // Логирование результата
                    process.exitValue() == 0 // возвращаем успех, если выходное значение равно 0
                } catch (e: Exception) {
                    Log.e("ShellExecutor", "Ошибка выполнения команды:", e)
                    false
                } finally {
                    outputStream?.close()
                    process?.destroy()
                }
            }
        }


        if (!Settings.System.canWrite(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS_PERMISSION)
        } else {
            // Если разрешение уже дано, меняем яркость сразу
            setScreenBrightness(requireContext(), 800) // Установим нормальное значение яркости (от 0 до 255)

            // Включаем глобальные настройки разработки
            shellExecutor.execShellCommand("settings put global development_settings_enabled 1")

            //Включаем navbar.gestural
            shellExecutor.execShellCommand("cmd overlay enable com.android.internal.systemui.navbar.gestural")

            //Выключение автояркости
            try {
                Settings.System.putInt(
                    requireContext().contentResolver, // Используем верный контекст
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL// Выключаем автояркость
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun setScreenBrightness(context: Context, brightnessValue: Int) {
        if (brightnessValue in 0..1000) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
        }
    }


    private fun unpackMain() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "main.tar.gz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл main.tar.gz не существует", Toast.LENGTH_SHORT).show()
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.decompressTarGz(tarGzFile, outputDir)
        Thread.sleep(5000L)
        downloadHelper2.copymain()
    }

    private fun installGit() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "git_aarch64.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл git_aarch64.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/releases/download/git/git_aarch64.tar.xz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copygit()
    }

    private fun installGNUPG() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "gnupg_aarch64.tar.xz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл gnupg_aarch64.tar.xz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/releases/download/gnupg/gnupg_aarch64.tar.xz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.unpackTarXz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copygnupg()
    }

    private fun handleDownloadResult(file: File?, name: String) {
        if (file != null) {
            Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки файла $name", Toast.LENGTH_SHORT).show()
        }
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


    fun Fragment.deletePkgFromFile(fileName: String) {
        if (!RootChecker.hasRootAccess(requireContext())) {
            showCompletionDialogroot(requireContext())
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Начинается удаление пакетов...", Toast.LENGTH_SHORT).show()
            }

            // Получаем внутреннюю директорию нашего приложения
            val appPrivateDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val filePath = "$appPrivateDirectory/$fileName"

            try {
                val file = File(filePath)
                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Файл не найден!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val reader = BufferedReader(FileReader(file))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) continue
                    deletePackage(line.trim())
                    delay(500) // Пауза между командами
                }
                reader.close()

                withContext(Dispatchers.Main) {
                    showCompletionDialog(requireContext())
                    Toast.makeText(requireContext(), "Удаление завершено!", Toast.LENGTH_SHORT).show()
                    createReloadDialog(requireContext())
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun showCompletionDialogroot(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверка root")
        builder.setMessage("Root доступ отсуствует,приложения не будут удалены")
        builder.setPositiveButton("Продолжить") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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

     fun createReloadDialog(context: Context) {
        val alertBuilder = AlertDialog.Builder(requireContext())

        // Заголовок диалога
        alertBuilder.setTitle("Подтверждение перезагрузки")

        // Сообщение в диалоговом окне
        alertBuilder.setMessage("Вы действительно хотите перезагрузить устройство?")

        // Положительная кнопка (перезагружаем устройство)
        alertBuilder.setPositiveButton("Да") { _: DialogInterface, _: Int ->
            // Логика перезагрузки устройства (нужны права администратора или root)
            // Пример:
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("su - root -c reboot", "/system/bin/reboot"))     }

        // Отрицательная кнопка (закрываем диалог)
        alertBuilder.setNegativeButton("Нет") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }

        // Показываем диалог
        val dialog = alertBuilder.create()
        dialog.show()
    }

}