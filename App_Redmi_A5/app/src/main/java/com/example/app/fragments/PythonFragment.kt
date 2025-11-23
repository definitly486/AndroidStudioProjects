package com.example.app.fragments

import DownloadHelper
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

class PythonFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2

    fun getDownloadFolder(): File? {
        return context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_python, container, false)
        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        //Кнопка установки python3
        val installPython = view.findViewById<Button>(R.id.installpython3)
        installPython.setOnClickListener { installPYTHON3() }

        val installEnvPython = view.findViewById<Button>(R.id.installenvpython3)
        installEnvPython.setOnClickListener { installENVPYTHON3() }


    }

    private fun  installENVPYTHON3() {
        downloadHelper2.installenvpython3()
    }
    private fun installPYTHON3() {
        val folder = getDownloadFolder() ?: return
        val tarGzFile = File(folder, "python-3.13-android-aarch64.tar.gz")
        val outputDir = File(folder, "")
        if (!tarGzFile.exists()) {
            Toast.makeText(requireContext(), "Файл python-3.13-android-aarch64.tar.gz не существует", Toast.LENGTH_SHORT).show()
            downloadHelper.downloadfile("https://github.com/definitly486/redmia5/releases/download/python3/python-3.13-android-aarch64.tar.gz")
            return
        }
        downloadHelper2 = DownloadHelper2(requireContext())
        downloadHelper2.decompressTarGz(tarGzFile, outputDir)
        Thread.sleep(3000L)
        downloadHelper2.copypython3()
    }



}