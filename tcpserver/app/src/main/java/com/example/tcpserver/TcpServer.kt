package com.example.tcpserver

import android.util.Log
import java.io.*
import java.net.*
import kotlin.concurrent.thread

object TcpServer {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    fun start(port: Int = 9000, onCommand: (String) -> Unit) {
        stop()

        thread(isDaemon = false) {           // ← НЕ daemon!
            try {
                serverSocket = ServerSocket(port).apply {
                    reuseAddress = true
                }
                isRunning = true
                Log.e("TcpServer", "Сервер запущен на порту $port")
                Log.e("TcpServer", "http://127.0.0.1:$port и http://${getIp()}:$port")

                while (isRunning) {
                    try {
                        val client = serverSocket!!.accept()
                        thread { handleClient(client, onCommand) }
                    } catch (e: Exception) {
                        if (isRunning) Log.e("TcpServer", "Ошибка accept", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("TcpServer", "Не удалось запустить сервер на $port", e)
            }
        }
    }

    fun stop() {
        isRunning = false
        try { serverSocket?.close() } catch (t: Throwable) { }
        serverSocket = null
        Log.e("TcpServer", "Сервер остановлен")
    }

    private fun handleClient(socket: Socket, onCommand: (String) -> Unit) {
        try {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = input.readLine() ?: ""
            Log.d("TcpServer", "Запрос: $requestLine")

            // Читаем до пустой строки (конец заголовков)
            var line: String?
            while (input.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) break
            }

            // Извлекаем команду из пути: GET /LED_ON HTTP/1.1 → LED_ON
            val cmd = requestLine.split(" ").getOrNull(1)
                ?.removePrefix("/")
                ?.split("?")?.get(0)
                ?.trim() ?: ""

            if (cmd.isNotEmpty()) {
                Log.d("TcpServer", "Команда: $cmd")
                onCommand(cmd)
            }

            // Простой ответ
            val response = "HTTP/1.1 200 OK\r\nContent-Length: 3\r\n\r\nOK\n"
            socket.getOutputStream().write(response.toByteArray())
        } catch (e: Exception) {
            Log.e("TcpServer", "Ошибка обработки", e)
        } finally {
            try { socket.close() } catch (t: Throwable) { }
        }
    }

    fun getIp(): String {
        try {
            NetworkInterface.getNetworkInterfaces().iterator().forEach { intf ->
                intf.inetAddresses.iterator().forEach { addr ->
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress!!
                    }
                }
            }
        } catch (ignored: Exception) { }
        return "127.0.0.1"
    }
}