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
import com.example.app.savePackagesToFile
import java.io.File

class FirstFragment : Fragment() {

    private lateinit var downloadHelper: DownloadHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)


        // Проверка root-доступа устройства
        if (RootChecker.hasRootAccess(requireContext())) {
            Toast.makeText(requireContext(), "Устройство имеет root-доступ.", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Root-доступ отсутствует.", Toast.LENGTH_SHORT).show()
        }

        // Проверка возможности записи в папку '/system'
        val pathToCheck = "/system"
        if (RootChecker.checkWriteAccess(pathToCheck)) {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' возможна!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Запись в '$pathToCheck' невозможна.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Инициализация помощника для скачивания
        downloadHelper = DownloadHelper(requireContext())

        // Карта кнопок и соответствующих URL для скачивания APK
        val installButtons = mapOf(
            R.id.installfm to "https://github.com/definitly486/redmia5/releases/download/apk/FM+v3.6.3.apk",
            R.id.installtermos to "https://github.com/definitly486/redmia5/releases/download/apk/Termos_v2.4_universal.apk",
            R.id.installaurora to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/com.aurora.store_70.apk",
            R.id.installnewpipe to "https://github.com/definitly486/redmia5/releases/download/apk/NewPipe_nightly-1068.apk",
            R.id.installjob to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/Ozon+Job_1.62.0-GMS-release_apkcombo.com_antisplit.apk",
            R.id.installtc to "https://github.com/definitly486/redmia5/releases/download/apk/Total_Commander_v.3.50d.apk",
            R.id.installsberbank to "https://github.com/definitly486/redmia5/releases/download/apk/SberbankOnline.apk",
            R.id.installozonbank to "https://github.com/definitly486/redmia5/releases/download/apk/Ozon_Bank_18.35.0.apk",
            R.id.installtelegram to "https://github.com/definitly486/redmia5/releases/download/apk/Telegram+11.14.1.apk",
            R.id.installgnucash to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org-gnucash-android-24003-39426726-deeea690953a751a05a1a35017540c33.apk",
            R.id.installkeychain to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/org.sufficientlysecure.keychain_60200.apk",
            R.id.installdpi to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/ByeByeDPI-arm64-v8a-release.apk",
            R.id.installsports to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/sports+2024_1.2_apkcombo.com_antisplit.apk",
            R.id.installgesture to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Gesture.apk",
            R.id.installhaker to "https://github.com/definitly486/Lenovo_TB-X304L/releases/download/apk/Hacker_v1.41.1.apk",
            R.id.installzepp to "https://github.com/definitly486/redmia5/releases/download/apk/zepplife_6.14.0_repack.apk",
            R.id.installmpv to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/is.xyz.mpv_41.apk",
            R.id.installray to "https://github.com/definitly486/redmia5/releases/download/apk/v2rayNG_1.10.24_arm64-v8a.apk",
            R.id.installtermosplus to "https://github.com/definitly486/redmia5/releases/download/apk/com.termoneplus_3.6.0.apk",
            R.id.installapatch to "https://github.com/definitly486/redmia5/releases/download/apk/APatch_11107_11107-release-signed.apk",
            R.id.installkernelsu to "https://github.com/definitly486/redmia5/releases/download/apk/KernelSU_v1.0.5_12081-release.apk",
            R.id.installcore to "https://github.com/definitly486/redmia5/releases/download/apk/Core+Music+Player_1.0.apk",
            R.id.installpluma to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Pluma_.private_fast.browser_1.80_APKPure.apk",
            R.id.installtelegramx to "https://github.com/definitly486/Lenovo_Tab_3_7_TB3-730X/releases/download/apk/Telegram+X+0.27.5.1747-arm64-v8a.apk"
        )

        // Назначаем обработчик события каждому элементу карты
        installButtons.forEach { (buttonId, url) ->
            view.findViewById<Button>(buttonId)?.apply {
                setOnClickListener { _: View -> // явное указание типа View
                    downloadHelper.download(url) { file ->
                        if (file != null) {
                            Toast.makeText(
                                requireContext(),
                                "Файл загружен: ${file.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
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