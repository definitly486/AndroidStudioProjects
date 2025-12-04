package com.example.tcpserver

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class TcpServerService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TcpServer::WakeLock")
        wakeLock?.acquire(24*60*60*1000L)

        TcpServer.start(port = 9000) { command ->
            println("Команда получена: $command")

            // Отправляем команду в MainActivity через LocalBroadcast (самый надёжный способ)
            val broadcastIntent = Intent("com.example.tcp.COMMAND_RECEIVED")
            broadcastIntent.putExtra("command", command)
            sendBroadcast(broadcastIntent)

            // Показываем уведомление
            showCommandNotification(command).toString()
        }

        return START_STICKY
    }

    private fun showCommandNotification(command: String) {
        val notification = NotificationCompat.Builder(this, "tcp_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Получена команда")
            .setContentText(command)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(command.hashCode(), notification)
    }

    override fun onDestroy() {
        TcpServer.stop()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "tcp_channel")
            .setContentTitle("TCP сервер работает")
            .setContentText("http://${TcpServer.getIp()}:8008")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // или своя иконка
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tcp_channel",
                "TCP Server Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Постоянный HTTP сервер" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}