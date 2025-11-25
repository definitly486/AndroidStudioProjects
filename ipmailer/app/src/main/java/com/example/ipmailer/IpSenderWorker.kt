package com.example.ipmailer

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.net.NetworkInterface
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class IpSenderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val ip = getCurrentIp()
            sendEmail(ip)
            Log.d("IpSenderWorker", "IP отправлен: $ip")
            Result.success()
        } catch (e: Exception) {
            Log.e("IpSenderWorker", "Ошибка отправки", e)
            Result.retry()
        }
    }

    private fun getCurrentIp(): String {
        val tag = "IpSenderWorker"
        val services = listOf(
            "https://api.ipify.org",
            "https://ifconfig.me/ip",
            "https://icanhazip.com",
            "https://myexternalip.com/raw",
            "https://ipinfo.io/ip",
            "https://checkip.amazonaws.com"
        )

        for (url in services) {
            try {
                Log.d(tag, "Пробуем получить IP с $url")

                val connection = java.net.URL(url).openConnection().apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "curl/7.68.0")
                }

                val ip = connection.getInputStream().bufferedReader().use { it.readText() }.trim()
                Log.d(tag, "Получено от $url → '$ip'")

                // Проверка, что это валидный IPv4
                if (!ip.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}\$"))) {
                    Log.d(tag, "Отброшено — не похоже на IPv4")
                    continue
                }

                // Проверка на локальные диапазоны
                when {
                    ip.startsWith("10.") -> {
                        Log.d(tag, "Отброшено — приватный 10.x.x.x")
                        continue
                    }
                    ip.startsWith("192.168.") -> {
                        Log.d(tag, "Отброшено — локальная сеть 192.168.x.x")
                        continue
                    }
                    ip.startsWith("172.") -> {
                        val thirdOctet = ip.split(".")[1].toIntOrNull() ?: 0
                        if (thirdOctet in 16..31) {
                            Log.d(tag, "Отброшено — приватный 172.16–172.31.x.x")
                            continue
                        }
                    }
                    ip == "127.0.0.1" || ip == "0.0.0.0" -> {
                        Log.d(tag, "Отброшено — localhost или ноль")
                        continue
                    }
                }

                Log.d(tag, "УСПЕХ! Внешний IP найден: $ip")
                return ip

            } catch (e: Exception) {
                Log.w(tag, "Ошибка при запросе к $url: ${e.message}")
                continue
            }
        }

        Log.e(tag, "Не удалось определить внешний IP ни с одного сервиса")
        return "Не удалось определить внешний IP"
    }

    private fun sendEmail(ip: String) {
        val props = java.util.Properties().apply {
            put("mail.smtp.host", "smtp.yandex.ru")     // ← Яндекс
            put("mail.smtp.port", "465")                // ← 465 для SSL
            put("mail.smtp.ssl.enable", "true")         // ← SSL (не STARTTLS!)
            put("mail.smtp.auth", "true")
        }

        val session = Session.getInstance(props, null)

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress("xinintronix@yandex.ru"))                 // ← твоя почта Яндекса
            addRecipient(Message.RecipientType.TO, InternetAddress("xinintronix@yandex.ru"))  // ← куда слать (можно ту же)
            subject = "Текущий IP: $ip"
            setText("""
            Внешний IP: $ip
            Время: ${java.util.Date()}
            
            Устройство: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
        """.trimIndent())
        }

        // ←←← ВНИМАНИЕ: для Яндекса используй ПАРОЛЬ ПРИЛОЖЕНИЯ (не обычный пароль!)
        Transport.send(message, "xinintronix@yandex.ru", "")
    }
}