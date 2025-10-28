@file:Suppress("DEPRECATION")

package com.example.app.fragments

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.decryptAndExtractArchive
import com.example.app.downloadplumaprofile

class FifthFragment : Fragment() {

    private lateinit var installopensslButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        // Ищем кнопку и добавляем ей слушатель
        installopensslButton = view.findViewById(R.id.installplumaprofile)
        installopensslButton.setOnClickListener {
            DecryptionTask().execute()
        }

        return view
    }

    // Асинхронная задача
    inner class DecryptionTask : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void): Boolean {
            val password = "639639"
            downloadplumaprofile(requireContext(),"https://github.com/definitly486/redmia5/releases/download/shared/com.qflair.browserq.tar.enc") // Возможно тоже убрать suspend!
            decryptAndExtractArchive(requireContext(),password)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            // Обновление UI или вывод сообщений пользователю
        }
    }
}