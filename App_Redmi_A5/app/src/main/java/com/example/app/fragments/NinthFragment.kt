package com.example.app.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.app.R
import android.app.Activity.RESULT_OK
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NinthFragment : Fragment() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etPassword: EditText
    private lateinit var btnDecrypt: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private var selectedFileUri: Uri? = null // Хранит URI выбранного файла

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_ninth, container, false)
        initViews(rootView)
        setupUI()
        checkPermissions()
        return rootView
    }

    private fun initViews(view: View) {
        tvSelectedFile = view.findViewById(R.id.tvSelectedFile)
        btnSelectFile = view.findViewById(R.id.btnSelectFile)
        etPassword = view.findViewById(R.id.etPassword)
        btnDecrypt = view.findViewById(R.id.btnDecrypt)
        progressBar = view.findViewById(R.id.progressBar)
        tvStatus = view.findViewById(R.id.tvStatus)
    }

    private fun setupUI() {
        btnSelectFile.setOnClickListener { selectFile() }
        btnDecrypt.setOnClickListener { decryptFile() }
        etPassword.setOnKeyListener { _, _, _ ->
            updateDecryptButtonState()
            false
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
       //         contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                tvSelectedFile.text = "Выбран файл: $uri"
                updateDecryptButtonState()
            }
        }
    }

    private fun updateDecryptButtonState() {
        btnDecrypt.isEnabled = selectedFileUri != null && !etPassword.text.isNullOrBlank()
    }

    private fun decryptFile() {
        val password = etPassword.text.toString()
        val inputFileUri = selectedFileUri ?: return
        val outputFilePath = "/storage/emulated/0/Android/data/com.example.decryptopenssl/files/Download/new.txt"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                decryptFile(inputFileUri, outputFilePath, password)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Файл успешно расшифрован", Toast.LENGTH_LONG).show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Ошибка: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun decryptFile(inputFileUri: Uri, outputFilePath: String, password: String) {

        val command = listOf(
            "openssl",
            "enc",
            "-d",
            "-iter",
            "100000",
            "-pbkdf2",
            "-aes-256-cbc",                     // Алгоритм шифрования
            "-in",inputFileUri,
            "-out", outputFilePath,
            "-pass", "pass:$password"
        )

        // Выполняем команду
        val result = runCommand(command as List<String>)
        println(result)

        if (result.contains("error")) {
            println("Ошибка при расшифровке!")
        } else {
            println("Файл успешно расшифрован!")
        }

    }

    fun runCommand(command: List<String>): String {
        return ProcessBuilder().command(command)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText()
    }

    companion object {
        const val REQUEST_CODE_SELECT_FILE = 1
        const val PERMISSION_REQUEST_CODE = 2
    }
}