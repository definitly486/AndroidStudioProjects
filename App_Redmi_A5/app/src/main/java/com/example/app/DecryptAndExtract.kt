package com.example.app

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.ProcessBuilder
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Paths

fun decryptAndExtractArchive(password: String) {
    val encryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/com.qflair.browserq.tar.enc"
    val decryptedFilePath = "/storage/emulated/0/Android/data/com.example.app/files/com.qflair.browserq.tar"
    val appDirectoryPath = "/data/data/com.qflair.browserq"

    // Проверяем наличие зашифрованного файла и скачиваем его, если отсутствует
    if (!File(encryptedFilePath).exists()) {
        println("Загрузка зашифрованного архива...")
        try {
            ProcessBuilder(
                "curl",
                "-k",
                "-L",
                "-o",
                encryptedFilePath,
                "https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc"
            ).start().waitFor()

            if (!File(encryptedFilePath).exists())
                throw RuntimeException("Ошибка загрузки файла.")

            println("Архив успешно загружен!")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    // Декодирование зашифрованного архива с использованием OpenSSL
    println("Декодирование архива...")
    try {
        ProcessBuilder(
            "openssl",
            "enc",
            "-aes-256-cbc",
            "-pbkdf2",
            "-iter",
            "100000",
            "-d",
            "-in",
            encryptedFilePath,
            "-out",
            decryptedFilePath,
            "-pass",
            "pass:$password"
        ).start().waitFor()

        if (!File(decryptedFilePath).exists())
            throw RuntimeException("Ошибка расшифровки файла.")

        println("Файл успешно расшифрован!")
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }

    // Удаление старого каталога приложения и извлечение нового содержимого
    println("Извлечение файлов...")
    try {
        Files.deleteIfExists(Paths.get(appDirectoryPath))
        ProcessBuilder(
            "busybox",
            "tar",
            "xf",
            decryptedFilePath
        ).start().waitFor()

        println("Каталог извлечён!")
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }

    // Получаем владельца директории и устанавливаем права доступа
    val userId = getUserId()
    if (userId.isNullOrBlank()) {
        throw RuntimeException("Не удалось определить ID пользователя.")
    }

    println("Установка прав доступа...")
    try {
        ProcessBuilder(
            "chown",
            "-R",
            "$userId:$userId",
            appDirectoryPath
        ).start().waitFor()

        println("Права доступа установлены!")
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }
}

// Вспомогательная функция для получения ID пользователя
private fun getUserId(): String? {
    try {
        // Выполняем команду ls -l и извлекаем нужную информацию
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "ls -l /data/data/ | grep qflair | head -n 1 | awk '{print \$3}'"))

        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            return reader.readLine()?.trim()
        }
    } catch (e: Exception) {
        println("Ошибка при определении ID пользователя: ${e.message}")
        return null
    }
}