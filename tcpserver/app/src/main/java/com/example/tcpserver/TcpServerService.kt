package com.example.tcpserver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TcpServerService : Service() {

    companion object {
        private const val CHANNEL_ID = "tcp_server_channel"
        private const val FOREGROUND_ID = 1
    }

    private var isServerRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (!isServerRunning) {
            startTcpServer()
            isServerRunning = true
        }

        // Постоянное уведомление (foreground service)
        startForeground(FOREGROUND_ID, createForegroundNotification())

        return START_STICKY
    }

    private fun startTcpServer() {
        TcpServer.start(port = 9000) { command ->

            // ←←← ВИДИМ В ЛОГАХ, ЧТО КОМАНДА ПРИШЛА
            android.util.Log.e("TCP_SERVICE", "Получена команда: $command")

            // ←←← ОТПРАВЛЯЕМ В MainActivity через LocalBroadcast (100% надёжно)
            val localIntent = Intent("TCP_COMMAND")
            localIntent.putExtra("command", command)
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

            // ←←← Уведомление о полученной команде
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Команда получена")
                .setContentText(command)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            nm.notify(1000 + command.hashCode(), notification)
        }
    }

    override fun onDestroy() {
        TcpServer.stop()
        isServerRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TCP Server Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления от TCP-сервера"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TCP сервер работает")
            .setContentText("http://${TcpServer.getIp()}:9000")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}