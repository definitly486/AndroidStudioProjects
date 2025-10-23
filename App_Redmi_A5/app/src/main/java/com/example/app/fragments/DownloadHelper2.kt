package com.example.app.fragments

import android.content.Context

import android.widget.Toast

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader


class DownloadHelper2(private val context: Context) {


    fun decompressTarGz(tarGzFile: File, outputDir: File) {

        // Ensure canonical path for security
        outputDir.canonicalFile

        if (!tarGzFile.exists()) throw FileNotFoundException("File not found: ${tarGzFile.path}") as Throwable
        GzipCompressorInputStream(BufferedInputStream(FileInputStream(tarGzFile))).use { gzIn ->
            TarArchiveInputStream(gzIn).use { tarIn ->
                generateSequence { tarIn.nextEntry }.forEach { entry ->

                    val outputFile = File(outputDir, entry.name).canonicalFile

                    // Check if the extracted file stays inside outputDir
                    // Prevent Zip Slip Vulnerability
                    // if (!outputFile.toPath().startsWith(canonicalOutputDir.toPath())) {
                    //     throw SecurityException("Zip Slip vulnerability detected! Malicious entry: ${entry.name}")
                    //  }

                    if (entry.isDirectory) outputFile.mkdirs()
                    else {
                        outputFile.parentFile.mkdirs()
                        outputFile.outputStream().use { outStream ->
                            tarIn.copyTo(outStream)
                        }
                    }
                }
            }
        }
    }


    fun unpackTarXz(tarXzFile: File, outputDirectory: File) {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        FileInputStream(tarXzFile).use { fis ->
            BufferedInputStream(fis).use { bis ->
                XZCompressorInputStream(bis).use { xzIn ->
                    TarArchiveInputStream(xzIn).use { tarIn ->
                        var entry = tarIn.nextEntry
                        while (entry != null) {
                            val outputFile = File(outputDirectory, entry.name)
                            if (entry.isDirectory) {
                                outputFile.mkdirs()
                            } else {
                                FileOutputStream(outputFile).use { fos ->
                                    tarIn.copyTo(fos)
                                }
                            }
                            entry = tarIn.nextEntry
                        }
                    }
                }
            }
        }

    }


    fun copymain() {
        Toast.makeText(context, "Копируем redmia5-main ...", Toast.LENGTH_SHORT).show()


        val prepareCommands =
            arrayOf("su - root -c chmod -R 0755 /storage/emulated/0/Android/data/com.example.app/files/Download/redmia5-main")
        for (command in prepareCommands) {
            Runtime.getRuntime().exec(command).waitFor()
        }

        val ownerCmd =
            "su - root -c   ls -l   /data_mirror/data_ce/null/0/com.termos | awk '{print $3}' | head -n 2"
        val fileOwner = execShell(ownerCmd)?.trim() ?: ""

        val commands = arrayOf(

            "su - root -c cp  -R /storage/emulated/0/Android/data/com.example.app/files/Download/redmia5-main /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chmod -R 0755 /data_mirror/data_ce/null/0/com.termos/files/home",
            "su - root -c chown -R  $fileOwner:$fileOwner /data_mirror/data_ce/null/0/com.termos/files/home/redmia5-main"
        )

        var process: Process? = null

        for (command in commands) {
            process = Runtime.getRuntime().exec(command)
            process.waitFor() // Wait for the command to finish
            if (process.exitValue() != 0) {
                Toast.makeText(context, "Ошибка при копирование main: $command", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }




        Toast.makeText(context, "Копирование  main завершенo", Toast.LENGTH_SHORT).show()
    }

    // Вспомогательная функция для выполнения shell-команд
    private fun execShell(cmd: String): String? {
        try {
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            if (process.exitValue() != 0) {
                throw Exception("Ошибка при выполнении команды: $cmd")
            }

            val outputStream = BufferedReader(InputStreamReader(process.inputStream))
            val resultBuilder = StringBuilder()
            while (true) {
                val line = outputStream.readLine() ?: break
                resultBuilder.append(line).append("\n")
            }
            return resultBuilder.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}