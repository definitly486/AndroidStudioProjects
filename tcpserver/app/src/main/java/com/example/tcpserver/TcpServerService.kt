package com.example.tcpserver

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TcpServerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "tcp_server_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())  // ← ВАЖНО: сразу в onCreate!
        TcpServer.start(port = 9000) { command ->
            // Пришла команда — отправляем в MainActivity
            val intent = Intent("com.example.tcpserver.COMMAND_RECEIVED")
            intent.putExtra("command", command)
            sendBroadcast(intent)

            // Показываем уведомление с командой
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1000 + command.hashCode(), NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Команда получена")
                .setContentText(command)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY  // Перезапустится, если убьют
    }

    override fun onDestroy() {
        TcpServer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TCP Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Постоянный TCP сервер на порту 9000"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TCP сервер активен")
            .setContentText("http://${TcpServer.getIp()}:9000")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}