package com.example.app.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.BufferedInputStream
import java.io.FileOutputStream


class ThirdFragment : Fragment() {

    var apkHttpUrl = "https://github.com/xinitronix/gnucash/raw/refs/heads/main/"
    var maitargzHttpUrl = "https://github.com/definitly486/Lenovo_TB-X304L/archive/"
    var GHHttpUrl = "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/gh_2.76.2_aarch64/"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_third, container, false)
        val button = view.findViewById<Button>(R.id.button5)
        button.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${apkHttpUrl}definitly.gnucash.gpg")
        }


        val button6 = view.findViewById<Button>(R.id.button6)
        button6.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${maitargzHttpUrl}main.tar.gz")
        }

        val button7 = view.findViewById<Button>(R.id.button7)
        button7.setOnClickListener {
            val folder = getDownloadFolder() ?: return@setOnClickListener
            val tarGzFile = File(folder,"main.tar.gz")
            val outputDir = File(folder,"")
            if (!tarGzFile.exists()) {
                Toast.makeText(requireContext(), "Файл main.tar.gz  не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            decompressTarGz (tarGzFile, outputDir)
        }

        val button8 = view.findViewById<Button>(R.id.button8)
        button8.setOnClickListener {
            val helper = DownloadHelper(requireContext())
            helper.download("${GHHttpUrl}gh_2.76.2_aarch64.tar.xz")
        }

        val button9 = view.findViewById<Button>(R.id.button9)
        button9.setOnClickListener {
            val folder = getDownloadFolder() ?: return@setOnClickListener
            val tarXzFilezFile = File(folder,"gh_2.76.2_aarch64.tar.xz")
            val outputDir = File(folder,"")
            if (!tarXzFilezFile.exists()) {
                Toast.makeText(requireContext(), "Файл gh_2.76.2_aarch64.tar.xz  не существует", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            unpackTarXz(tarXzFilezFile, outputDir)

        }

        val button10 = view.findViewById<Button>(R.id.button10)
        button10.setOnClickListener {
           installGH()
        }



        return view
    }

    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }


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

    fun installGH(){
        Toast.makeText(context, "Начинается установка GH...", Toast.LENGTH_SHORT).show()
        Runtime.getRuntime().exec("su - root -c mount -o rw,remount /")
        Runtime.getRuntime().exec("su - root -c cp /storage/emulated/0/Android/data/com.example.app/files/Download/gh_2.76.2_aarch64/gh /system/bin/")
        Runtime.getRuntime().exec("su - root -c chmod +x  /system/bin/gh")
        Runtime.getRuntime().exec("su - root -c chmod 0755  /system/bin/gh")

    }


}
