package com.example.decryptopenssl

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.decryptopenssl.databinding.ActivityMainBinding
import org.bouncycastle.crypto.params.Blake3Parameters.context
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


@Suppress("SameParameterValue")
class MainActivity : AppCompatActivity() {

    lateinit var downloadManager: DownloadManager
    var mydownloadID: Long = 0
    var apkHttpUrl = "https://github.com/definitly486/definitly486/releases/download/shared/"

    private lateinit var binding: ActivityMainBinding
    private var inputFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Security.addProvider(BouncyCastleProvider())

        // Обработчик выбора файла
        val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { updateSelectedFileInfo(it) }
        }




        val folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        if (!folder.exists()) {
            folder.mkdirs()
        }


        binding.buttonSelectFile.setOnClickListener {
            if (isStoragePermissionGranted()) {
                selectFileLauncher.launch(arrayOf("*/*"))
            } else {
                requestStoragePermission()
            }
        }
 downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // Обработчик расшифровки
        binding.buttonDecrypt.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(this, "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            decryptFile(inputFileUri!!, password)
        }

        binding.button.setOnClickListener {
            val password = binding.passwordInput.text.toString()
            if (password.isEmpty() || inputFileUri == null) {
                Toast.makeText(this, "Заполните пароль и выберите файл", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            decryptFileopenssl(inputFileUri!!, password)
        }

        binding.download.setOnClickListener {

            download("${apkHttpUrl}new.enc")
        }


    }

    // Функция обновления информации о выбранном файле
    @SuppressLint("SetTextI18n")
    private fun updateSelectedFileInfo(uri: Uri) {
        inputFileUri = uri
        binding.fileNameLabel.text = "Выбран файл: $uri"
    }

    // Проверка разрешений на доступ к хранилищу
    private fun isStoragePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // Запрашиваем разрешение на доступ к хранилищу
    private fun requestStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    // Обрабатываем результат запрашиваемых разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            binding.buttonSelectFile.callOnClick()
        } else {
            Toast.makeText(this, "Доступ запрещен", Toast.LENGTH_LONG).show()
        }
    }

    // Основной метод расшифровки файла
    @SuppressLint("SetTextI18n")
    private fun decryptFile(fileUri: Uri, password: String) {
        Thread {
            try {
                // Чтение содержимого файла
                val stream = contentResolver.openInputStream(fileUri)!!
                val rawBase64Data = InputStreamReader(stream).readText()

                // Очистка строки от посторонних символов
                val cleanedBase64 = rawBase64Data.trim().replace("[^A-Za-z0-9+/=]+".toRegex(), "")

                // Дополнение длины до кратности 4
                val paddedBase64 = cleanedBase64.padEnd((cleanedBase64.length + 3) / 4 * 4, '=')

                // Попытка декодирования
                val decodedData = Base64.decode(paddedBase64)

                // Процесс расшифровки
                val decryptedData = decryptWithOpenSSLAES(decodedData, password)

                runOnUiThread {
                    binding.outputData.text = "Результат:\n$decryptedData"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Ошибка при расшифровке: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    // Основная логика расшифровки AES CBC
    private fun decryptWithOpenSSLAES(cipheredData: ByteArray, password: String): String {
        val keySalt = byteArrayOf(0x73, 0x6F, 0x6D, 0x65, 0x53, 0x61, 0x6C, 0x74) // Произвольный соль
        val keyIter = 100000
        val keyLen = 256

        // Генерация ключа и вектора инициализации
        val key = derivePBKDF2Key(password.toCharArray(), keySalt, keyIter, keyLen)
        val iv = ByteArray(16) // Первый блок исходных данных используется как вектор инициализации
        System.arraycopy(cipheredData, 0, iv, 0, iv.size.coerceAtMost(cipheredData.size))

        // Распаковка данных и их расшифровка
        val cipherData = ByteArrayInputStream(cipheredData)
        val plainData = decryptAES256CBC(key, iv, cipherData.readBytes())

        return String(plainData)
    }

    // Функция для получения ключа на основе пароля (PBKDF2)
    private fun derivePBKDF2Key(password: CharArray, salt: ByteArray, iterations: Int, length: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, length)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "BC")
        return factory.generateSecret(spec).encoded
    }

    // Декодирование AES CBC
    @SuppressLint("DeprecatedProvider")
    private fun decryptAES256CBC(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)
        return cipher.doFinal(ciphertext)
    }

    fun decryptFileopenssl(fileUri: Uri, password: String) {

        // Чистый путь к файлу (без корня и префиксов)
        val filePath = cleanFilePath(fileUri)

        // Полный путь к файлу на внешнем накопителе
        val fullPath = "$filePath"

        // Проверяем существование файла
        if (!File(fullPath).exists()) {
            Log.e("Decryption", "Файл не найден: $fullPath")
            return
        }

        // Далее выполняйте
        val outputFile="/storage/emulated/0/Android/data/com.example.decryptopenssl/files/Download/new.txt"
      //  val outputFile= File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "decrypted_file.txt")
        val password =  password   // Ваш пароль
        // Команда для расшифровки файла с помощью OpenSSL
        val command = listOf(
            "openssl",
            "enc",
            "-d",
            "-iter",
            "100000",
            "-pbkdf2",
            "-aes-256-cbc",                     // Алгоритм шифрования
            "-in", filePath,
            "-out", outputFile,
            "-pass", "pass:$password"
        )

        // Выполняем команду
        val result = runCommand(command)
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

    // Очистка пути от ненужных символов
    fun cleanFilePath(uri: Uri): String {
        var path = uri.path.orEmpty()
        while (path.startsWith("/") || path.startsWith("root")) {
            path = path.removePrefix("/")
                .removePrefix("root/")
        }
        return path
    }

    fun download(url: String) {
        val folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val lastPart = url.split("/").last()
        val file = File(folder, lastPart)

        if (file.exists()) {
            Toast.makeText(this, "Файл уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Начинается загрузка...", Toast.LENGTH_SHORT)
                        .show()
                }

                val fileName = url.substringAfterLast('/')
                val request = DownloadManager.Request(Uri.parse(url))
                request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )
                request.setTitle(fileName)
                request.setDescription("Загружается...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.allowScanningByMediaScanner()
                request.setDestinationInExternalFilesDir(
                    this@MainActivity,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

                mydownloadID = downloadManager.enqueue(request)
            } catch (ex: Exception) {
                ex.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка при загрузке: ${ex.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Устанавливает APK-файлы
     */
    fun installApk(filename: String) {
        val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)

        if (apkFile.exists()) {
            val apkUri = FileProvider.getUriForFile(
                applicationContext,
                "$packageName.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Распаковывает ZIP-файл
     */
    fun unzip(filename: String): Boolean {
        val zipFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
        val outputFolder = File(getExternalFilesDir(null)?.path!!, "Download")
        outputFolder.mkdirs()
        // Создаем объект File для binance.base.apk
        val filePath = "/storage/emulated/0/Android/data/com.example.a304/files/Download/binance.base.apk"
        val file = File(filePath)

        if (file.exists()) {
            Toast.makeText(this, "Файл уже существует", Toast.LENGTH_SHORT).show()
            installApk("binance.base.apk")
            return true
        }

        val fis = FileInputStream(zipFile)
        val zis = ZipInputStream(fis)

        var entry: ZipEntry?
        while (zis.nextEntry.also { entry = it } != null) {
            val fileName = entry!!.name
            val destFile = File(outputFolder, fileName)
            destFile.parentFile.mkdirs()

            if (!entry.isDirectory) {
                val fos = FileOutputStream(destFile)
                val bufferSize = 4096
                val data = ByteArray(bufferSize)
                var count: Int
                while (zis.read(data, 0, bufferSize).also { count = it } != -1) {
                    fos.write(data, 0, count)
                }
                fos.flush()
                fos.close()
            }
            zis.closeEntry()
        }
        zis.close()
        fis.close()
        return true
    }

    // Приемник уведомлений о завершении загрузки
    private val downloadCompleteBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == mydownloadID) {
                Toast.makeText(applicationContext, "Загрузка завершена", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        registerReceiver(
            downloadCompleteBroadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onPause() {
        unregisterReceiver(downloadCompleteBroadcastReceiver)
        super.onPause()
    }








}