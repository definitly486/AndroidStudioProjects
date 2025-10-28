package com.example.app

import java.io.File
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Paths

fun decryptAndExtractArchive(password: String) {
    val encryptedFilePath = "com.qflair.browserq.tar.enc"
    val decryptedFilePath = "com.qflair.browserq.tar"
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

    // Декодирование зашифрованного архива с использованием openssl
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
    println("Получение прав собственности...")
    try {
        val idProcess = ProcessBuilder(
            "ls",
            "-l",
            "/data/data/",
            "|",
            "grep",
            "qflair",
            "|",
            "head",
            "-n",
            "1",
            "|",
            "awk",
            "'{print \\$3}'"
        )
        val process = idProcess.start()
        val outputStream = process.inputStream.bufferedReader()
        val userId = outputStream.readLine() ?: ""

        if (userId.isEmpty()) {
            throw RuntimeException("Не удалось определить ID пользователя.")
        }

        println("Установка прав доступа...")
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