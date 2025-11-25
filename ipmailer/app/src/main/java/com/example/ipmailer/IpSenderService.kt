@file:Suppress("DEPRECATION")

package com.example.ipmailer
import android.app.IntentService
import android.content.Intent
import android.os.Build
import java.net.URL
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class IpSenderService : IntentService("IpSenderService") {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        try {
            val ip = getExternalIp()
            sendEmail(ip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getExternalIp(): String {
        val urls = listOf(
            "https://api.ipify.org",
            "https://icanhazip.com",
            "https://checkip.amazonaws.com"
        )

        for (u in urls) {
            try {
                val conn = URL(u).openConnection().apply {
                    connectTimeout = 7000
                    readTimeout = 7000
                }
                val ip = conn.getInputStream()
                    .bufferedReader().use { it.readText().trim() }

                if (ip.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")))
                    return ip

            } catch (_: Exception) { }
        }

        return "Unknown"
    }

    private fun sendEmail(ip: String) {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.yandex.ru")
            put("mail.smtp.port", "465")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.auth", "true")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            @Suppress("SpellCheckingInspection")
            override fun getPasswordAuthentication() =
                PasswordAuthentication(
                    "xinintronix@yandex.ru",
                    "" // ← пароль приложения Яндекс
                )
        })

        @Suppress("SpellCheckingInspection") val msg = MimeMessage(session).apply {
            setFrom(InternetAddress("xinintronix@yandex.ru"))
            addRecipient(Message.RecipientType.TO, InternetAddress("xinintronix@yandex.ru"))
            subject = "IP после загрузки устройства"
            setText("IP: $ip\nМодель: ${Build.MODEL}\nВремя: ${Date()}")
        }

        Transport.send(msg)
    }
}
