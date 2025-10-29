package com.example.app.fragments

import DownloadHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.decryptAndExtractArchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FifthFragment : Fragment() {


    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadPlumaProfileButton: View
    private lateinit var installPlumaProfileButton: View
    private lateinit var editTextPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        // Получить доступ к полям ввода и кнопкам
        editTextPassword = view.findViewById(R.id.editTextPassword)
        downloadPlumaProfileButton = view.findViewById(R.id.downloadplumaprofile)
        installPlumaProfileButton = view.findViewById(R.id.installplumaprofile)

        // Настроить действия кнопок
        downloadPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                downloadProfile()
            }
        }

        installPlumaProfileButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                installProfile()
            }
        }

        return view
    }

    /**
     * Скачивание профиля в отдельном фоне
     */
    private suspend fun downloadProfile() {

        downloadHelper = DownloadHelper(requireContext())
        downloadHelper.downloadgpg("https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc")
    }

    /**
     * Установка и декодирование профиля в отдельном фоне
     */
    private suspend fun installProfile() {
        withContext(Dispatchers.IO) {
            try {
                // Получаем введённый пароль из TextView
                val enteredPassword = editTextPassword.text.toString()

                // Передаём пароль в функцию decryption
                decryptAndExtractArchive(requireContext(), password = enteredPassword)
                showToast("Архив успешно установлен и извлечён!")
            } catch (e: Exception) {
                showToast("Ошибка при установке и извлечении архива: ${e.message}")
            }
        }
    }

    /**
     * Вспомогательная функция для отображения уведомлений
     */
    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }
}