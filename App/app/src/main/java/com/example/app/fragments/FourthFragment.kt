package com.example.app.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import org.bouncycastle.util.encoders.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.Security
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.annotation.SuppressLint
import com.example.app.databinding.FragmentFourthBinding

class FourthFragment : Fragment() {

    private lateinit var downloadManager: DownloadManager
    private var myDownloadID: Long = 0
    private val apkHttpUrl = "https://github.com/definitly486/definitly486/releases/download/shared/"

    private var inputFileUri: Uri? = null

    // Объявляем binding (если используете ViewBinding)
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

        // Обработчик выбора файла
        binding.buttonSelectFile.setOnClickListener {
            if (isStoragePermissionGranted()) {
                selectFileLauncher.launch(arrayOf("*/*"))
            } else {
                requestStoragePermission()
            }
        }

        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Обработчик расшифровки
        binding.buttonDecrypt.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(requireContext(), "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            decryptFile(inputFileUri!!, password)
        }

        binding.button.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(requireContext(), "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            decryptFileopenssl(inputFileUri!!, password)
        }


    }

    override fun onResume() {
        super.onResume()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requireContext().registerReceiver(
                downloadCompleteBroadcastReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                downloadCompleteBroadcastReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(downloadCompleteBroadcastReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Обновление информации о выбранном файле
    @SuppressLint("SetTextI18n")
    private fun updateSelectedFileInfo(uri: Uri) {
        inputFileUri = uri
        binding.fileNameLabel.text = "Выбран файл: $uri"
    }

    // Проверка разрешений
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

    // Расшифровка файла
    @SuppressLint("SetTextI18n")
    private fun decryptFile(fileUri: Uri, password: String) {
        Thread {
            try {
                val stream = requireContext().contentResolver.openInputStream(fileUri)!!
                val rawBase64Data = InputStreamReader(stream).readText()

                val cleanedBase64 = rawBase64Data.trim().replace("[^A-Za-z0-9+/=]+".toRegex(), "")
                val paddedBase64 = cleanedBase64.padEnd((cleanedBase64.length + 3) / 4 * 4, '=')

                val decodedData = android.util.Base64.decode(paddedBase64, android.util.Base64.DEFAULT)

                val decryptedData = decryptWithOpenSSLAES(decodedData, password)

                requireActivity().runOnUiThread {
                    binding.outputData.text = "Результат:\n$decryptedData"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка при расшифровке: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun decryptWithOpenSSLAES(cipheredData: ByteArray, password: String): String {
        val keySalt = byteArrayOf(0x73, 0x6F, 0x6D, 0x65, 0x53, 0x61, 0x6C, 0x74)
        val keyIter = 100000
        val keyLen = 256

        val key = derivePBKDF2Key(password.toCharArray(), keySalt, keyIter, keyLen)
        val iv = ByteArray(16)
        System.arraycopy(cipheredData, 0, iv, 0, iv.size.coerceAtMost(cipheredData.size))

        val cipherData = ByteArrayInputStream(cipheredData)
        val plainData = decryptAES256CBC(key, iv, cipherData.readBytes())

        return String(plainData)
    }

    private fun derivePBKDF2Key(password: CharArray, salt: ByteArray, iterations: Int, length: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, length)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "BC")
        return factory.generateSecret(spec).encoded
    }

    private fun decryptAES256CBC(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)
        return cipher.doFinal(ciphertext)
    }

    fun decryptFileopenssl(fileUri: Uri, password: String) {
        val filePath = cleanFilePath(fileUri)
        val fullPath = "$filePath"

        if (!File(fullPath).exists()) {
            Log.e("Decryption", "Файл не найден: $fullPath")
            return
        }

        val outputFile = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/new.txt"

        val command = listOf(
            "openssl",
            "enc",
            "-d",
            "-iter",
            "100000",
            "-pbkdf2",
            "-aes-256-cbc",
            "-in", fullPath,
            "-out", outputFile,
            "-pass", "pass:$password"
        )

        val result = runCommand(command)
        println(result)

        if (result.contains("error", ignoreCase = true)) {
            println("Ошибка при расшифровке!")
        } else {
            println("Файл успешно расшифрован!")
        }
    }

    private fun runCommand(command: List<String>): String {
        return try {
            ProcessBuilder().command(command)
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            "Ошибка выполнения команды"
        }
    }

    fun cleanFilePath(uri: Uri): String {
        var path = uri.path.orEmpty()
        while (path.startsWith("/") || path.startsWith("root")) {
            path = path.removePrefix("/")
                .removePrefix("root/")
        }
        return path
    }





    // Объявление BroadcastReceiver (если нужно)
    private val downloadCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Обработка завершения загрузки
        }
    }
}