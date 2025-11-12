package com.example.app.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import java.io.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.*

class NinthFragment : Fragment() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etPassword: EditText
    private lateinit var btnDecrypt: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private var selectedFileUri: Uri? = null
    private var outputFile: File? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleFileSelected(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_ninth, container, false)
        initViews(rootView)
        setupUI()
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
        etPassword.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDecryptButtonState()
            }
        })
    }

    private fun selectFile() {
        selectFileLauncher.launch(arrayOf("*/*"))
    }

    private fun handleFileSelected(uri: Uri) {
        selectedFileUri = uri
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) { /* игнорируем, если временный доступ */ }

        tvSelectedFile.text = "Выбран: ${getFileName(uri)}"
        updateDecryptButtonState()
    }

    private fun getFileName(uri: Uri): String {
        return try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else "файл"
            } ?: "файл"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "файл"
        }
    }

    private fun updateDecryptButtonState() {
        btnDecrypt.isEnabled = selectedFileUri != null && etPassword.text.toString().trim().isNotEmpty()
    }

    private fun decryptFile() {
        val password = etPassword.text.toString().trim()
        if (password.isEmpty() || selectedFileUri == null) return

        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Расшифровка..."
        btnDecrypt.isEnabled = false
        outputFile = getOutputFile()

        // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: viewLifecycleOwner.lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                decryptWithOpenSslFormat(selectedFileUri!!, outputFile!!, password)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    private fun getOutputFile(): File {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        dir.mkdirs()
        return File(dir, "decrypted_${System.currentTimeMillis()}.txt")
    }

    private fun decryptWithOpenSslFormat(inputUri: Uri, outputFile: File, password: String) {
        requireContext().contentResolver.openInputStream(inputUri)?.use { input ->
            FileOutputStream(outputFile).use { fileOut ->

                // 1. Читаем "Salted__"
                val header = ByteArray(8)
                if (input.read(header) != 8 || !header.contentEquals("Salted__".toByteArray())) {
                    throw IllegalArgumentException("Файл не в формате OpenSSL")
                }

                // 2. Читаем соль (8 байт)
                val salt = ByteArray(8)
                if (input.read(salt) != 8) throw IOException("Не удалось прочитать salt")

                // 3. Генерируем ключ (32) + IV (16) через PBKDF2-HMAC-SHA256
                val (key, iv) = deriveKeyAndIvWithPbkdf2(password, salt)

                // 4. Расшифровка
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
                    init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
                }

                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    input.copyTo(cipherOut) // ВЕСЬ остаток — ciphertext
                }
            }
        } ?: throw IOException("Не удалось открыть файл")
    }
    private fun deriveKeyAndIvWithPbkdf2(password: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 384) // 384 бита = 32 + 16
        val key = factory.generateSecret(spec).encoded

        val aesKey = ByteArray(32)
        val iv = ByteArray(16)
        System.arraycopy(key, 0, aesKey, 0, 32)
        System.arraycopy(key, 32, iv, 0, 16)

        return aesKey to iv
    }

    private fun onSuccess() {
        progressBar.visibility = View.GONE
        tvStatus.text = "Успешно!"
        btnDecrypt.isEnabled = true
        Toast.makeText(requireContext(), "Файл расшифрован: ${outputFile?.name}", Toast.LENGTH_LONG).show()
    }

    private fun onError(e: Exception) {
        progressBar.visibility = View.GONE
        tvStatus.text = "Ошибка"
        btnDecrypt.isEnabled = true
        Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
    }

    abstract class SimpleTextWatcher : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }
}