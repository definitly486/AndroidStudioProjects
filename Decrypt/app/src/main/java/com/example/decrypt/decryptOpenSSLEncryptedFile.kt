package com.example.decrypt

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.io.File
import java.security.spec.KeySpec

fun decryptOpenSSLEncryptedFile(encryptedFile: File, password: CharArray): String? {
    val fileBytes = encryptedFile.readBytes()

    // Assuming "Salted__" header and 8-byte salt
    val headerSize = 8 // "Salted__".toByteArray().size
    val saltSize = 8
    val expectedHeader = "Salted__".toByteArray(Charsets.US_ASCII)

    if (!fileBytes.copyOfRange(0, headerSize).contentEquals(expectedHeader)) {
        println("File does not have expected OpenSSL 'Salted__' header.")
        return null
    }

    val salt = fileBytes.copyOfRange(headerSize, headerSize + saltSize)
    val cipherText = fileBytes.copyOfRange(headerSize + saltSize, fileBytes.size)

    // Derive key and IV (adjust iterations, keyLength, ivSize as per OpenSSL encryption)
    val iterations = 10000 // Example, match OpenSSL's -pbkdf2 iterations
    val keyLength = 256 // AES-256
    val ivSize = 16 // AES block size for IV
 //   Security.addProvider( BouncyCastleProvider())
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec: KeySpec = PBEKeySpec(password, salt, iterations, keyLength + ivSize * 8)
    val tmp = factory.generateSecret(spec)
    val derivedBytes = tmp.encoded

    val key = derivedBytes.copyOfRange(0, keyLength / 8)
    val iv = derivedBytes.copyOfRange(keyLength / 8, keyLength / 8 + ivSize)

    val secretKey = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)

    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding") // Or appropriate mode/padding
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

    return try {
        String(cipher.doFinal(cipherText)).trim()
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

// Example usage:
// val encryptedFile = File("encrypted_data.bin")
// val decryptedContent = decryptOpenSSLEncryptedFile(encryptedFile, "your_password".toCharArray())
// if (decryptedContent != null) {
//     println("Decrypted content: $decryptedContent")
// }