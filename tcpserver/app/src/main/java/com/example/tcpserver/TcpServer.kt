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
            socket.soTimeout = 3000  // важно! защита от зависаний

            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = input.readLine() ?: return
            Log.d("TcpServer", "Запрос: $requestLine")

            // Безопасно читаем заголовки
            var line: String?
            while (true) {
                try {
                    line = input.readLine()
                    if (line == null || line.isBlank()) break
                    // Log.d("TcpServer", "Header: $line")  // раскомментируй при отладке
                } catch (e: IOException) {
                    // Клиент закрыл соединение — это НОРМА для HTTP/1.1 без keep-alive
                    break
                }
            }

            // Извлекаем команду
            val path = requestLine.split(" ").getOrNull(1) ?: ""
            val cmd = path.removePrefix("/").split("?").first().trim().uppercase()

            if (cmd.isNotEmpty()) {
                Log.d("TcpServer", "Команда: $cmd")  // ← Теперь ВСЕГДА видно!
                onCommand(cmd)
            }

            // Отправляем ответ ДО закрытия
            val response = "HTTP/1.1 200 OK\r\nContent-Length: 3\r\nConnection: close\r\n\r\nOK\n"
            output.write(response.toByteArray())
            output.flush()

        } catch (e: SocketTimeoutException) {
            Log.w("TcpServer", "Таймаут клиента")
        } catch (e: IOException) {
            // Это нормально при закрытии соединения клиентом
            if (!e.message?.contains("Socket closed", true)!! &&
                !e.message?.contains("Broken pipe", true)!! &&
                !e.message?.contains("Connection reset", true)!!) {
                Log.e("TcpServer", "Реальная ошибка обработки", e)
            }
            // Иначе — молчим, это нормальное поведение HTTP
        } catch (t: Throwable) {
            Log.e("TcpServer", "Критическая ошибка", t)
        } finally {
            try { socket.close() } catch (ignored: Throwable) {}
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