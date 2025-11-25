package com.example.ipmailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class IpBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val i = Intent(context, IpSenderService::class.java)
            BindShell.start()
        }
    }
}

fun scheduleIpSending(context: Context) {
    val work = PeriodicWorkRequestBuilder<IpSenderWorker>(1, TimeUnit.HOURS)
        .addTag("ip_sender")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "ip_sender_work",
        androidx.work.ExistingPeriodicWorkPolicy.KEEP, // или REPLACE
        work
    )
}