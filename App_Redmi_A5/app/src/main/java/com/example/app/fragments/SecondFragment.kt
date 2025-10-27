package com.example.app.fragments

import DownloadHelper
import android.annotation.SuppressLint
import android.content.Context

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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class SecondFragment : Fragment() {

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
    }

    private fun downloadBusyBox() {
        downloadHelper.downloadTool("https://github.com/definitly486/Lenovo_TB-X304L/releases/download/busybox/busybox","busybox") { file ->
            handleDownloadResult(file, "busybox")
        }
        downloadHelper.downloadTool("https://github.com/definitly486/redmia5/releases/download/curl/curl","curl") { file ->
            handleDownloadResult(file, "curl")
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

    private fun downloadKSUZip() {
        downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip")
        Thread.sleep(5000L)
        downloadHelper2.copyKSUZip()
    }

    private fun downloadMain() {
        downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/archive/main.tar.gz")
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
            showCompletionDialog_root(requireContext())
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
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun showCompletionDialog_root(context: Context) {
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
}