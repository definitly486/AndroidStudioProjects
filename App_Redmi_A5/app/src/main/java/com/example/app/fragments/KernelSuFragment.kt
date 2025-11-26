package com.example.app.fragments

import DownloadHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.app.KernelSetupScript
import com.example.app.R

class KernelSuFragment : Fragment() {


    private lateinit var downloadHelper: DownloadHelper
    private lateinit var downloadHelper2: DownloadHelper2

    private lateinit var kernelScript: KernelSetupScript

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kernelScript = KernelSetupScript(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Инициализация DownloadHelper
        downloadHelper = DownloadHelper(requireContext())
        downloadHelper2 = DownloadHelper2(requireContext())


        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        val installApatchKsu = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installApatchKsu.setOnClickListener {
            kernelScript.startInstall()
        }

        // Кнопка скачивания ksuzip
        val downloadksuzip = view.findViewById<Button>(R.id.downloadksuzip)
        downloadksuzip.setOnClickListener { downloadKSUZip() }


    }

    private fun downloadKSUZip() {
        downloadHelper.downloadToPublic("https://github.com/definitly486/redmia5/releases/download/root/APatch-KSU.zip")
    }


}
