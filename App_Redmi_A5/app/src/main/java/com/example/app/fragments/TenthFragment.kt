package com.example.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.app.BuildConfig
import com.example.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import com.example.app.fragments.RootChecker.checkWriteAccess
import com.example.app.fragments.RootChecker.hasRootAccess

class TenthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tenth, container, false)

        // Дата сборки (у вас уже есть)
        val buildDate = Date(BuildConfig.BUILD_TIME)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        rootView.findViewById<TextView>(R.id.buildTimeText).text =
            "APK создан: ${formatter.format(buildDate)}"

        // ========== НОВОЕ: версия и ветка ==========
        val versionNameText = rootView.findViewById<TextView>(R.id.versionNameText)
        val branchText      = rootView.findViewById<TextView>(R.id.branchText)

        // Самый надёжный способ — берём из PackageInfo (то, что видит система и пользователь)
        val packageInfo = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0)

        val fullVersion = packageInfo.versionName ?: "unknown"
        versionNameText.text = "Версия приложения: $fullVersion"

        // Суффикс берём из BuildConfig (то, что мы сами положили в Gradle)
        val suffix = BuildConfig.VERSION_NAME_SUFFIX
        branchText.text = if (suffix.isNotEmpty()) "Ветка Git: $suffix" else "Ветка: release"

        // Root-чекбоксы (у вас уже есть)
        rootView.findViewById<CheckBox>(R.id.checkBox).isChecked =
            hasRootAccess(requireContext())
        rootView.findViewById<CheckBox>(R.id.checkBox2).isChecked =
            checkWriteAccess("/system")

        return rootView
    }
}