package com.example.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.app.KernelSetupScript
import com.example.app.R

class KernelSuFragment : Fragment() {

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

        val view = inflater.inflate(R.layout.fragment_kernelsu, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        val installApatchKsu = view.findViewById<Button>(R.id.install_apatch_ksu_zip)

        installApatchKsu.setOnClickListener {
            kernelScript.startInstall()
        }
    }
}