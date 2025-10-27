package com.example.app.fragments

import android.content.Context
import android.content.pm.PackageManager
object RootChecker {
    fun hasRootAccess(context: Context): Boolean {
        return checkSuBinary() || checkSuperUserApps(context)
    }



    private fun checkSuBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", "su -c id"))
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun checkSuperUserApps(context: Context): Boolean {
        val packageNames = arrayOf(
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.topjohnwu.magisk"
        )

        for (pkg in packageNames) {
            try {
                context.packageManager.getApplicationInfo(pkg, 0)
                return true
            } catch (ignored: PackageManager.NameNotFoundException) {}
        }
        return false
    }



}