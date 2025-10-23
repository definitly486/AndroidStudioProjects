package com.example.app.fragments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import java.io.File

class GitClone {

    /**
     * Функция для клонирования репозитория Git
     *
     * @return Результат выполнения операции клонирования (true - успех, исключение - ошибка)
     */
    suspend fun cloneRepository(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val remoteUri = "https://github.com/definitly486/DCIM"
            val localDirectory = File("/storage/emulated/0/Android/data/com.example.app/files/Download/DCIM")

            // Создание директории, если она не существует
            if (!localDirectory.exists()) {
                localDirectory.mkdirs()
            }

            // Выполняем операцию клонирования
            Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(localDirectory)
                .call()

            Result.success(true) // Возвращаем успешный результат
        } catch (e: Exception) {
            println("Ошибка клонирования репозитория: ${e.message}")
            Result.failure(e) // Возвращаем результат с ошибкой
        }
    }
}