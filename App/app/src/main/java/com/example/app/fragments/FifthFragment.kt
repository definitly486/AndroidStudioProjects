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
import android.os.Environment
import android.provider.OpenableColumns
import com.example.app.databinding.FragmentFourthBinding
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class FifthFragment : Fragment() {

    private lateinit var downloadManager: DownloadManager
    private var inputFileUri: Uri? = null
    private var _binding: FragmentFourthBinding? = null
    private val binding get() = _binding!!

    private val selectFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { updateSelectedFileInfo(it) }
    }

    private val saveFileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            saveDecryptedDataToUri(uri)
        } else {
            Toast.makeText(requireContext(), "Сохранение отменено", Toast.LENGTH_SHORT).show()
        }
    }

    private var decryptedBytes: ByteArray? = null

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

        binding.buttonSaveInternal.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(requireContext(), "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            decryptAndSaveFile(inputFileUri!!, password)
        }
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

    // Основная функция для расшифровки файла
    private fun decryptFileWithFallback(fileUri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                requireContext().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    val encryptedData = inputStream.readBytes()

                    val result = tryAllDecryptionMethods(encryptedData, password)

                    withContext(Dispatchers.Main) {
                        if (result.success && result.data != null) {
                            decryptedBytes = result.data
                            binding.outputData.text = "Файл успешно расшифрован"
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
        val data: ByteArray? = null,
        val method: String = "",
        val errorMessages: String = ""
    )

    private fun tryAllDecryptionMethods(encryptedData: ByteArray, password: String): DecryptionResult {
        val errorMessages = StringBuilder()

        // Метод 1
        try {
            val decrypted = decryptOpenSSLEVPBytesToKey(encryptedData, password, "MD5")
            return DecryptionResult(true, decrypted, "OpenSSL EVP_BytesToKey (MD5)")
        } catch (e: Exception) {
            errorMessages.append("• EVP_BytesToKey (MD5): ${e.message}\n")
        }

        // Метод 2
        try {
            val decrypted = decryptOpenSSLEVPBytesToKey(encryptedData, password, "SHA-256")
            return DecryptionResult(true, decrypted, "OpenSSL EVP_BytesToKey (SHA256)")
        } catch (e: Exception) {
            errorMessages.append("• EVP_BytesToKey (SHA256): ${e.message}\n")
        }

        // Метод 3
        try {
            val decrypted = decryptPBKDF2(encryptedData, password, 100000)
            return DecryptionResult(true, decrypted, "PBKDF2 (100000 итераций)")
        } catch (e: Exception) {
            errorMessages.append("• PBKDF2 (100000): ${e.message}\n")
        }

        // Метод 4
        try {
            val decrypted = decryptPBKDF2(encryptedData, password, 1)
            return DecryptionResult(true, decrypted, "PBKDF2 (1 итерация)")
        } catch (e: Exception) {
            errorMessages.append("• PBKDF2 (1): ${e.message}\n")
        }

        // Метод 5
        try {
            val decrypted = decryptWithoutSalt(encryptedData, password)
            return DecryptionResult(true, decrypted, "Без соли")
        } catch (e: Exception) {
            errorMessages.append("• Без соли: ${e.message}\n")
        }

        return DecryptionResult(false, null, errorMessages = errorMessages.toString())
    }

    // Реализация метода EVP_BytesToKey
    private fun decryptOpenSSLEVPBytesToKey(
        encryptedData: ByteArray,
        password: String,
        digestAlgorithm: String
    ): ByteArray {
        if (encryptedData.size < 16) throw IllegalArgumentException("Файл слишком короткий")
        val hasSalt = String(encryptedData.copyOfRange(0, 8)) == "Salted__"
        val salt = if (hasSalt) encryptedData.copyOfRange(8, 16) else ByteArray(0)
        val actualEncryptedData = if (hasSalt) encryptedData.copyOfRange(16, encryptedData.size) else encryptedData

        val (key, iv) = evpBytesToKey(
            password.toByteArray(StandardCharsets.UTF_8),
            salt,
            32,
            16,
            digestAlgorithm
        )

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

        return cipher.doFinal(actualEncryptedData)
    }

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
            if (currentHash.isNotEmpty()) md.update(currentHash)
            md.update(password)
            if (salt.isNotEmpty()) md.update(salt)
            currentHash = md.digest()

            val toCopy = minOf(currentHash.size, totalLength - offset)
            System.arraycopy(currentHash, 0, result, offset, toCopy)
            offset += toCopy
        }

        val key = result.copyOfRange(0, keyLen)
        val iv = result.copyOfRange(keyLen, totalLength)
        return Pair(key, iv)
    }

    private fun decryptPBKDF2(encryptedData: ByteArray, password: String, iterations: Int): ByteArray {
        if (encryptedData.size < 16) throw IllegalArgumentException("Файл слишком короткий")
        val hasSalt = String(encryptedData.copyOfRange(0, 8)) == "Salted__"
        if (!hasSalt) throw IllegalArgumentException("Требуется соль")
        val salt = encryptedData.copyOfRange(8, 16)
        val actualEncryptedData = encryptedData.copyOfRange(16, encryptedData.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256 + 128)
        val derivedKey = factory.generateSecret(spec).encoded

        val key = derivedKey.copyOfRange(0, 32)
        val iv = derivedKey.copyOfRange(32, 48)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

        return cipher.doFinal(actualEncryptedData)
    }

    private fun decryptWithoutSalt(encryptedData: ByteArray, password: String): ByteArray {
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

    // Сохранение расшифрованных данных
    private fun decryptAndSaveFile(fileUri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                requireContext().contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    val encryptedData = inputStream.readBytes()
                    val result = tryAllDecryptionMethods(encryptedData, password)
                    withContext(Dispatchers.Main) {
                        if (result.success && result.data != null) {
                            decryptedBytes = result.data
                            val fileName = getFileNameFromUri(fileUri) ?: "decrypted_file"
                            val outputFileName = "${fileName.removeSuffix(".enc")}"
                            saveFileLauncher.launch(outputFileName)
                        } else {
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

    // Сохраняем байты в выбранный URI
    private fun saveDecryptedDataToUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                decryptedBytes?.let { data ->
                    requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(data)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Файл успешно сохранен", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Нет данных для сохранения", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SaveFile", "Ошибка сохранения файла", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri?): String? {
        if (uri == null) return null
        return when (uri.scheme) {
            "content" -> {
                requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) cursor.getString(index) else null
                    } else null
                }
            }
            "file" -> uri.lastPathSegment
            else -> null
        }
    }
}