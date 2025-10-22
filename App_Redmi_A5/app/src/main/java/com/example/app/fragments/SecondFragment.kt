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



class SecondFragment : Fragment() {
    private lateinit var downloadHelper: DownloadHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        // Инициализация
        downloadHelper = DownloadHelper(requireContext())

        val installButton = view.findViewById<Button>(R.id.deletepkg)
        installButton.setOnClickListener {
      deletepkg()
        }
        return view
    }
    fun deletepkg() {
        Toast.makeText(context, "Начинается удаление пакетов ...", Toast.LENGTH_SHORT).show()

        val packagesToDelete = listOf(
            "com.miui.analytics.go",
            "ru.ivi.client",
            "ru.vk.store",
            "ru.beru.android",
            "ru.more.play",
            "ru.oneme.app",
            "com.miui.player",
            "com.miui.videoplayer",
            "com.miui.theme.lite",
            "com.miui.gameCenter.overlay",
            "com.miui.videoplayer.overlay",
            "com.miui.bugreport",
            "com.miui.cleaner.go",
            "com.miui.player.overlay",
            "com.miui.msa.global",
            "com.yandex.searchapp",
            "com.yandex.browser",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.android.shareMe.overlay",
            "com.vitastudio.mahjong",
            "com.oakever.tiletrip",
            "com.ordinaryjoy.woodblast",
            "com.go.browser",
            "com.facebook.appmanager",
            "com.google.android.apps.tachyon",
            "com.xiaomi.midrop",
            "com.miui.global.packageinstaller",
            "com.xiaomi.discover",
            "com.xiaomi.mipicks",
            "com.google.android.videos",
            "com.miui.android.fashiongallery",
            "com.google.android.apps.safetyhub",
            "com.google.android.overlay.gmsconfig.searchgo",
            "com.google.android.apps.searchlite",
            "com.google.android.appsearch.apk",
            "com.google.android.apps.docs",
            "com.xiaomi.glgm",
            "com.google.android.gm",
            "com.yandex.preinstallsatellite",
            "com.tencent.soter.soterserver " ,
            "com.android.bookmarkprovider",
            "com.xiaomi.mipicks",
            "com.xiaomi.discover",
            "com.facebook.services",
            "com.android.bips",
            "com.android.stk",
            "com.facebook.system",
            "com.google.android.feedback",
            "com.google.android.go.documentsui",
            "android.autoinstalls.config.Xiaomi.model",
            "com.google.android.apps.wellbeing",
            "com.android.vending",
            "com.android.musicfx",
            "com.google.android.tts",
            "com.mi.globalminusscreen",
            "com.android.printspooler",
            "com.google.android.printservice.recommendation",
            "com.google.android.setupwizard",
            "com.android.ons",
            "com.google.android.partnersetup",
            "com.android.providers.partnerbookmarks",
            "com.mi.android.globalFileexplorer.overlay",
            "com.android.backupconfirm",
            "android.overlay.multiuser",
            "com.android.calllogbackup",
            "com.android.cameraextensions",
            "com.google.android.marvin.talkback",
            "org.ifaa.aidl.manager",
            "com.android.wallpaperbackup",
            "com.android.avatarpicker",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.ext.shared",
            "com.android.sharedstoragebackup"
        )

        for (packageName in packagesToDelete) {
            try {
                Runtime.getRuntime().exec("su - root -c pm uninstall --user 0 $packageName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}