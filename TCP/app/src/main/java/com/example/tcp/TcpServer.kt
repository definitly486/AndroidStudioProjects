package com.example.tcp

import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class TcpServer {

    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null

    fun startServer(
        port: Int = 8008,
        onCommand: (String) -> Unit
    ) {
        stopServer() // если вдруг уже запущен

        serverJob = CoroutineScope(Dispatchers.IO).launch {

            try {
                serverSocket = ServerSocket(port)
                println("Сервер запущен на порту $port")

                while (isActive) {
                    val client = try {
                        serverSocket?.accept()
                    } catch (e: IOException) {
                        if (!isActive) break
                        println("Ошибка accept(): ${e.message}")
                        continue
                    }

                    if (client != null) {
                        println("Клиент подключился: ${client.inetAddress.hostAddress}")

                        launch { handleClient(client, onCommand) }
                    }
                }

            } catch (e: Exception) {
                println("Ошибка запуска сервера: ${e.message}")
            } finally {
                closeServerSocket()
            }
        }
    }

    private suspend fun handleClient(
        client: Socket,
        onCommand: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            client.use { socket ->
                try {
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val output = PrintWriter(socket.getOutputStream(), true)

                    while (isActive && !socket.isClosed) {
                        val line = input.readLine() ?: break
                        println("Получено: $line")

                        onCommand(line)

                        output.println("OK: received $line")
                    }
                } catch (e: IOException) {
                    println("Ошибка клиента: ${e.message}")
                }
            }
            println("Клиент отключился")
        }
    }

    fun stopServer() {
        serverJob?.cancel()
        closeServerSocket()
        println("Сервер остановлен.")
    }

    private fun closeServerSocket() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
    }
}
