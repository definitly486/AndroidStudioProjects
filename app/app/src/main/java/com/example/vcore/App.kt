package com.example.vcore

import android.app.Application
import timber.log.Timber
//import com.example.vcore.BuildConfig  // Import the BuildConfig explicitly

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //     if (BuildConfig.DEBUG) {
        //         Timber.plant(Timber.DebugTree())
        //     }
        // }
    }
}