package com.example.app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.app.R

class NinthFragment : Fragment() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etPassword: EditText
    private lateinit var btnDecrypt: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_ninth, container, false)

        // Привязываем элементы UI к соответствующим объектам
        initViews(rootView)
        setupUI()
        checkPermissions()

        return rootView
    }

    // Метод для подключения элементов интерфейса
    private fun initViews(view: View) {
        tvSelectedFile = view.findViewById(R.id.tvSelectedFile)
        btnSelectFile = view.findViewById(R.id.btnSelectFile)
        etPassword = view.findViewById(R.id.etPassword)
        btnDecrypt = view.findViewById(R.id.btnDecrypt)
        progressBar = view.findViewById(R.id.progressBar)
        tvStatus = view.findViewById(R.id.tvStatus)
    }

    // Подготовка пользовательского интерфейса
    private fun setupUI() {
        btnSelectFile.setOnClickListener { selectFile() }
        btnDecrypt.setOnClickListener { decryptFile() }

        etPassword.setOnKeyListener { _, _, _ ->
            updateDecryptButtonState()
            false
        }
    }

    // Проверка наличия необходимых разрешений
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            val shouldRequestPermissions = permissions.any { permission ->
                ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
            }

            if (shouldRequestPermissions) {
                // TODO: реализовать запрос разрешений здесь
                // Например, используя ActivityResultContract
            }
        }
    }

    // Выбор файла через ACTION_OPEN_DOCUMENT
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    // Простое обновление состояния кнопки "Дешифровать"
    private fun updateDecryptButtonState() {
        btnDecrypt.isEnabled = !etPassword.text.isNullOrBlank()
    }

    // Эмулятор функции расшифровки (реализуйте реальную логику позже)
    private fun decryptFile() {
        // Добавьте вашу логику дешифровки файла здесь
        Toast.makeText(context, "Нажата кнопка Дешифровать", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_CODE_SELECT_FILE = 1
    }
}