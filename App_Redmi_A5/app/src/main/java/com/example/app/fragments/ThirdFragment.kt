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
            }
        }

        return view
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

