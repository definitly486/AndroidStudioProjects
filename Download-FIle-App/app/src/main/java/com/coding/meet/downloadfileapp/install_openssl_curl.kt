package com.coding.meet.downloadfileapp

import android.R
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

fun unpacktarxz(filetarxz: String) {
    val tarXzFile = File("/storage/emulated/0/Download/"+filetarxz)
    val outputDir = File("/storage/emulated/0/Download/")
    unpackTarXz(tarXzFile, outputDir)

}