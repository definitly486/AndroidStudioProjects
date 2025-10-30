package com.example.app.fragments

import DownloadHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ThirdFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third, container, false)
        downloadHelper = DownloadHelper(requireContext())
        downloadHelper2 = DownloadHelper2(requireContext())
        setupInstallButton(view)
        setupDownloadNoteButton(view)
        setupGitCloneButton(view)
        setupCopyCloneButton(view)

        return view
    }

    private fun setupInstallButton(view: View) {
        val installButton = view.findViewById<Button>(R.id.downloadgnucashgpg)
        installButton.setOnClickListener {
            val apkUrl1 = "https://github.com/xinitronix/gnucash/raw/refs/heads/main/definitly.gnucash.gpg"
            downloadHelper.download2(apkUrl1)

        }
    }

    private fun setupDownloadNoteButton(view: View) {
        val downloadnote = view.findViewById<Button>(R.id.downloadnote)
        downloadnote.setOnClickListener {
            val apkUrl1 = "https://raw.githubusercontent.com/definitly486/definitly486/refs/heads/main/note"
            downloadHelper.download2(apkUrl1)
        }
    }

    private fun setupGitCloneButton(view: View) {
        val gitCloneButton = view.findViewById<Button>(R.id.gitclonedcim)
        gitCloneButton.setOnClickListener {
            lifecycleScope.launch {
                val gitCloneJob = async(Dispatchers.IO) { handleGitCloneOperation() }
                val success = gitCloneJob.await() // Ждём завершения клонирования и получаем результат

                if (success) {
                    // Клонирование прошло успешно, выполняем следующую функцию
                    copymain()
                } else {
                    Toast.makeText(context, "Ошибка клонирования, невозможно скопировать файлы.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCopyCloneButton(view: View) {
        val copyCloneButton = view.findViewById<Button>(R.id.copydcim)
        copyCloneButton.setOnClickListener {
            copymain()
        }
    }

    fun copymain() {
        Toast.makeText(context, "Копируем DCIM...", Toast.LENGTH_SHORT).show()

        val prepareCommands = arrayOf(
            "su - root -c chmod -R 0755 /storage/emulated/0/Android/data/com.example.app/files/Download/DCIM"
        )
        executeCommands(prepareCommands)

        val ownerCmd = "su - root -c ls -l /data_mirror/data_ce/null/0/com.termos | awk '{print \$3}' | head -n 2"
        val fileOwner = execShell(ownerCmd)?.trim() ?: ""

        val commands = arrayOf(
            "su - root -c cp -R /storage/emulated/0/Android/data/com.example.app/files/Download/DCIM /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chmod -R 0755 /data_mirror/data_ce/null/0/com.termos/files/home/DCIM",
            "su - root -c chown -R $fileOwner:$fileOwner /data_mirror/data_ce/null/0/com.termos/files/home/DCIM"
        )
        executeCommands(commands)

        Toast.makeText(context, "Копирование DCIM завершено", Toast.LENGTH_SHORT).show()
    }

    private fun executeCommands(commands: Array<String>) {
        var process: Process? = null
        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor()
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копировании DCIM: $command", Toast.LENGTH_LONG).show()
                return
            }
        }
    }

    private fun execShell(cmd: String): String? {
        try {
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            if (process.exitValue() != 0) {
                throw Exception("Ошибка при выполнении команды: $cmd")
            }

            val outputStream = BufferedReader(InputStreamReader(process.inputStream))
            val resultBuilder = StringBuilder()
            while (true) {
                val line = outputStream.readLine() ?: break
                resultBuilder.append(line).append("\n")
            }
            return resultBuilder.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private suspend fun handleGitCloneOperation(): Boolean {
        val gitClone = GitClone()
        return withContext(Dispatchers.IO) {
            val result = gitClone.cloneRepository()
            if (result.isSuccess) {
                showToast("Репозиторий успешно клонирован.")
                return@withContext true
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                showToast("Ошибка клонирования: $errorMessage")
                return@withContext false
            }
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}