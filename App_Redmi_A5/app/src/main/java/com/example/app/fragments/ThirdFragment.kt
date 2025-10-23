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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


class ThirdFragment :  Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third, container, false)
        // Инициализация
        downloadHelper = DownloadHelper(requireContext())

        val installButton = view.findViewById<Button>(R.id.downloadgnucashgpg)
        installButton.setOnClickListener {
            val apkUrl1 =
                "https://github.com/xinitronix/gnucash/raw/refs/heads/main/definitly.gnucash.gpg"
            downloadHelper.downloadgpg(apkUrl1)

        }

        val downloadnote = view.findViewById<Button>(R.id.downloadnote)
        downloadnote.setOnClickListener {
            val apkUrl1 =
                "https://raw.githubusercontent.com/definitly486/definitly486/refs/heads/main/note"
            downloadHelper.downloadgpg(apkUrl1)

        }

        // Найдем кнопку для запуска клонирования
        val gitCloneButton = view.findViewById<Button>(R.id.gitclonedcim)
        gitCloneButton.setOnClickListener {
            lifecycleScope.launch {
                handleGitCloneOperation()
                copymain()
            }
        }

        return view
    }

    fun copymain() {
        Toast.makeText(context, "Копируем DCIM ...", Toast.LENGTH_SHORT).show()


        val prepareCommands =
            arrayOf("su - root -c chmod -R 0755 /storage/emulated/0/Android/data/com.example.app/files/Download/DCIM")
        for (command in prepareCommands) {
            Runtime.getRuntime().exec(command).waitFor()
        }

        val ownerCmd =
            "su - root -c   ls -l   /data_mirror/data_ce/null/0/com.termos | awk '{print $3}' | head -n 2"
        val fileOwner = execShell(ownerCmd)?.trim() ?: ""

        val commands = arrayOf(

            "su - root -c cp  -R /storage/emulated/0/Android/data/com.example.app/files/Download/DCIM /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chmod -R 0755 /data_mirror/data_ce/null/0/com.termos/files/home/DCIM",
            "su - root -c chown -R  $fileOwner:$fileOwner /data_mirror/data_ce/null/0/com.termos/files/home/DCIM"
        )

        var process: Process? = null

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование DCIM: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }
        Toast.makeText(context, "Копирование  DCIM завершенo", Toast.LENGTH_SHORT).show()
    }

    // Вспомогательная функция для выполнения shell-команд
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


    private suspend fun handleGitCloneOperation() {
        val gitClone = GitClone()
        withContext(Dispatchers.IO) {
            val result = gitClone.cloneRepository()
            if (result.isSuccess) {
                showToast("Репозиторий успешно клонирован.")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                showToast("Ошибка клонирования: $errorMessage")
            }
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

}

