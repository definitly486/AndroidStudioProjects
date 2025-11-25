package com.example.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app.BuildConfig
import com.example.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TenthFragment : Fragment() {

    private lateinit var btnFactoryReset: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tenth, container, false)

        // === Дата сборки ===
        val buildDate = Date(BuildConfig.BUILD_TIME)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        rootView.findViewById<TextView>(R.id.buildTimeText).text =
            "APK создан: ${formatter.format(buildDate)}"

        // === Версия и ветка ===
        val packageInfo = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0)
        rootView.findViewById<TextView>(R.id.versionNameText).text =
            "Версия приложения: ${packageInfo.versionName ?: "unknown"}"

        val suffix = BuildConfig.VERSION_NAME_SUFFIX
        rootView.findViewById<TextView>(R.id.branchText).text =
            if (suffix.isNotEmpty()) "Ветка Git: $suffix" else "Ветка: release"

        // === Root-чекбоксы ===
        rootView.findViewById<CheckBox>(R.id.checkBox).isChecked =
            RootChecker.hasRootAccess(requireContext())
        rootView.findViewById<CheckBox>(R.id.checkBox2).isChecked =
            RootChecker.checkWriteAccess("/system")

        // === КНОПКА СБРОСА ВСЕХ ДАННЫХ ===
        btnFactoryReset = rootView.findViewById(R.id.btnFactoryReset)
        btnFactoryReset.setOnClickListener {
            showConfirmDialog()
        }

        return rootView
    }

    // Диалог подтверждения — чтобы случайно не удалить всё
    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("ПОЛНЫЙ СБРОС ТЕЛЕФОНА")
            .setMessage(
                "ВНИМАНИЕ!\n\n" +
                        "Это действие удалит ВСЁ:\n" +
                        "• Все приложения\n" +
                        "• Фото, видео, музыку\n" +
                        "• Контакты и аккаунты\n" +
                        "• Настройки\n\n" +
                        "Телефон станет как только из коробки.\n\n" +
                        "Продолжить?"
            )
            .setNegativeButton("Отмена", null)
            .setPositiveButton("ДА, УДАЛИТЬ ВСЁ") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    deleteAllData()
                }
            }
            .setCancelable(false)
            .show()
    }


    // Основная функция полного сброса
    private suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            try {
                btnFactoryReset.isEnabled = false
                btnFactoryReset.text = "Выполняется сброс…"

                // Выполнение команды для сброса всех данных кроме SD-карты
                val command = "su - root -c am broadcast -p android -a android.intent.action.FACTORY_RESET --receiver-foreground --ez android.intent.extra.WIPE_EXTERNAL_STORAGE false"
                Runtime.getRuntime().exec(command).waitFor()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    btnFactoryReset.isEnabled = true
                    btnFactoryReset.text = "СБРОСИТЬ ВСЁ НА ТЕЛЕФОНЕ"

                    // Сообщение пользователю
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Заводской сброс выполнен!")
                        .setMessage("Ваш телефон успешно сброшен до заводских настроек. Данные на SD-карте сохранены.")
                        .setPositiveButton("Закрыть", null)
                        .show()
                }
            }
        }
    }
}