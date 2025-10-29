package com.example.app.fragments

import DownloadHelper
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.decryptAndExtractArchive
import com.example.app.download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FifthFragment : Fragment() {

    private lateinit var downloadPlumaProfileButton: View
    private lateinit var   installPlumaProfileButton: View
    private lateinit var downloadTelegramProfileButton: View
    private lateinit var  installTelegramProfileButton: View
    private lateinit var editTextPassword: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        editTextPassword = view.findViewById(R.id.editTextPassword)
        downloadPlumaProfileButton = view.findViewById(R.id.downloadplumaprofile)
        installPlumaProfileButton = view.findViewById(R.id.installplumaprofile)
        downloadTelegramProfileButton = view.findViewById(R.id.downloadtelegramprofile)
        installTelegramProfileButton = view.findViewById(R.id.installtelegramprofile)

        downloadPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadProfile()

            }
        }

        installPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installProfile()
            }

            downloadTelegramProfileButton.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    downloadtelegramProfile()
                }
            }

            installTelegramProfileButton.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    installtelegramProfile()
                }
            }

        }

        return view
    }

    private suspend fun installtelegramProfile() {
        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptAndExtractArchive(requireContext(), password = enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }

    private suspend fun downloadtelegramProfile() {
        download(requireContext(),"https://github.com/definitly486/redmia5/releases/download/shared/org.thunderdog.challegram.tar.enc")

    }

    private suspend fun downloadProfile() {
        download(requireContext(),"https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc")
    }

    private suspend fun installProfile() {
        withContext(Dispatchers.IO) {
            try {
                // Получаем введенный пароль из поля ввода
                val enteredPassword = editTextPassword.text.toString()

                // Проверка на пустой пароль
                if (enteredPassword.isEmpty()) {
                    showToast("Пароль не введен. Пожалуйста, введите пароль.")
                    return@withContext
                }

                // Преобразование пароля и установка
                decryptAndExtractArchive(requireContext(), password = enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}