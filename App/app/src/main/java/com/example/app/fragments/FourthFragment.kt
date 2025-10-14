package com.example.app.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.app.R
import org.bouncycastle.jce.provider.BouncyCastleProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.annotation.SuppressLint
import android.provider.OpenableColumns
import com.example.app.databinding.FragmentFourthBinding
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class FourthFragment : Fragment() {

    private lateinit var downloadManager: DownloadManager
    private var inputFileUri: Uri? = null
    private var _binding: FragmentFourthBinding? = null
    private val binding get() = _binding!!

    private val selectFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { updateSelectedFileInfo(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFourthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Security.addProvider(BouncyCastleProvider())

        binding.buttonSelectFile.setOnClickListener {
            if (isStoragePermissionGranted()) {
                selectFileLauncher.launch(arrayOf("*/*"))
            } else {
                requestStoragePermission()
            }
        }

        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        binding.buttonDecrypt.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(requireContext(), "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            decryptFileWithFallback(inputFileUri!!, password)
        }

    }

    override fun onResume() {
        super.onResume()
        // ... existing code ...
    }

    override fun onPause() {
        super.onPause()
        // ... existing code ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun updateSelectedFileInfo(uri: Uri) {
        inputFileUri = uri
        val fileName = getFileNameFromUri(uri)
        binding.fileNameLabel.text = "Выбран файл: $fileName"
    }

    private fun isStoragePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            binding.buttonSelectFile.callOnClick()
        } else {
            Toast.makeText(requireContext(), "Доступ запрещен", Toast.LENGTH_LONG).show()
        }
    }

    // Main decryption method with multiple fallbacks
    private fun decryptFileWithFallback(fileUri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                requireContext().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    val encryptedData = inputStream.readBytes()

                    val result = tryAllDecryptionMethods(encryptedData, password)

                    withContext(Dispatchers.Main) {
                        if (result.success) {
                            binding.outputData.text = "Результат:\n${result.data}"
                            Toast.makeText(requireContext(), "Файл успешно расшифрован (${result.method})", Toast.LENGTH_LONG).show()
                        } else {
                            binding.outputData.text = "Не удалось расшифровать файл\n\nПопробованные методы:\n${result.errorMessages}"
                            Toast.makeText(requireContext(), "Все методы расшифровки failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Decryption", "Ошибка при чтении файла", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private data class DecryptionResult(
        val success: Boolean,
        val data: String = "",
        val method: String = "",
        val errorMessages: String = ""
    )

    private fun tryAllDecryptionMethods(encryptedData: ByteArray, password: String): DecryptionResult {
        val errorMessages = StringBuilder()

        // Method 1: OpenSSL EVP_BytesToKey with MD5 (traditional)
        try {
            val decrypted = decryptOpenSSLEVPBytesToKey(encryptedData, password, "MD5")
            return DecryptionResult(true, String(decrypted), "OpenSSL EVP_BytesToKey (MD5)")
        } catch (e: Exception) {
            errorMessages.append("• EVP_BytesToKey (MD5): ${e.message}\n")
            Log.d("Decryption", "Method 1 failed: ${e.message}")
        }

        // Method 2: OpenSSL EVP_BytesToKey with SHA256
        try {
            val decrypted = decryptOpenSSLEVPBytesToKey(encryptedData, password, "SHA-256")
            return DecryptionResult(true, String(decrypted), "OpenSSL EVP_BytesToKey (SHA256)")
        } catch (e: Exception) {
            errorMessages.append("• EVP_BytesToKey (SHA256): ${e.message}\n")
            Log.d("Decryption", "Method 2 failed: ${e.message}")
        }

        // Method 3: PBKDF2 with 10000 iterations
        try {
            val decrypted = decryptPBKDF2(encryptedData, password, 100000)
            return DecryptionResult(true, String(decrypted), "PBKDF2 (10000 iterations)")
        } catch (e: Exception) {
            errorMessages.append("• PBKDF2 (100000): ${e.message}\n")
            Log.d("Decryption", "Method 3 failed: ${e.message}")
        }

        // Method 4: PBKDF2 with 1 iteration (old format)
        try {
            val decrypted = decryptPBKDF2(encryptedData, password, 1)
            return DecryptionResult(true, String(decrypted), "PBKDF2 (1 iteration)")
        } catch (e: Exception) {
            errorMessages.append("• PBKDF2 (1): ${e.message}\n")
            Log.d("Decryption", "Method 4 failed: ${e.message}")
        }

        // Method 5: Without salt header
        try {
            val decrypted = decryptWithoutSalt(encryptedData, password)
            return DecryptionResult(true, String(decrypted), "No salt header")
        } catch (e: Exception) {
            errorMessages.append("• No salt: ${e.message}\n")
            Log.d("Decryption", "Method 5 failed: ${e.message}")
        }

        return DecryptionResult(false, errorMessages = errorMessages.toString())
    }

    // OpenSSL EVP_BytesToKey decryption
    private fun decryptOpenSSLEVPBytesToKey(
        encryptedData: ByteArray,
        password: String,
        digestAlgorithm: String
    ): ByteArray {
        if (encryptedData.size < 16) {
            throw IllegalArgumentException("File too short")
        }

        val hasSalt = String(encryptedData.copyOfRange(0, 8)) == "Salted__"

        val salt = if (hasSalt) encryptedData.copyOfRange(8, 16) else ByteArray(0)
        val actualEncryptedData = if (hasSalt) encryptedData.copyOfRange(16, encryptedData.size) else encryptedData

        // Derive key and IV using EVP_BytesToKey
        val (key, iv) = evpBytesToKey(
            password.toByteArray(StandardCharsets.UTF_8),
            salt,
            32, // key length
            16,  // iv length
            digestAlgorithm
        )

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

        return cipher.doFinal(actualEncryptedData)
    }

    // Generic EVP_BytesToKey implementation
    private fun evpBytesToKey(
        password: ByteArray,
        salt: ByteArray,
        keyLen: Int,
        ivLen: Int,
        digestAlgorithm: String
    ): Pair<ByteArray, ByteArray> {
        val totalLength = keyLen + ivLen
        val result = ByteArray(totalLength)
        var currentHash = ByteArray(0)

        val md = MessageDigest.getInstance(digestAlgorithm)
        var offset = 0

        while (offset < totalLength) {
            md.reset()

            // Add previous hash if it exists
            if (currentHash.isNotEmpty()) {
                md.update(currentHash)
            }

            // Add password
            md.update(password)

            // Add salt if present
            if (salt.isNotEmpty()) {
                md.update(salt)
            }

            currentHash = md.digest()

            // Copy as much as we need
            val toCopy = minOf(currentHash.size, totalLength - offset)
            System.arraycopy(currentHash, 0, result, offset, toCopy)
            offset += toCopy
        }

        val key = result.copyOfRange(0, keyLen)
        val iv = result.copyOfRange(keyLen, keyLen + ivLen)

        return Pair(key, iv)
    }

    // PBKDF2 decryption
    private fun decryptPBKDF2(
        encryptedData: ByteArray,
        password: String,
        iterations: Int
    ): ByteArray {
        if (encryptedData.size < 16) {
            throw IllegalArgumentException("File too short")
        }

        val hasSalt = String(encryptedData.copyOfRange(0, 8)) == "Salted__"
        if (!hasSalt) {
            throw IllegalArgumentException("PBKDF2 requires salt")
        }

        val salt = encryptedData.copyOfRange(8, 16)
        val actualEncryptedData = encryptedData.copyOfRange(16, encryptedData.size)

        // Use PBKDF2 to derive key
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256 + 128) // key + iv
        val derivedKey = factory.generateSecret(spec).encoded

        val key = derivedKey.copyOfRange(0, 32)
        val iv = derivedKey.copyOfRange(32, 48)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

        return cipher.doFinal(actualEncryptedData)
    }

    // Decryption without salt header
    private fun decryptWithoutSalt(encryptedData: ByteArray, password: String): ByteArray {
        // Try with empty salt
        val (key, iv) = evpBytesToKey(
            password.toByteArray(StandardCharsets.UTF_8),
            ByteArray(0),
            32,
            16,
            "MD5"
        )

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

        return cipher.doFinal(encryptedData)
    }

    // Save decrypted file and share
    private fun decryptAndSaveFile(fileUri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                requireContext().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    val encryptedData = inputStream.readBytes()

                    val result = tryAllDecryptionMethods(encryptedData, password)

                    if (result.success) {
                        val fileName = getFileNameFromUri(fileUri) ?: "decrypted_file"
                        val outputFileName = "${fileName.removeSuffix(".enc")}.txt"
                        val outputFile = File(requireContext().filesDir, outputFileName)

                        FileOutputStream(outputFile).use { outputStream ->
                            outputStream.write(result.data.toByteArray(StandardCharsets.UTF_8))
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Файл расшифрован: ${outputFile.name}", Toast.LENGTH_LONG).show()
                            shareFile(outputFile)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Не удалось расшифровать файл", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Decryption", "Ошибка при сохранении файла", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
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
            "file" -> uri.lastPathSegment
            else -> null
        }
    }

    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Поделиться расшифрованным файлом"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при открытии файла: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val downloadCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Обработка завершения загрузки
        }
    }
}