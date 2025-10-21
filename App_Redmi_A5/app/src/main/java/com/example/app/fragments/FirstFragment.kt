package com.example.app.fragments

import DownloadHelper


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import androidx.fragment.app.Fragment
import com.example.app.R


class FirstFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        // Инициализация
        downloadHelper = DownloadHelper(requireContext())

        val installButton = view.findViewById<Button>(R.id.installfm)
        installButton.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/FM+v3.6.3.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installtermos = view.findViewById<Button>(R.id.installtermos)
        installtermos.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Termos_v2.4_universal.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installaurora = view.findViewById<Button>(R.id.installaurora)
        installaurora.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/com.aurora.store_70.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installnewpipe = view.findViewById<Button>(R.id.installnewpipe)
        installnewpipe.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/NewPipe_nightly-1068.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installjob = view.findViewById<Button>(R.id.installjob)
        installjob.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/Ozon+Job_1.62.0-GMS-release_apkcombo.com_antisplit.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val installtc = view.findViewById<Button>(R.id.installtc)
        installtc.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val installsberbank = view.findViewById<Button>(R.id.installsberbank)
        installsberbank.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/SberbankOnline.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val installozonbank = view.findViewById<Button>(R.id.installozonbank)
        installozonbank.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Ozon_Bank_18.35.0.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installtelegram = view.findViewById<Button>(R.id.installtelegram)
        installtelegram.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/redmia5/releases/download/apk/Telegram+11.14.1.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val installgnucash = view.findViewById<Button>(R.id.installgnucash)
        installgnucash.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org-gnucash-android-24003-39426726-deeea690953a751a05a1a35017540c33.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installkeychain = view.findViewById<Button>(R.id.installkeychain)
        installkeychain.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org.sufficientlysecure.keychain_60200.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installdpi = view.findViewById<Button>(R.id.installdpi)
        installdpi.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installsports = view.findViewById<Button>(R.id.installsports)
        installsports.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/sports+2024_1.2_apkcombo.com_antisplit.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val installgesture = view.findViewById<Button>(R.id.installgesture)
        installgesture.setOnClickListener {
            val apkUrl1 = "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Gesture.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(requireContext(), "Файл загружен: ${file.name}", Toast.LENGTH_SHORT).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }


        val installhaker = view.findViewById<Button>(R.id.installhaker)
        installhaker.setOnClickListener {
            val apkUrl1 =
                "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/Hacker_v1.41.1.apk"
            downloadHelper.download(apkUrl1) { file ->
                if (file != null) {
                    Toast.makeText(
                        requireContext(),
                        "Файл загружен: ${file.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Установка происходит автоматически после завершения
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }

        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadHelper.cleanup()
    }
}