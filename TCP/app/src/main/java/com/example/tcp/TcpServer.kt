package com.example.tcp



import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class TcpServer {  // оставляем старое имя класса — ничего менять в остальном коде не придётся
    private var server: MyHttpServer? = null
    private var serverJob: Job? = null

    fun startServer(
        port: Int = 8008,
        onCommand: (String) -> Unit
    ) {
        stopServer()

        serverJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    server = MyHttpServer(port, onCommand)
                    server?.start()
                    println("Локальный HTTP-сервер запущен на http://127.0.0.1:$port")

                    // Ждём, пока сервер жив (блокирует поток, как и должно быть)
                    while (isActive && server?.isAlive == true) {
                        delay(500)
                    }
                } catch (e: Exception) {
                    println("Ошибка запуска HTTP-сервера: ${e.message}")
                }

                // Если мы здесь — сервер упал
                server?.stop()
                server = null

                if (isActive) {
                    println("Перезапуск сервера через 3 секунды...")
                    delay(3000)
                }
            }
        }
    }

    fun stopServer() {
        serverJob?.cancel()
        serverJob = null
        server?.stop()
        server = null
        println("Сервер остановлен.")
    }

    // Внутренний класс — наш HTTP-сервер
    private class MyHttpServer(
        port: Int,
        private val onCommand: (String) -> Unit
    ) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response {
            return try {
                // Поддерживаем GET и POST
                val body = when (session.method) {
                    Method.POST -> {
                        val files = HashMap<String, String>()
                        session.parseBody(files)
                        files["postData"] ?: ""
                    }
                    Method.GET -> session.queryParameterString ?: ""
                    else -> ""
                }

                val command = when {
                    body.isNotBlank() -> body
                    session.uri.length > 1 -> session.uri.substring(1) // /hello → hello
                    else -> ""
                }.trim()

                if (command.isNotEmpty()) {
                    println("Получена команда: $command")
                    onCommand(command)
                }

                newFixedLengthResponse("OK\n")
            } catch (e: Exception) {
                println("Ошибка обработки запроса: $e")
                newFixedLengthResponse("ERROR\n")
            }
        }
    }
}