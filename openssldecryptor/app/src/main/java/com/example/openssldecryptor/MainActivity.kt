package com.example.openssldecryptor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.*
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etPassword: EditText
    private lateinit var btnDecrypt: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private var encryptedFileUri: Uri? = null
    private var encryptedFilePath: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            showToast("Permissions are required for file operations")
        }
    }

    @SuppressLint("SetTextI18n")
    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                encryptedFileUri = uri
                encryptedFilePath = getFileNameFromUri(uri)
                tvSelectedFile.text = "Selected: ${encryptedFilePath ?: "Unknown file"}"
                updateDecryptButtonState()
                showStatus("File selected successfully", false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupUI()
        checkPermissions()
    }

    private fun initViews() {
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        etPassword = findViewById(R.id.etPassword)
        btnDecrypt = findViewById(R.id.btnDecrypt)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
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

            val shouldRequestPermissions = permissions.any { permission ->
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            }

            if (shouldRequestPermissions) {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFileLauncher.launch(intent)
    }

    private fun updateDecryptButtonState() {
        val isFileSelected = encryptedFileUri != null
        val isPasswordEntered = etPassword.text?.isNotEmpty() == true
        btnDecrypt.isEnabled = isFileSelected && isPasswordEntered
    }

    private fun decryptFile() {
        val password = etPassword.text?.toString()
        if (password.isNullOrEmpty()) {
            showStatus("Please enter password", true)
            return
        }

        encryptedFileUri?.let { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    showProgress(true)

                    val result = withContext(Dispatchers.IO) {
                        decryptOpenSSLFile(uri, password)
                    }

                    if (result.isSuccess) {
                        val outputPath = result.getOrNull()
                        showStatus("File decrypted successfully!", false)
                        showToast("File saved to: ${getOutputDirectory().absolutePath}")
                        openDecryptedFile(outputPath)
                    } else {
                        handleDecryptionError(result.exceptionOrNull())
                    }
                } finally {
                    showProgress(false)
                }
            }
        } ?: showStatus("Please select a file first", true)
    }

    private fun decryptOpenSSLFile(encryptedFileUri: Uri, password: String): Result<String> = runCatching {
        val inputStream = contentResolver.openInputStream(encryptedFileUri)
            ?: throw IOException("Cannot open input file")

        BufferedInputStream(inputStream).use { bufferedInputStream ->
            // Проверяем заголовок OpenSSL
            val header = ByteArray(8)
            bufferedInputStream.mark(16)
            val bytesRead = bufferedInputStream.read(header)

            return@runCatching if (bytesRead == 8 && header.copyOfRange(0, 8).contentEquals("Salted__".toByteArray())) {
                // Файл с солью - читаем соль
                val salt = ByteArray(8)
                if (bufferedInputStream.read(salt) != 8) {
                    throw IOException("Cannot read salt from file")
                }
                val (key, iv) = generateKeyAndIV(password, salt)
                decryptWithCipher(bufferedInputStream, key, iv, encryptedFileUri)
            } else {
                // Файл без соли - возвращаемся к началу
                bufferedInputStream.reset()
                val (key, iv) = generateKeyAndIV(password, ByteArray(8))
                decryptWithCipher(bufferedInputStream, key, iv, encryptedFileUri)
            }
        }
    }

    private fun decryptWithCipher(
        inputStream: InputStream,
        key: ByteArray,
        iv: ByteArray,
        encryptedFileUri: Uri
    ): String {
        val cipher = createOpenSSLCipher(key, iv)
        val cipherInputStream = CipherInputStream(inputStream, cipher)

        val originalFileName = getFileNameFromUri(encryptedFileUri) ?: "encrypted_file"
        val outputFileName = generateOutputFileName(originalFileName)
        val outputFile = File(getOutputDirectory(), outputFileName)

        // Убедимся, что файл не существует
        if (outputFile.exists()) {
            outputFile.delete()
        }

        FileOutputStream(outputFile).use { outputStream ->
            cipherInputStream.copyTo(outputStream)
        }

        cipherInputStream.close()

        return outputFile.absolutePath
    }

    private fun generateKeyAndIV(password: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
        // Упрощенная генерация ключа и IV
        // В реальном приложении используйте PBKDF2 или аналогичный KDF
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val keyAndIV = ByteArray(48) // 32 байта ключ + 16 байт IV

        // Простая ключевая деривация (для демонстрации)
        for (i in keyAndIV.indices) {
            val passwordIndex = i % passwordBytes.size
            val saltIndex = i % salt.size
            keyAndIV[i] = (passwordBytes[passwordIndex].toInt() xor salt[saltIndex].toInt()).toByte()
        }

        val key = keyAndIV.copyOfRange(0, 32)
        val iv = keyAndIV.copyOfRange(32, 48)

        return Pair(key, iv)
    }

    private fun createOpenSSLCipher(key: ByteArray, iv: ByteArray): Cipher {
        return try {
            val secretKeySpec = SecretKeySpec(key, "AES")
            val ivParameterSpec: AlgorithmParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
            cipher
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize cipher: ${e.message}", e)
        }
    }

    private fun generateOutputFileName(originalName: String): String {
        var baseName = originalName
            .removeSuffix(".enc")
            .removeSuffix(".aes")
            .removeSuffix(".des3")
            .removeSuffix(".crypt")
            .removeSuffix(".encrypted")

        // Удаляем multiple extensions
        while (baseName.contains(".enc.") || baseName.contains(".aes.")) {
            baseName = baseName.removeSuffix(".enc").removeSuffix(".aes")
        }

        return "decrypted_$baseName"
    }

    private fun getOutputDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Decrypted")
        } else {
            File(Environment.getExternalStorageDirectory(), "Documents/OpenSSLDecryptor")
        }.apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    cursor.getString(displayNameIndex)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun handleDecryptionError(exception: Throwable?) {
        val errorMessage = when {
            exception is javax.crypto.BadPaddingException ->
                "Wrong password or corrupted file"
            exception is java.io.IOException ->
                "File error: ${exception.message}"
            exception is java.security.InvalidKeyException ->
                "Invalid encryption key"
            else ->
                "Decryption failed: ${exception?.message ?: "Unknown error"}"
        }
        showStatus(errorMessage, true)
        showToast(errorMessage)
    }

    private fun openDecryptedFile(filePath: String?) {
        filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                try {
                    val uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, getMimeType(file))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(intent, "Open decrypted file with..."))
                } catch (e: Exception) {
                    showToast("Cannot open file: No suitable app found")
                }
            }
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "txt", "log" -> "text/plain"
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "zip" -> "application/zip"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xml" -> "application/xml"
            "json" -> "application/json"
            else -> "*/*"
        }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) ProgressBar.VISIBLE else ProgressBar.GONE
        btnDecrypt.isEnabled = !show
        btnSelectFile.isEnabled = !show
    }

    private fun showStatus(message: String, isError: Boolean) {
        tvStatus.text = message
        tvStatus.setTextColor(
            ContextCompat.getColor(this,
                if (isError) android.R.color.holo_red_dark
                else android.R.color.holo_green_dark
            )
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}